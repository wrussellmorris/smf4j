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
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class WindowedCounter implements Accumulator {

    private volatile boolean on;

    /**
     * The number of staging buckets we'll use by default.
     */
    private static final int STAGING_BUCKETS = 2;

    /**
     * The {@code IntervalStrategy} responsible for managing time intervals
     */
    private final IntervalStrategy strategy;

    private final WindowedIntervals windowedIntervals;

    /**
     * The {@link TimeReporter} responsible for giving the
     * current time, in nanos.
     */
    private final TimeReporter timeReporter;

    private String units;

    public WindowedCounter(int timeWindow, int intervals) {
        this(timeWindow, intervals, false,
             SystemNanosTimeReporter.INSTANCE);
    }

    public WindowedCounter(int timeWindow, int intervals,
            TimeReporter timeReporter) {
        this(timeWindow, intervals, false, timeReporter);
    }

    public WindowedCounter(int timeWindow, int intervals,
            boolean powersOfTwo) {
        this(timeWindow, intervals, powersOfTwo,
             SystemNanosTimeReporter.INSTANCE);
    }

    public WindowedCounter(int timeWindow, int intervals,
            boolean powersOfTwo, TimeReporter timeReporter) {
        this.timeReporter = timeReporter;
        if(timeReporter == null) {
            throw new NullPointerException("timeReporter");
        }

        if(powersOfTwo) {
            strategy = new PowersOfTwoIntervalStrategy(timeWindow,
                    intervals, STAGING_BUCKETS);
        } else {
            strategy = new SecondsIntervalStrategy(timeWindow, intervals,
                    STAGING_BUCKETS);
        }

        windowedIntervals = new WindowedIntervalsImpl(strategy, timeReporter);
    }

    public WindowedCounter(IntervalStrategy strategy,
            WindowedIntervals intervals) {
        this(strategy, intervals, SystemNanosTimeReporter.INSTANCE);
    }

    public WindowedCounter(IntervalStrategy strategy,
            WindowedIntervals intervals, TimeReporter timeReporter) {
        this.strategy = strategy;
        this.windowedIntervals = intervals;
        this.timeReporter = timeReporter;
    }

    public long getTimeWindow() {
        return strategy.timeWindowInNanos();
    }

    public int getIntervals() {
        return strategy.intervals();
    }

    @Override
    public Mutator getMutator() {
        if(!isOn()) {
            return Mutator.NOOP;
        }
        return windowedIntervals.getMutator();
    }

    @Override
    public long get() {
        if(!isOn()) {
            return 0L;
        }

        return windowedIntervals.get(timeReporter.nanos());
    }

    public long[] buckets() {
        return windowedIntervals.buckets(timeReporter.nanos());
    }

    @Override
    public boolean isOn() {
        return on;
    }

    @Override
    public void setOn(boolean on) {
        this.on = on;
    }

    @Override
    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}
