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

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class SecondsIntervalStrategy implements IntervalStrategy {

    public static final long ONE_BILLION = 1000000000L;
    public static final int MAX_INTERVALS = 100;
    public static final int BUFFER_INTERVALS = 2;

    private final long timeWindowInNanos;
    private final int intervals;
    private final int totalIntervals;
    private final long intervalResolutionInNanos;

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

        // Extend timeWindowInNanos so that it is evenly
        // divided by intervals
        this.timeWindowInNanos = ((long)timeWindowInSeconds) * ONE_BILLION;

        // Figure out the interval resolution
        this.intervalResolutionInNanos = timeWindowInNanos / intervals;
    }

    @Override
    public int intervals() {
        return intervals;
    }

    @Override
    public int bufferIntervals() {
        return BUFFER_INTERVALS;
    }

    @Override
    public long intervalResolutionInNanos() {
        return intervalResolutionInNanos;
    }

    @Override
    public long timeWindowInNanos() {
        return timeWindowInNanos;
    }

    @Override
    public int intervalIndex(long nanos) {
        int index = (int)((nanos / intervalResolutionInNanos) % totalIntervals);
        return index;
    }
}