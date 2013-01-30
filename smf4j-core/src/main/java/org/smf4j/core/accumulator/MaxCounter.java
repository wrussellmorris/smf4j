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

import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class MaxCounter extends AbstractAccumulator {

    private final AtomicLong scavengedValue;

    public MaxCounter() {
        super(MaxMutator.MUTATOR_FACTORY);
        this.scavengedValue = new AtomicLong(Long.MIN_VALUE);
    }

    @Override
    public final long get() {
        long value = Long.MIN_VALUE;
        for (MutatorRegistry.Registration registration : mutatorRegistry) {
            // Grab this mutator's current value
            long tmp = registration.getMutator().syncGet();
            if(registration.isDead() &&
                    mutatorRegistry.unregister(registration)) {
                // If this mutator was unregistered because it's owning thread
                // has gone away, combine it with our scavenged value
                while(true) {
                    long cur = scavengedValue.get();
                    if(tmp > cur) {
                        // Try to set the new max scavenged value
                        if(scavengedValue.compareAndSet(cur, tmp)) {
                            break;
                        }
                    } else {
                        // tmp <= cur, so no need to attempt to set
                        break;
                    }
                }
            } else {
                // Otherwise, combine it with the other values we've gathered
                // from the active threads
                value = Math.max(value, tmp);
            }
        }

        // Return the sum of the active registrations plus our scavenged value.
        return Math.max(value, scavengedValue.get());
    }
}
