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
 * {@code UnboundedMinMutator} is an unbounded {@code Mutator} that reports
 * the smallest value it has been given.
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
public final class UnboundedMinMutator extends AbstractUnboundedMutator {
    /**
     * The initial value of an {@code UnboundedMaxMutator} -
     * {@code Long.MAX_VALUE}.
     */
    public static final long INITIAL_VALUE = Long.MAX_VALUE;

    /**
     * A {@link MutatorFactory} that can create instances of this mutator.
     */
    public static final MutatorFactory MUTATOR_FACTORY = new Factory();

    /**
     * Creates an instance of {@code UnboundedMinMutator}.
     */
    public UnboundedMinMutator() {
        super(INITIAL_VALUE);
    }

    @Override
    public void put(final long delta) {
        if(delta < localValue) {
            localValue = delta;
            value.lazySet(delta);
        }
    }

    /**
     * {@code UnboundedMinMutator.Factory} is an instance of
     * {@code MutatorFactory} that can create instances of
     * {@code UnboundedMinMutator}.
     */
    public static final class Factory extends AbstractMutatorFactory {
        public Mutator createMutator() {
            return new UnboundedMinMutator();
        }

        public long getInitialValue() {
            return INITIAL_VALUE;
        }

        public long combine(long value, Mutator mutator) {
            long other = mutator.get();
            return value <= other ? value : other;
        }
    };
}
