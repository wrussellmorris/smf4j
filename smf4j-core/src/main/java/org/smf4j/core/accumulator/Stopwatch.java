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
 * {@code Stopwatch} is a simple utility class for keeping track of how much
 * time has passed.  It supports laps and pause/resume.
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class Stopwatch {
    private final TimeReporter timeReporter;
    private long total = 0L;
    private long laps = 0L;
    private long mark = 0L;
    private long intraLap = 0L;

    /**
     * Creates a new {@code Stopwatch} instance.
     */
    public Stopwatch() {
        this(SystemNanosTimeReporter.INSTANCE);
    }

    /**
     * Creates a new {@code Stopwatch} instance with the given
     * {@code TimeReporter}.
     * <p>
     * This constructor is intended for unit-testing scenarios.
     * </p>
     * @param timeReporter The {@link TimeReporter} to use.
     */
    public Stopwatch(TimeReporter timeReporter) {
        this.timeReporter = timeReporter;
    }

    /**
     * Starts the stopwatch, clearing any prior state.
     */
    public void start() {
        mark = timeReporter.nanos();
        total = 0L;
        laps = 0L;
        intraLap = 0L;
    }

    /**
     * Marks a lap, returning the amount of time that has passed since the
     * most recent call to {@link #start()} or {@link #lap()}.
     * <p>
     * The amount of time spent in the lap is added to the total recorded by
     * the stopwatch.
     * </p>
     * @return Returns the amount of time that has passed since the most recent
     *         call to {@link #start()} or {@link #lap()}, or {@code 0} if
     *         the stopwatch has not been started.
     */
    public long lap() {
        if(mark == 0L) {
            return 0L;
        }

        long nanos = timeReporter.nanos();
        long delta = nanos - mark + intraLap;
        mark = nanos;
        total += delta;
        laps++;
        intraLap = 0L;
        return delta;
    }

    /**
     * Stops the stopwatch, returning the amount of time that has passed since
     * the most recent call to {@link #start()} or {@link #lap()}.
     * <p>
     * The amount returned is also added to the total recorded by the stopwatch.
     * </p>
     * @return Returns the amount of time that has passed since the most recent
     *         call to {@link #start()} or {@link #lap()}, or {@code 0} if
     *         the stopwatch has not been started.
     */
    public long stop() {
        if(mark == 0L) {
            return 0L;
        }

        long delta = timeReporter.nanos() - mark + intraLap;
        total += delta;
        laps++;
        mark = 0L;
        intraLap = 0L;
        return delta;
    }

    /**
     * Pauses the stopwatch, returning the amount of time that has passed since
     * the most recent call to {@link #start()} or {@link #lap()}.
     * <p>
     * The amount of time returned is held in reserve, and will be added to
     * the stopwatch total on the next call to {@link #lap()} or
     * {@link #start()}.
     * </p>
     *
     * @return Returns the amount of time that has passed since the most recent
     *         call to {@link #start()} or {@link #lap()}, or {@code 0} if
     *         the stopwatch has not been started.
     */
    public long pause() {
        if(mark == 0L) {
            return 0L;
        }

        long delta = timeReporter.nanos() - mark;
        intraLap += delta;
        mark = 0L;
        return delta;
    }

    /**
     * Resumes the stopwatch, if it was previously paused.
     */
    public void resume() {
        if(mark == 0L) {
            mark = timeReporter.nanos();
        }
    }

    /**
     * Gets the total number of laps recorded by this stopwatch.
     * @return The total number of laps recorded by this stopwatch.
     */
    public long getLaps() {
        return laps;
    }

    /**
     * Gets the total amount of time, in nanoseconds, recorded by this stopwatch
     * since the most recent call to {@link #start()}.
     * @return The total amount of time, in nanoseconds, recorded by this
     * stopwatch since the most recent call to {@link #start()}.
     */
    public long getTotal() {
        return total;
    }
}
