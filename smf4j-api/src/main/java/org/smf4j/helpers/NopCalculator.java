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
package org.smf4j.helpers;

import java.util.Map;
import org.smf4j.Accumulator;
import org.smf4j.Calculator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class NopCalculator implements Calculator {

    public static final NopCalculator INSTANCE = new NopCalculator();

    public Object calculate(Map<String, Long> values,
            Map<String, Accumulator> accumulators) {
        return null;
    }

    public String getUnits() {
        return null;
    }
}