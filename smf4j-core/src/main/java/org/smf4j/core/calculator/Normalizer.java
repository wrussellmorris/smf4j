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
package org.smf4j.core.calculator;

import java.util.Map;
import org.smf4j.Accumulator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class Normalizer extends AbstractCalculator {

    private String accumulator;
    private Frequency frequency = Frequency.SECONDS;

    @Override
    public Double calculate(Map<String, Long> values,
        Map<String, Accumulator> accumulators) {

        Long val = values.get(getAccumulator());
        Accumulator a = accumulators.get(getAccumulator());
        if(a == null || val == null) {
            return 0.0d;
        }

        double window = a.getTimeWindow();
        if(window <= 0.0d) {
            return val.doubleValue();
        }
        return (frequency.getNanos() / window) * val;
    }

    public String getAccumulator() {
        return accumulator;
    }

    public void setAccumulator(String accumulator) {
        this.accumulator = accumulator;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        if(frequency != null) {
            this.frequency = frequency;
        }
    }

    @Override
    public String getUnits() {
        return frequency.getUnits();
    }
}
