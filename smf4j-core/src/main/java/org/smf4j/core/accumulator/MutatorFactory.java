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
package org.smf4j.core.accumulator;

import java.util.Map;
import org.smf4j.Mutator;
import org.smf4j.core.accumulator.hc.HighContentionAccumulator;
import org.smf4j.core.accumulator.lc.LowContentionAccumulator;

/**
 * {@code MutatorFactory} implementations create instances of
 * {@link Mutator}s.
 *
 * @see HighContentionAccumulator
 * @see LowContentionAccumulator
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public interface MutatorFactory {

    /**
     * Creates a new {@link Mutator} instance.
     * @return A new {@link Mutator} instance.
     */
    Mutator createMutator();

    /**
     * Gets a {@link Map} containing metadata information describing all of the
     * {@link Mutator}s returned by {@link #createMutator()}.
     * @return A {@link Map} containing metadata information describing all of
     *         the {@link Mutator}s returned by {@link #createMutator()}.
     */
    Map<Object, Object> getMetadata();

    /**
     * Gets the initial value reported by {@code Mutator} instances created by
     * this factory.
     * @return The initial value reported by {@code Mutator} instances created
     *         by this factory.
     */
    long getInitialValue();

    /**
     * Combines the value reported by {@code mutator.get()} with an existing
     * value representing the combined values of other {@code Mutator}s
     * and/or the value of {@link #getInitialValue()}.
     *
     * @param value A value representing the initial value or a combination
     *              of the values reported by other {@code Mutator}s.
     * @param mutator The {@code Mutator} whose value is to be combined with
     *                {@code value}.
     *
     * @return Returns
     */
    long combine(long value, Mutator mutator);
}
