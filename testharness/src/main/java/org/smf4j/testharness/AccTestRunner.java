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

import java.util.Map;
import org.smf4j.Accumulator;
import org.smf4j.Mutator;
import org.smf4j.Registrar;
import org.smf4j.RegistrarFactory;
import org.smf4j.RegistryNode;
import org.smf4j.core.accumulator.IntervalStrategy;
import org.smf4j.core.calculator.Frequency;
import org.smf4j.core.calculator.Normalizer;

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
        RegistryNode node = r.getNode(testName);
        node.register("accumulator", accumulator);
        Map<Object, Object> metadata = accumulator.getMetadata();
        if(metadata.containsKey(IntervalStrategy.METADATA_TIME_WINDOW)) {
            Long timeWindow = (Long)metadata.get(
                    IntervalStrategy.METADATA_TIME_WINDOW);
            if(timeWindow > 0) {
                Normalizer wn = new Normalizer();
                wn.setFrequency(Frequency.SECONDS);
                wn.setAccumulator("accumulator");
                node.register("normalized", wn);
            }
        }
    }

    @Override
    public final void run() {
        long localCount = 0;
        Mutator mutator = accumulator.getMutator();
        long start = System.currentTimeMillis();
        while(localCount < testIterations) {
            //mutator.put(1L);
            accumulator.getMutator().put(1L);
            localCount++;
        }
        durations.offer(System.currentTimeMillis() - start);
    }

    public final Accumulator getAccumulator() {
        return accumulator;
    }
}
