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

import static org.junit.Assert.*;

import java.util.Map;
import org.junit.Test;
import org.smf4j.Mutator;
import org.smf4j.core.accumulator.MutatorFactory;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class TestNewMutatorRegistry {

    @Test
    public void get()
    throws Exception {
        MutatorFactory f = new MutatorFactory() {

            public Mutator createMutator() {
                return new Mutator() {
                    public void put(long delta) {
                    }
                    public long get() {
                        return 1;
                    }
                };
            }

            public Map<Object, Object> getMetadata() {
                return null;
            }

            public long getInitialValue() {
                return 0;
            }

            public long combine(long value, Mutator mutator) {
                return value + mutator.get();
            }
        };

        final NewMutatorRegistry r = new NewMutatorRegistry(f);
        r.get();

        Thread t = new Thread() {
            @Override
            public void run() {
                r.get();
            }
        };
        t.start();
        t.join();

        assertEquals(r.get(), r.get());
        assertEquals(2, r.getCombinedValue());
    }
}
