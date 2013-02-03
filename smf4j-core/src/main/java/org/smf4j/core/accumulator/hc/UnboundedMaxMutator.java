/*
 * Copyright 2012 rmorris.
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
package org.smf4j.core.accumulator.hc;

import org.smf4j.Mutator;
import org.smf4j.core.accumulator.MutatorFactory;

/**
 *
 * @author rmorris
 */
public final class UnboundedMaxMutator extends AbstractUnboundedMutator {

    public static final MutatorFactory MUTATOR_FACTORY = new Factory();

    public UnboundedMaxMutator() {
        super(Long.MIN_VALUE);
    }

    public void put(long delta) {
        if(delta > localValue) {
            localValue = delta;
            value.lazySet(delta);
        }
    }

    public long combine(long other) {
        long val = value.get();
        return val >= other ? val : other;
    }

    public static final class Factory implements MutatorFactory{
        public Mutator createMutator() {
            return new UnboundedMaxMutator();
        }

        public long getTimeWindow() {
            return 0L;
        }

        public int getIntervals() {
            return 0;
        }
    };
}
