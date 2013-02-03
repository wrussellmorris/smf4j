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
package org.smf4j.core.accumulator.lc;

import org.smf4j.Mutator;
import org.smf4j.core.accumulator.MutatorFactory;

/**
 *
 * @author rmorris
 */
public final class UnboundedMinMutator extends AbstractUnboundedMutator {

    public static final MutatorFactory MUTATOR_FACTORY = new Factory();

    public UnboundedMinMutator() {
        super(Long.MAX_VALUE);
    }

    @Override
    public void put(final long delta) {
        while(true) {
            long val = value.get();
            if(delta < val) {
                if(value.compareAndSet(val, delta)) {
                    break;
                }
            } else {
                break;
            }
        }
    }

    @Override
    public long combine(long other) {
        long val = value.get();
        return val <= other ? val : other;
    }

    public static final class Factory implements MutatorFactory {
        @Override
        public Mutator createMutator() {
            return new UnboundedMinMutator();
        }

        @Override
        public long getTimeWindow() {
            return 0L;
        }

        @Override
        public int getIntervals() {
            return 0;
        }
    };
}
