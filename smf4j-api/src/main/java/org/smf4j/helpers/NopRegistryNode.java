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

import java.util.Collections;
import java.util.Map;
import org.smf4j.Accumulator;
import org.smf4j.Calculator;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class NopRegistryNode implements RegistryNode {
    public static final RegistryNode INSTANCE = new NopRegistryNode();

    private static final Map<String, Accumulator> emptyAccMap =
            Collections.emptyMap();
    private static final Map<String, Calculator> emptyCalcMap =
            Collections.emptyMap();
    private static final Map<String, Object> emptySnapshot =
            Collections.emptyMap();
    private static final Map<String, RegistryNode> emptyNodesMap =
            Collections.emptyMap();

    private NopRegistryNode() {
    }

    public String getName() {
        return "nop";
    }

    public Accumulator register(String name, Accumulator accumulator) {
        return NopAccumulator.INSTANCE;
    }

    public Calculator register(String name, Calculator calculator) {
        return NopCalculator.INSTANCE;
    }

    public boolean unregister(String name, Accumulator accumulator) {
        return true;
    }

    public boolean unregister(String name, Calculator calculator) {
        return true;
    }

    public Map<String, Accumulator> getAccumulators() {
        return emptyAccMap;
    }

    public Map<String, Calculator> getCalculators() {
        return emptyCalcMap;
    }

    public Accumulator getAccumulator(String name) {
        return NopAccumulator.INSTANCE;
    }

    public Calculator getCalculator(String name) {
        return NopCalculator.INSTANCE;
    }

    public Map<String, Object> snapshot() {
        return emptySnapshot;
    }

    public Map<String, RegistryNode> getChildNodes() {
        return emptyNodesMap;
    }

    public RegistryNode getChildNode(String name) {
        return INSTANCE;
    }

    public boolean isOn() {
        return false;
    }

    public void setOn(boolean on) {
    }

    public void clearOn() {
    }

}
