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

import org.smf4j.Accumulator;

/**
 * {@code SecondsIntervalStrategy} is an implementation of
 * {@link IntervalStrategy} that tracks a time window whose time span is given
 * in seconds, and is split into equally sized intervals.
 * <p>
 * While {@code SecondsIntervalStrategy} feels more natural, its
 * {@link #intervalIndex(long) intervalIndex} requires an
 * <em>integer division</em> and <em>modulus</em>, making it much slower than
 * {@link PowersOfTwoIntervalStrategy#intervalIndex(long) PowersOfTwoIntervalStrategy.intervalIndex}.
 * Consider using the latter for {@link Accumulator}s that are written very
 * frequently.
 * </p>
 *
 * @see PowersOfTwoIntervalStrategy
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class SecondsIntervalStrategy implements IntervalStrategy {

    private static final long ONE_BILLION = 1000000000L;

    /**
     * The maximum number of intervals supported.
     */
    public static final int MAX_INTERVALS = 100;

    /**
     * The number of buffer intervals used.
     */
    public static final int BUFFER_INTERVALS = 2;

    private final long timeWindowInNanos;
    private final int intervals;
    private final int totalIntervals;
    private final long intervalResolutionInNanos;

    /**
     * Creates a new {@code SecondsIntervalStrategy} instance tracking a time
     * window of {@code timeWindowInSeconds} seconds, split into
     * {@code intervals} equally-sized intervals.
     * @param timeWindowInSeconds The time window, in seconds.
     * @param intervals The number of intervals the time window is split into.
     */
    public SecondsIntervalStrategy(int timeWindowInSeconds, int intervals) {

        if(timeWindowInSeconds <= 0) {
            throw new IllegalArgumentException(
                    "timeWindowInSeconds must be > 0");
        }

        if(intervals <= 0 || intervals > MAX_INTERVALS) {
            throw new IllegalArgumentException(
                    "intervals must be > 0 and < " + MAX_INTERVALS);
        }

        this.intervals = intervals;
        this.totalIntervals = intervals + BUFFER_INTERVALS;
        this.timeWindowInNanos = ((long)timeWindowInSeconds) * ONE_BILLION;

        // Figure out the interval resolution
        this.intervalResolutionInNanos = timeWindowInNanos / intervals;
    }

    public int intervals() {
        return intervals;
    }

    public int bufferIntervals() {
        return BUFFER_INTERVALS;
    }

    public long intervalResolutionInNanos() {
        return intervalResolutionInNanos;
    }

    public long timeWindowInNanos() {
        return timeWindowInNanos;
    }

    public int intervalIndex(long nanos) {
        int index = (int)((nanos / intervalResolutionInNanos) % totalIntervals);
        return index;
    }
}