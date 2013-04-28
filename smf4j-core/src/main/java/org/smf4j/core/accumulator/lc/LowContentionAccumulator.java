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
package org.smf4j.core.accumulator.lc;

import java.util.Map;
import org.smf4j.Mutator;
import org.smf4j.core.accumulator.AbstractAccumulator;
import org.smf4j.core.accumulator.MutatorFactory;
import org.smf4j.nop.NopMutator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class LowContentionAccumulator extends AbstractAccumulator {

    private final MutatorFactory mutatorFactory;
    private final Mutator mutator;

    public LowContentionAccumulator(MutatorFactory mutatorFactory) {
        this.mutatorFactory = mutatorFactory;
        this.mutator = mutatorFactory.createMutator();
    }

    public Mutator getMutator() {
        if(!isOn()) {
            return NopMutator.INSTANCE;
        }
        return mutator;
    }

    public long get() {
        return mutator.get();
    }

    public Map<Object, Object> getMetadata() {
        return mutatorFactory.getMetadata();
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public void put(long delta) {
        if(on) {
            getMutator().put(delta);
        }
    }
}
