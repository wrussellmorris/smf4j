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
public final class PowersOfTwoIntervalStrategy implements IntervalStrategy {

    public static final long MAX_NUM_INTERVALS_EXP = 6;
    public static final long MAX_BUFFER_INTERVALS = 2;

    private final long totalWindowInNanos;
    private final long reportedWindowInNanos;
    private final int intervals;
    private final int intervalWidthExp;
    private final int reportedIntervals;
    private final int bufferIntervals;
    private final long intervalResolutionInNanos;
    private final long indexMask;

    public PowersOfTwoIntervalStrategy(
            int timeWindowExp,
            int intervalExp,
            int bufferIntervals) {

        if(timeWindowExp < 0) {
            throw new IllegalArgumentException("timeWindowExp must be > 0");
        }

        if(intervalExp < 0) {
            throw new IllegalArgumentException("intervalExp must be > 0");
        }

        if(bufferIntervals < 0 || bufferIntervals > MAX_BUFFER_INTERVALS) {
            throw new IllegalArgumentException(
                    "bufferIntervals must be >= 0 and <= " +
                    MAX_BUFFER_INTERVALS);
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

        // bufferIntervals requires no interpretation on its own
        this.bufferIntervals = bufferIntervals;

        // reportedIntervals reserves bufferIntervals intervals for buffering
        this.reportedIntervals = intervals - bufferIntervals;

        // bufferIntervals must be less than intervals
        if(reportedIntervals <= 0) {
            throw new IllegalArgumentException(
                    "2^(timeWindowExp - intervalExp)-bufferIntervals must "
                    + "be > 0");
        }

        // reportedWindowInNanos takes into account intervals reserved
        // for buffering
        this.reportedWindowInNanos = totalWindowInNanos -
                ((long)bufferIntervals * intervalResolutionInNanos);
    }

    @Override
    public int intervals() {
        return reportedIntervals;
    }

    @Override
    public int bufferIntervals() {
        return bufferIntervals;
    }

    @Override
    public long intervalResolutionInNanos() {
        return intervalResolutionInNanos;
    }

    @Override
    public long timeWindowInNanos() {
        return reportedWindowInNanos;
    }

    @Override
    public int intervalIndex(long nanos) {
        return (int)((nanos & indexMask) >> intervalWidthExp);
    }
}
