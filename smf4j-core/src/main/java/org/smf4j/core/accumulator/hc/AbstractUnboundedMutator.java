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
package org.smf4j.core.accumulator.hc;

import java.util.concurrent.atomic.AtomicLong;
import org.smf4j.Mutator;

/**
 * {@code AbstractUnboundedMutator} serves as a base class for high-contention,
 * non-windowed {@link Mutator}s that are designed to be written to by
 * <strong>exactly</strong> one thread at a time, but safely readable by any
 * number of threads.
 *
 * @see HighContentionAccumulator
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public abstract class AbstractUnboundedMutator implements Mutator {
    /**
     * The 'local' value of this accumulator, which can be safely modified
     * without worrying about read-modify-write locking semantics.
     */
    protected long localValue;

    /**
     * A mirror of the local value that is less likely to be read out-of-date
     * than our non-volatile {@code localValue}.
     */
    protected final AtomicLong value;

    /**
     * Protected constructor of {@code AbstractUnboundedMutator} that subclasses
     * use to set their initial value.
     * @param initialValue The initial value, reported when no writes have been
     *                     made.
     * <p>
     * {@code initialValue} should be chosen in such a manner that it serves as
     * an identity transformation when passed to {@link #combine(long)}.  For
     * example, {@link UnboundedAddMutator} uses {@code 0}, and
     * {@link UnboundedMaxMutator} uses {@link Long#MIN_VALUE}.
     * </p>
     */
    protected AbstractUnboundedMutator(long initialValue) {
        localValue = initialValue;
        value = new AtomicLong(initialValue);
    }

    public final long get() {
        return value.get();
    }
}
