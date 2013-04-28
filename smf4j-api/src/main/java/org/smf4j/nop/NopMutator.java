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
package org.smf4j.nop;

import org.smf4j.Mutator;

/**
 * {@code NopMutator} is a no-operation (nop) implementation of
 * {@link Mutator} that can be returned in instances where an actual
 * {@link Mutator} instance cannot be found, or is otherwise inappropriate.
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class NopMutator implements Mutator {

    /**
     * The static singleton {@code NopMutator}.
     */
    public static final Mutator INSTANCE = new NopMutator();

    /**
     * {@code NopMutator} is a static singleton.
     */
    private NopMutator() {
    }

    /**
     * Takes no action.
     * @param delta Ignored.
     */
    public void put(long delta) {
    }

    /**
     * Always returns {@code 0}.
     * @param other Ignored.
     * @return {@code 0}.
     */
    public long combine(long other) {
        return 0L;
    }

    /**
     * Always returns {@code 0}.
     * @return {@code 0}.
     */
    public long get() {
        return 0L;
    }
}
