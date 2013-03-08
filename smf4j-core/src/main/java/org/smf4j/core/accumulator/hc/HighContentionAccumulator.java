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
package org.smf4j.core.accumulator.hc;

import java.util.Map;
import org.smf4j.Accumulator;
import org.smf4j.Mutator;
import org.smf4j.core.accumulator.AbstractAccumulator;
import org.smf4j.core.accumulator.MutatorFactory;
import org.smf4j.helpers.NopMutator;

/**
 * {@code HighContentionAccumulator} is an {@link Accumulator} implementation
 * that is built to be read from and written to in high contention by multiple
 * threads.
 * <p>
 * {@code HighContentionAccumulator} uses a {@link MutatorRegistry} internally
 * to schedule individual {@link Mutator} instances in such a way as to
 * guarantee that every {@link Mutator} returned by {@link #getMutator()} is
 * bound to the calling thread ({@code Thread.currentThread()} until
 * such time as {@code Thread.currentThrad().isAlive()} return {@code false}. In
 * combination with the high-contention {@link AbstractUnboundedMutator} or
 * {@link AbstractWindowedMutator}, this guarantees that calls to
 * {@link Mutator#put(long)} will not require any read-modify-write
 * synchronization semantics in order to accurately update the internal state
 * of the {@code Mutator}.
 * </p>
 * <p>
 * Calls to {@link #getMutator()} may cause the allocation of a new instance of
 * a {@code Mutator} (via the associated {@link MutatorFactory}), but only if
 * the instance's {@link MutatorRegistry} does not have an existing instance
 * whose associated {@code Thread.isAlive() == false}.
 * </p>
 * <p>
 * To create an instance of {@code HighContentionAccumulator}, you must supply
 * an instance of a {@link MutatorFactory} implementation that provides the
 * {@link Mutator} implementation.
 * </p>
 *
 * @see UnboundedMaxMutator
 * @see UnboundedMaxMutator.Factory
 * @see WindowedAddMutator
 * @see WindowedAddMutator.Factory
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class HighContentionAccumulator extends AbstractAccumulator {

    /**
     * The {@link MutatorRegistry} used to schedule {@code Mutator} instances.
     */
    private final MutatorRegistry mutatorRegistry;

    /**
     * The {@link Map} that contains metadata describing for this instance.
     */
    private final Map<Object, Object> metadata;

    /**
     * Creates a new {@code HighContentionAccumulator} that gets new
     * {@code Mutator} instances from {@code mutatorFactory}.
     * @param mutatorFactory The factory which produces new instances of
     *                       {@code Mutator} when necessary.
     */
    public HighContentionAccumulator(MutatorFactory mutatorFactory) {
        this.mutatorRegistry = new MutatorRegistry(mutatorFactory);
        this.metadata = mutatorFactory.getMetadata();
    }

    /**
     * Gets a {@link Mutator} instance that can modify the state of this
     * {@code HighContentionAccumulator}, and binds the returned instance to
     * the current thread for the lifetime of the current thread.
     * <p>
     * If the caller will use the returned {@code Mutator} instance multiple
     * times as part of the current thread, it is best to store the returned
     * instance in a local variable instead of repeatedly calling this method
     * to obtain an instance.
     * </p>
     * <pre>
     * private HighContentionAccumulator itemsProcessed;
     * private HighContentionAccumulator bytesProcessed;
     *
     * public void processItems(Item[] items) {
     *     // ...
     *     Mutator itemsMutator = itemsProcessed.getMutator();
     *     Mutator bytesMutator = bytesProcessed.getMutator();
     *     for(int i=0; i&lt;items.length; i++) {
     *         // ... process items[i]
     *         itemsMutator.put(1);
     *         bytesMutator.put(items[i].numBytes());
     *     }
     *     // ...
     * }
     * </pre>
     * <p>
     * While the returned instances of {@link Mutator} are pooled, it is
     * guaranteed that the instance returned by this method will not be
     * given to any thread other than {@code Thread.currentThread()} for as
     * long as {@code Thread.currentThread().isAlive() == true}.
     * </p>
     * @return An instance of {@link Mutator} that is bound to
     *         {@code Thread.currentThread()} for as long as
     *         {@code Thread.currentThread().isAlive() == true}.
     */
    public final Mutator getMutator() {
        if(!isOn()) {
            return NopMutator.INSTANCE;
        }
        return mutatorRegistry.get();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The value is derived by combining all of the values recorded by all
     * {@code Mutator}s this instance has provided via {@link #getMutator()}.
     * The semantics for this combination are given by the underlying
     * {@code Mutator} implementation provided by this instance's
     * {@link MutatorFactory}.
     * </p>
     * @return The combination of the values in all of this instance's
     *         {@code Mutator}s.
     */
    public final long get() {
        long value = 0L;

        // Sum up all of the active mutators
        boolean seenOneMutator = false;
        for (Mutator mutator : mutatorRegistry) {
            if(seenOneMutator) {
                value = mutator.combine(value);
            } else {
                value = mutator.get();
                seenOneMutator = true;
            }
        }

        return value;
    }

    public Map<Object, Object> getMetadata() {
        return metadata;
    }
}
