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
package org.smf4j.core.accumulator;

import java.util.concurrent.atomic.AtomicLongArray;
import org.smf4j.Mutator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class Intervals implements Mutator {

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

    Intervals(IntervalStrategy strategy, TimeReporter timeReporter) {
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

    @Override
    public void add(long delta) {
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
            values.lazySet(index, values.get(index) + delta);
        }
    }

    @Override
    public long syncGet() {
        long nanos = timeReporter.nanos();
        long result = 0L;
        int index = strategy.intervalIndex(nanos);
        long stale = nanos - staleWindowTimestampOffset;
        for(int count=0,i=parw(index-bufferIntervals);
            count<intervals;
            i = parw(i-1),count++) {

            long bucketTimestamp = timestamps.get(i);
            if(bucketTimestamp >= stale) {
                result += values.get(i);
            }
        }

        return result;
    }

    @Override
    public long localGet() {
        long nanos = timeReporter.nanos();
        long result = 0L;
        int index = strategy.intervalIndex(nanos);
        long stale = nanos - staleWindowTimestampOffset;
        for(int count=0,i=parw(index-bufferIntervals);
            count<intervals;
            i = parw(i-1),count++) {

            long bucketTimestamp = localTimestamps[i];
            if(bucketTimestamp >= stale) {
                result += values.get(i);
            }
        }

        return result;
    }

    public long[] buckets(long nanos) {
        int index = strategy.intervalIndex(nanos);
        long[] ret = new long[intervals];
        long stale = nanos - staleWindowTimestampOffset;
        for(int count=0,i=parw(index-bufferIntervals);
            count<intervals;
            i = parw(i-1),count++) {

            long bucketTimestamp = timestamps.get(i);
            if(bucketTimestamp >= stale) {
                ret[count] = values.get(i);
            }
        }

        return ret;
    }

    private int parw(int index) {
        if(index < 0) {
            return buckets+index;
        }
        return index;
    }
}
