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
import org.smf4j.Mutator;

/**
 * Implementations of the {@code IntervalStrategy} interface are used by
 * <em>windowed</em> {@link Mutator}s to map some notion of the current time
 * in nanoseconds to an index into a relatively small number of intervals.
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public interface IntervalStrategy {

    /**
     * The key in {@link Accumulator#getMetadata()} that returns the total
     * time window, in nanoseconds, spanned by the intervals in this strategy.
     */
    public static final String METADATA_TIME_WINDOW = "timewindow";

    /**
     * The key in {@link Accumulator#getMetadata()} that returns the total
     * number of reported intervals in this strategy.
     */
    public static final String METADATA_INTERVALS = "intervals";

    /**
     * Gets the number of reported intervals for this strategy.
     * @return The number of reported intervals for this strategy.
     */
    int intervals();

    /**
     * Gets the number of unreported intervals reserved as a buffer to ensure
     * that reads won't read partially-full intervals.
     * @return The number of unreported intervals reserved as a buffer to ensure
     *         that reads won't read partially-full intervals.
     */
    int bufferIntervals();

    /**
     * Gets the time window, in nanoseconds, spanned by the intervals in this
     * strategy.
     * @return The time window, in nanoseconds, spanned by the intervals in this
     *         strategy.
     */
    long timeWindowInNanos();

    /**
     * Gets the length of time, in nanoseconds, spanned by a single interval in
     * this strategy.
     * @return The length of time, in nanoseconds, spanned by a single interval
     *         in this strategy.
     */
    long intervalResolutionInNanos();

    /**
     * Gets the interval index, in the range {@code [0, intervals)}, for the
     * given time {@code nanos}.
     * @param nanos The time whose interval index is to be found.
     * @return The interval index, in the range {@code [0, intervals)}, for the
     *         given time {@code nanos}.
     */
    int intervalIndex(long nanos);
}
