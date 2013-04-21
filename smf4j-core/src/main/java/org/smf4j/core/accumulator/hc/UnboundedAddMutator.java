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
 * {@code UnboundedAddMutator} is an unbounded {@code Mutator} that reports the
 * sum of all values provided to it.
 * <p>
 * This version does not implement any read-modify-write semantics to its
 * internal value, and as such should only be used in concert with
 * {@link MutatorRegistry}.
 * </p>
 *
 * @see MutatorRegistry
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class UnboundedAddMutator extends AbstractUnboundedMutator {
    /**
     * The initial value of an {@code UnboundedAddMutator} - {@code 0}.
     */
    public static final long INITIAL_VALUE = 0L;

    /**
     * A {@link MutatorFactory} that can create instances of this mutator.
     */
    public static final MutatorFactory MUTATOR_FACTORY = new Factory();

    /**
     * Creates an instance of {@code UnboundedAddMutator}.
     */
    public UnboundedAddMutator() {
        super(INITIAL_VALUE);
    }

    public void put(long delta) {
        localValue += delta;
        value.lazySet(localValue);
    }

    /**
     * {@code UnboundedAddMutator.Factory} is an instance of
     * {@code MutatorFactory} that can create instances of
     * {@code UnboundedAddMutator}.
     */
    public static final class Factory extends AbstractMutatorFactory {
        public Mutator createMutator() {
            return new UnboundedAddMutator();
        }

        public long combine(long value, Mutator mutator) {
            return value + mutator.get();
        }

        public long getInitialValue() {
            return INITIAL_VALUE;
        }
    }
}
