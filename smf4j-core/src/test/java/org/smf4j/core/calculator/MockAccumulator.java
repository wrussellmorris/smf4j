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
package org.smf4j.core.calculator;

import org.smf4j.Accumulator;
import org.smf4j.Mutator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class MockAccumulator implements Accumulator, Mutator {

    long value;
    boolean on;
    long timeWindow = 0L;
    int intervals = 0;

    @Override
    public long get() {
        return value;
    }

    public void set(long value) {
        this.value = value;
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
    public void put(long delta) {
        this.value = delta;
    }

    @Override
    public Mutator getMutator() {
        return this;
    }

    @Override
    public String getUnits() {
        return null;
    }

    @Override
    public long combine(long other) {
        return other;
    }

    @Override
    public long getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(long timeWindow) {
        this.timeWindow = timeWindow;
    }

    @Override
    public int getIntervals() {
        return intervals;
    }

    public void setIntervals(int intervals) {
        this.intervals = intervals;
    }

}
