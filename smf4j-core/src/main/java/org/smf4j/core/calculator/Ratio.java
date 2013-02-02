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
public class Ratio extends AbstractCalculator {

    private String numerator;
    private String denominator;

    @Override
    public Double calculate(Map<String, Long> values,
        Map<String, Accumulator> accumulators) {

        Long num = values.get(numerator);
        Long den = values.get(denominator);
        if(num == null) {
            return 0.0d;
        }
        if(den == null || den == 0L) {
            return Double.NaN;
        }

        return (double)num / (double)den;
    }

    public String getDenominator() {
        return denominator;
    }

    public void setDenominator(String denominator) {
        this.denominator = denominator;
    }

    public String getNumerator() {
        return numerator;
    }

    public void setNumerator(String numerator) {
        this.numerator = numerator;
    }
}
