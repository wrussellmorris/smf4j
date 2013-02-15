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
import org.smf4j.core.accumulator.AbstractMutatorFactory;
import org.smf4j.core.accumulator.MutatorFactory;

/**
 *
 * @author rmorris
 */
public final class UnboundedAddMutator extends AbstractUnboundedMutator {

    public static final MutatorFactory MUTATOR_FACTORY = new Factory();

    public UnboundedAddMutator() {
        super(0L);
    }

    @Override
    public void put(long delta) {
        localValue += delta;
        value.lazySet(localValue);
    }

    @Override
    public long combine(long other) {
        return value.get() + other;
    }

    public static final class Factory extends AbstractMutatorFactory {
        public Mutator createMutator() {
            return new UnboundedAddMutator();
        }
    }
}
