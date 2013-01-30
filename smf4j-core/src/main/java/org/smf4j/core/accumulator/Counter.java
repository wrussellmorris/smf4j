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

import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class Counter extends AbstractAccumulator {

    private final AtomicLong scavengedValue;

    public Counter() {
        super(CounterMutator.MUTATOR_FACTORY);
        this.scavengedValue = new AtomicLong();
    }

    @Override
    public final long get() {
        long value = 0;
        for (MutatorRegistry.Registration registration : mutatorRegistry) {
            // Grab this mutator's current value
            long tmp = registration.getMutator().syncGet();
            if(registration.isDead() &&
                    mutatorRegistry.unregister(registration)) {
                // If this mutator was unregistered because it's owning thread
                // has gone away, add it to our scavenged value.
                scavengedValue.addAndGet(tmp);
            } else {
                // Otherwise, add it to our sum
                value += tmp;
            }
        }

        // Return the sum of the active registrations plus our scavenged value.
        return value + scavengedValue.get();
    }
}
