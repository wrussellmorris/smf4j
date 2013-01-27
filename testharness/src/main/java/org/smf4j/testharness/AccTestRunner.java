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
package org.smf4j.testharness;

import org.smf4j.Accumulator;
import org.smf4j.InvalidNodeNameException;
import org.smf4j.Registrar;
import org.smf4j.RegistrarFactory;
import org.smf4j.RegistryNode;
import org.smf4j.core.accumulator.WindowedCounter;
import org.smf4j.core.calculator.WindowNormalizer;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class AccTestRunner extends TestRunner {

    private final Accumulator accumulator;

    public AccTestRunner(long testIterations, String testName,
            Accumulator accumulator) {
        super(testIterations, testName);
        this.accumulator = accumulator;

        Registrar r = RegistrarFactory.getRegistrar();
        try {
            RegistryNode node = r.register(testName);
            node.register("accumulator", accumulator);
            if(accumulator instanceof WindowedCounter) {
                WindowNormalizer wn = new WindowNormalizer();
                wn.setFrequency(WindowNormalizer.Frequency.SECONDS);
                wn.setWindowedCounter("accumulator");
                node.register("normalized", wn);
            }
        } catch(InvalidNodeNameException e) {
            e.printStackTrace();
        }
    }

    @Override
    public final void doRun() {
        accumulator.add(1);
    }

}
