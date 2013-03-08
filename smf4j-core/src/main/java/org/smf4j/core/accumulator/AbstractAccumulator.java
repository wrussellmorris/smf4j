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

import org.smf4j.Accumulator;

/**
 * {@code AbstractAccumulator} serves as a base for the {@link Accumulator}
 * implementations in {@code smf4j-core}.
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public abstract class AbstractAccumulator implements Accumulator {
    /**
     * A {@code boolean} that tracks our on/off state.
     */
    private volatile boolean on;

    /**
     * A potentially-{@code null} string describing our units.
     */
    private String units;

    public final boolean isOn() {
        return on;
    }

    public final void setOn(boolean on) {
        this.on = on;
    }

    public final String getUnits() {
        return units;
    }

    /**
     * Sets the {@code units} this {@link Accumulator} implementation reports.
     * @param units A string describing the units this {@link Accumulator}
     *              reports.
     */
    public final void setUnits(String units) {
        this.units = units;
    }
}
