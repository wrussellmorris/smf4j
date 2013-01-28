/*
 * Copyright 2012 rmorris.
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
import org.smf4j.Mutator;

/**
 *
 * @author rmorris
 */
public final class MaxMutator implements Mutator {

    static final MutatorFactory MUTATOR_FACTORY = new MutatorFactory() {
        public Mutator createMutator() {
            return new MaxMutator();
        }
    };

    private long localValue;
    private final AtomicLong value = new AtomicLong();

    public void add(final long delta) {
        if(delta > localValue) {
            localValue = delta;
            value.lazySet(delta);
        }
    }

    public long localGet() {
        return localValue;
    }

    public long syncGet() {
        return value.get();
    }
}
