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
package org.smf4j.core.accumulator;

import org.smf4j.Accumulator;
import org.smf4j.Mutator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public abstract class AbstractAccumulator implements Accumulator {

    private volatile boolean on;
    protected final MutatorRegistry mutatorRegistry;

    protected AbstractAccumulator(final MutatorFactory mutatorFactory) {
        this.mutatorRegistry = new MutatorRegistry(mutatorFactory);
    }

    @Override
    public final boolean isOn() {
        return on;
    }

    @Override
    public final void setOn(boolean on) {
        this.on = on;
    }

    public final Mutator getMutator() {
        if(!isOn()) {
            return Mutator.NOOP;
        }

        return mutatorRegistry.get();
    }
}
