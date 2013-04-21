/*
 * Copyright 2012 Russell Morris (wrussellmorris@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smf4j.core.accumulator.hc;

import java.util.concurrent.atomic.AtomicLongArray;
import org.smf4j.Mutator;
import org.smf4j.core.accumulator.IntervalStrategy;
import org.smf4j.core.accumulator.TimeReporter;

/**
 * {@code AbstractWindowedMutator} serves as a base class for high-contention,
 * <em>windowed</em> {@link Mutator}s that are designed to be written to by
 * <strong>exactly</strong> one thread at a time, but safely readable by any
 * number of threads.
 * <p>
 * A <em>windowed</em> {@link Mutator} is a {@code Mutator} that combines and
 * reports the values it was shown between <em>now</em> and
 * <em>now</em> - <em>time window</em>.  For example, if the
 * <em>time window</em> for a {@link WindowedAddMutator} was 10 seconds, its
 * {@link WindowedAddMutator#get() get()} would return the sum of all of the
 * values it was show via its {@link WindowedAddMutator#put(long) put} that
 * occurred within the last 10 seconds.
 * </p>
 * <p>
 * The backing store for the information is two fixed-size circular buffers
 * recording timestamps and values. An instance of an {@link IntervalStrategy}
 * is used to determine the size of these buffers, as well as indexing
 * into them.
 * </p>
 * <p>
 * {@code AbstractWindowedMutator} instances will require storage space
 * proportional to the number of intervals indicated by the associated
 * {@link IntervalStrategy}.  All storage is allocated during construction -
 * once constructed, {@code AbstractWindowedMutator} never allocates any more
 * storage space as a result of reads or writes.
 * </p>
 * <p>
 * Subclasses are required to implement both {@link #combine(long)} and
 * {@link #combine(long, long)}.
 * </p>
 *
 * @see HighContentionAccumulator
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public abstract class AbstractWindowedMutator implements Mutator {

    private final TimeReporter timeReporter;
    private final int buckets;
    private final int intervals;
    private final int bufferIntervals;
    private final AtomicLongArray values;
    private final long[] localTimestamps;
    private final AtomicLongArray timestamps;
    private final long intervalResolutionInNanos;
    private final IntervalStrategy strategy;
    private final long staleWindowTimestampOffset;
    private final long initialValue;

    /**
     * Creates a new instance of {@code AbstractWindowedMutator}.
     * <p>
     * {@code initialValue} should be chosen so that it acts as an identity
     * in the {@link #combine(long)} and {@link #combine(long, long)}
     * operations.
     * </p>
     * @param initialValue The initial value reported by this
     *                     {@code AbstractWindowedMutator}
     * @param strategy The {@link IntervalStrategy} used to allocated and manage
     *                 the timestamp and value buffers.
     * @param timeReporter The {@link TimeReporter} used to determine the
     *                     current time, in nanoseconds.
     */
    protected AbstractWindowedMutator(long initialValue,
            IntervalStrategy strategy, TimeReporter timeReporter) {
        this.initialValue = initialValue;
        this.timeReporter = timeReporter;
        this.strategy = strategy;
        this.intervals = strategy.intervals();
        this.bufferIntervals = strategy.bufferIntervals();
        this.intervalResolutionInNanos = strategy.intervalResolutionInNanos();
        this.buckets = intervals + bufferIntervals;
        this.values = new AtomicLongArray(buckets);
        this.timestamps = new AtomicLongArray(buckets);
        this.localTimestamps = new long[buckets];
        this.staleWindowTimestampOffset = intervalResolutionInNanos * buckets;
    }

    public final void put(long delta) {
        long nanos = timeReporter.nanos();
        int index = strategy.intervalIndex(nanos);
        long stale = nanos - intervalResolutionInNanos;
        if(localTimestamps[index] < stale) {
            // This bucket is stale
            timestamps.lazySet(index, nanos);
            localTimestamps[index] = nanos;
            values.lazySet(index, delta);
        } else {
            // Bucket's still fresh...
            values.lazySet(index, combine(values.get(index), delta));
        }
    }

    /**
     * A variant of {@link #combine(long)} that returns the combined value of
     * {@code local} and {@code delta}.
     * <p>
     * This {@code protected} method is used by {@link #put(long) put} to
     * combine the existing buffer value with the value passed to {@code put}.
     * </p>
     * @param local The current local value in the buffer.
     * @param delta A new value to be combined with {@code local}.
     * @return The combination of {@code local} and {@code delta}.
     */
    protected abstract long combine(long local, long delta);

    public final long get() {
        long nanos = timeReporter.nanos();
        long result = initialValue;
        int index = strategy.intervalIndex(nanos);
        long stale = nanos - staleWindowTimestampOffset;
        for(int count=0,i=parw(index-bufferIntervals);
            count<intervals;
            i = parw(i-1),count++) {

            long bucketTimestamp = timestamps.get(i);
            if(bucketTimestamp >= stale) {
                result = combine(result, values.get(i));
            }
        }

        return result;
    }

    /**
     * Returns a copy of the value buffer.
     * <p>
     * Any buffer values that are stale will be reported as the
     * {@code initialValue} passed to the constructor.  The first value in
     * the returned array will be the value for the most recent interval, and
     * the last value in the array will be for the oldest recorded interval.
     * </p>
     * @param nanos The current time.
     * @return A copy of the value buffer.
     */
    public final long[] buckets(long nanos) {
        int index = strategy.intervalIndex(nanos);
        long[] ret = new long[intervals];
        long stale = nanos - staleWindowTimestampOffset;
        for(int count=0,i=parw(index-bufferIntervals);
            count<intervals;
            i = parw(i-1),count++) {

            long bucketTimestamp = timestamps.get(i);
            if(bucketTimestamp >= stale) {
                ret[count] = values.get(i);
            } else {
                ret[count] = initialValue;
            }
        }

        return ret;
    }

    /**
     * Decrements {@code index}, wrapping around to the end of the list if
     * {@code index == 0}.
     * @param index The index to decrement.
     * @return {@code index - 1}, or {@code buckets + index} if
     * {@code index < 0}.
     */
    private int parw(int index) {
        if(index < 0) {
            return buckets+index;
        }
        return index;
    }
}
