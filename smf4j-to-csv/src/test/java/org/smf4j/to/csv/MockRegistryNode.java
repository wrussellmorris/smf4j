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
package org.smf4j.to.csv;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.smf4j.Accumulator;
import org.smf4j.Calculator;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class MockRegistryNode implements RegistryNode {

    private String name;
    private Map<String, Accumulator> accs = new HashMap<String, Accumulator>();
    private Map<String, Calculator> calcs = new HashMap<String, Calculator>();

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean register(String name, Accumulator accumulator) {
        if(!accs.containsKey(name)) {
            accs.put(name, accumulator);
            return true;
        }
        return false;
    }

    @Override
    public boolean register(String name, Calculator calculator) {
        if(!calcs.containsKey(name)) {
            calcs.put(name, calculator);
            return true;
        }
        return false;
    }

    @Override
    public boolean unregister(String name, Accumulator accumulator) {
        return accs.remove(name) != null;
    }

    @Override
    public boolean unregister(String name, Calculator calculator) {
        return calcs.remove(name) != null;
    }

    @Override
    public Map<String, Accumulator> getAccumulators() {
        return accs;
    }

    @Override
    public Map<String, Calculator> getCalculators() {
        return calcs;
    }

    @Override
    public Accumulator getAccumulator(String name) {
        return accs.get(name);
    }

    @Override
    public Calculator getCalculator(String name) {
        return calcs.get(name);
    }

    @Override
    public Map<String, Object> snapshot() {
        // Snapshot the values for all of the accumulators
        Map<String, Long> vals = new HashMap<String, Long>();
        for(Map.Entry<String, Accumulator> entry : accs.entrySet()) {
            vals.put(entry.getKey(), entry.getValue().getValue());
        }

        // Run calculators with accumulator values as input
        Map<String, Object> results = new HashMap<String, Object>();
        for(Map.Entry<String, Calculator> entry : calcs.entrySet()) {
            Object o = null;
            try {
                o = entry.getValue().calculate(vals, accs);
            } catch(Throwable t) {
                // TODO: Log
            }
            results.put(entry.getKey(), o);
        }

        // Stuff all recorded accumulator values into the results as well.
        results.putAll(vals);

        return results;
    }

    @Override
    public Map<String, RegistryNode> getChildNodes() {
        return Collections.emptyMap();
    }

    @Override
    public RegistryNode getChildNode(String name) {
        return null;
    }

    @Override
    public boolean isOn() {
        return false;
    }

    @Override
    public void setOn(boolean on) {
    }

    @Override
    public void clearOn() {
    }
}
