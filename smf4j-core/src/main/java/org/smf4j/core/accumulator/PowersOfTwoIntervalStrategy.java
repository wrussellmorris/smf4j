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

import org.smf4j.core.calculator.Normalizer;
import org.smf4j.Accumulator;

/**
 * {@code PowersOfTwoIntervalStrategy} is an implementation of
 * {@link IntervalStrategy} that tracks a time window whose nominal time span is
 * {@code 2^n} nanoseconds split into {@code 2^i} equal intervals, for some
 * pair {@code n}, {@code i} where {@code n > i}.  While this interval
 * strategy is certainly a bit stranger and more cumbersome than the
 * {@link SecondsIntervalStrategy}, it is <strong>much</strong> faster at
 * calculating the interval index for the current time, as it only needs to
 * do a <em>mask</em> and <em>right-shift</em>, whereas
 * {@link SecondsIntervalStrategy} must do an <em>integer division</em> and
 * <em>modulus</em>.
 * <p>
 * The exact width of the reported time window is actually a little smaller than
 * {@code 2^n} nanoseconds, as 2 intervals are reserved as buffers so that
 * reads of the intervals never read an interval that could still be written to
 * in the very near future.
 * </p>
 * <p>
 * An interval's time span in nanoseconds is given by {@code 2^(n-i)}.  Exactly
 * two intervals are reserved for buffering.  As such, the actual total time
 * window reported by this interval strategy will be {@code 2^n - 2*2^(n-i)}
 * nanoseconds.
 * </p>
 * <p>
 * Consider using the {@link Normalizer} calculator to normalize the reported
 * value of windowed {@link Accumulator}s using
 * {@code PowersOfTwoIntervalStrategy} to a more easily-comprehendible
 * frequency.
 * </p>
 *
 * @see SecondsIntervalStrategy
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class PowersOfTwoIntervalStrategy implements IntervalStrategy {

    private static final long ONE_BILLION = 1000000000L;

    /**
     * The maximum interval exponent.
     */
    public static final int MAX_NUM_INTERVALS_EXP = 6;

    /**
     * The number of buffer intervals.
     */
    public static final int BUFFER_INTERVALS = 2;

    /**
     * If intervals are below this resolution, then use buffer intervals.
     */
    private static final long MAX_RESOLUTION_FOR_BUFFER_INTERVALS =
            1 * ONE_BILLION;

    private final long totalWindowInNanos;
    private final long reportedWindowInNanos;
    private final int intervals;
    private final int intervalWidthExp;
    private final int reportedIntervals;
    private final long intervalResolutionInNanos;
    private final long indexMask;
    private final int bufferIntervals;

    /**
     * Creates a new instance of {@code PowersOfTwoIntervalStrategy} with the
     * time window of just under {@code 2^(timeWindowExp)} nanoseconds cut into
     * {@code 2^(intervalExp)} intervals, each of width
     * {@code 2^(timeWindowExp-intervalExp)} nanoseconds.
     * @param timeWindowExp The power of 2 defining the time window, in
     *                      nanoseconds.
     * @param intervalExp The power of 2 defining the number of intervals that
     *                    the time window is split into.
     */
    public PowersOfTwoIntervalStrategy(int timeWindowExp, int intervalExp) {

        if(timeWindowExp < 0) {
            throw new IllegalArgumentException("timeWindowExp must be > 0");
        }

        if(intervalExp < 0) {
            throw new IllegalArgumentException("intervalExp must be > 0");
        }

        final int expDiff = timeWindowExp - intervalExp;
        if(expDiff < 0 || intervalExp > MAX_NUM_INTERVALS_EXP) {
            throw new IllegalArgumentException(
                    "(timeWindowExp - intervalExp) must be >= 0 and"
                    + "<= " + MAX_NUM_INTERVALS_EXP);
        }

        this.intervalWidthExp = (timeWindowExp - intervalExp);

        // Total window is 2^timeWindowExp nanoseconds
        this.totalWindowInNanos = 1L << (long)timeWindowExp;

        // Interval resolution is 2^(timeWindowExp - intervalExp) nanoseconds
        this.intervalResolutionInNanos = 1L << (long)(timeWindowExp - intervalExp);

        // indexMask zeros all bits to the left of timeWindowExp
        this.indexMask = (1L << (long)timeWindowExp) - 1L;

        // Total number of intervals is 2^intervalExp nanos
        this.intervals = 1 << intervalExp;

        // Resolutions below a threshold will use extra buffer intervals
        if(intervalResolutionInNanos <= MAX_RESOLUTION_FOR_BUFFER_INTERVALS) {
            this.bufferIntervals = BUFFER_INTERVALS;
        } else {
            this.bufferIntervals = 0;
        }

        // reportedIntervals reserves bufferIntervals intervals for buffering
        this.reportedIntervals = intervals - bufferIntervals;

        // bufferIntervals must be less than intervals
        if(reportedIntervals <= 0) {
            throw new IllegalArgumentException(
                    "2^(intervalExp)-BUFFER_INTERVALS must be > 0");
        }

        // reportedWindowInNanos takes into account intervals reserved
        // for buffering
        this.reportedWindowInNanos = totalWindowInNanos -
                ((long)bufferIntervals * intervalResolutionInNanos);
    }

    public int intervals() {
        return reportedIntervals;
    }

    public long intervalResolutionInNanos() {
        return intervalResolutionInNanos;
    }

    public long timeWindowInNanos() {
        return reportedWindowInNanos;
    }

    public int intervalIndex(long nanos) {
        return (int)((nanos & indexMask) >> intervalWidthExp);
    }

    public int bufferIntervals() {
        return bufferIntervals;
    }
}
