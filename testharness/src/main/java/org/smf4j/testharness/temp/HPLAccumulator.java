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
package org.smf4j.testharness.temp;

import java.util.Collections;
import java.util.Map;
import org.cliffc.high_scale_lib.Counter;
import org.smf4j.Accumulator;
import org.smf4j.Mutator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class HPLAccumulator implements Accumulator, Mutator {
    private Counter counter = new Counter();

    public boolean isOn() {
        return true;
    }

    public void setOn(boolean on) {
    }

    public Mutator getMutator() {
        return this;
    }

    public long get() {
        return counter.get();
    }

    public String getUnits() {
        return null;
    }

    public Map<Object, Object> getMetadata() {
        return Collections.emptyMap();
    }

    public void put(long delta) {
        counter.add(delta);
    }

}
