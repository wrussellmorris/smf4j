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

import java.util.Collections;
import java.util.Map;
import org.smf4j.Accumulator;
import org.smf4j.Mutator;

/**
 * {@code NopAccumulator} is a no-operation (nop) implementation of
 * {@link Accumulator} that can be returned in instances where an actual
 * {@link Accumulator} instance cannot be found, or is otherwise inappropriate.
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class NopAccumulator implements Accumulator {

    /**
     * The static singleton {@code NopAccumulator}.
     */
    public static final Accumulator INSTANCE = new NopAccumulator();

    /**
     * {@code NopAccumulator} is a static singleton.
     */
    private NopAccumulator() {
    }

    /**
     * Always returns {@link NopMutator#INSTANCE}.
     * @return {@link NopMutator#INSTANCE}.
     */
    public Mutator getMutator() {
        return NopMutator.INSTANCE;
    }

    /**
     * Always returns {@code 0}.
     * @return {@code 0}.
     */
    public long get() {
        return 0L;
    }

    /**
     * Always returns {@code false}.
     * @return {@code false}
     */
    public boolean isOn() {
        return false;
    }

    /**
     * Takes no action.
     * @param on Ignored.
     */
    public void setOn(boolean on) {
    }

    /**
     * Always returns {@code null}.
     * @return {@code null}.
     */
    public String getUnits() {
        return null;
    }

    /**
     * Always returns {@link Collections#emptyMap()}
     * @return {@link Collections#emptyMap()}
     */
    public Map<Object, Object> getMetadata() {
        return Collections.emptyMap();
    }
}
