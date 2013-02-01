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

import org.smf4j.Mutator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class MinCounter extends AbstractAccumulator {

    public MinCounter() {
        super(MinMutator.MUTATOR_FACTORY);
    }

    @Override
    public final long get() {
        boolean hasMutators = false;
        long value = Long.MAX_VALUE;
        for (Mutator mutator : mutatorRegistry) {
            value = Math.min(value, mutator.syncGet());
            hasMutators = true;
        }

        return hasMutators ? value : 0L;
    }
}
