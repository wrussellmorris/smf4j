/*
 * Copyright 2013 Russell Morris (wrussellmorris@gmail.com).
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
package org.smf4j.to.csv;

import org.smf4j.Accumulator;
import org.smf4j.Mutator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class MockAccumulator implements Accumulator, Mutator {

    private long val;

    public MockAccumulator(long val) {
        this.val = val;
    }

    @Override
    public boolean isOn() {
        return false;
    }

    @Override
    public void setOn(boolean on) {
    }

    @Override
    public void put(long delta) {
        this.val = delta;
    }

    @Override
    public Mutator getMutator() {
        return this;
    }

    @Override
    public long get() {
        return val;
    }

    @Override
    public String getUnits() {
        return null;
    }

    @Override
    public long getTimeWindow() {
        return 0L;
    }

    @Override
    public int getIntervals() {
        return 0;
    }

    @Override
    public long combine(long other) {
        return 0L;
    }
}
