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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.smf4j.Accumulator;
import org.smf4j.Mutator;
import org.smf4j.core.accumulator.IntervalStrategy;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class MockAccumulator implements Accumulator, Mutator {

    long value;
    boolean on;
    Map<Object, Object> metadata = new HashMap<Object, Object>();

    public long get() {
        return value;
    }

    public void set(long value) {
        this.value = value;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public void put(long delta) {
        this.value = delta;
    }

    public Mutator getMutator() {
        return this;
    }

    public String getUnits() {
        return null;
    }

    public long combine(long other) {
        return other;
    }

    public void setTimeWindow(long timeWindow) {
        metadata.put(IntervalStrategy.METADATA_TIME_WINDOW, timeWindow);
    }

    public void setIntervals(int intervals) {
        metadata.put(IntervalStrategy.METADATA_INTERVALS, intervals);
    }

    public Map<Object, Object> getMetadata() {
        return metadata;
    }
}
