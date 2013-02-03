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

import org.smf4j.Mutator;
import org.smf4j.core.accumulator.AbstractAccumulator;
import org.smf4j.core.accumulator.MutatorFactory;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class HighConcurrencyAccumulator extends AbstractAccumulator {

    protected final MutatorRegistry mutatorRegistry;
    private final long timeWindow;
    private final int intervals;

    public HighConcurrencyAccumulator(MutatorFactory mutatorFactory) {
        this.mutatorRegistry = new MutatorRegistry(mutatorFactory);
        this.timeWindow = mutatorFactory.getTimeWindow();
        this.intervals = mutatorFactory.getIntervals();
    }

    public final Mutator getMutator() {
        if(!isOn()) {
            return Mutator.NOP;
        }
        return mutatorRegistry.get();
    }

    @Override
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

    public long getTimeWindow() {
        return timeWindow;
    }

    public int getIntervals() {
        return intervals;
    }
}
