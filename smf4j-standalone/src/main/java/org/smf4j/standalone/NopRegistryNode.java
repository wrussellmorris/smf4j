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
package org.smf4j.standalone;

import java.util.Collections;
import java.util.Map;
import org.smf4j.Accumulator;
import org.smf4j.Calculator;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
final class NopRegistryNode implements RegistryNode {
    public static final NopRegistryNode INSTANCE = new NopRegistryNode();
    private static final Map<String, Accumulator> EMPTY_ACC_MAP =
            Collections.emptyMap();
    private static final Map<String, Calculator> EMPTY_CALC_MAP =
            Collections.emptyMap();
    private static final Map<String, Object> EMPTY_SNAPSHOT_MAP =
            Collections.emptyMap();
    private static final Map<String, RegistryNode> EMPTY_CHILD_NODES_MAP =
            Collections.emptyMap();

    private NopRegistryNode() {
    }

    public String getName() {
        return "noop";
    }

    public boolean register(String name, Accumulator accumulator) {
        return false;
    }

    public boolean register(String name, Calculator calculator) {
        return false;
    }

    public boolean unregister(String name, Accumulator accumulator) {
        return true;
    }

    public boolean unregister(String name, Calculator calculator) {
        return true;
    }

    public Map<String, Accumulator> getAccumulators() {
        return EMPTY_ACC_MAP;
    }

    public Map<String, Calculator> getCalculators() {
        return EMPTY_CALC_MAP;
    }

    public Accumulator getAccumulator(String name) {
        return NopAccumulator.INSTANCE;
    }

    public Calculator getCalculator(String name) {
        return NopCalculator.INSTANCE;
    }

    public Map<String, Object> snapshot() {
        return EMPTY_SNAPSHOT_MAP;
    }

    public Map<String, RegistryNode> getChildNodes() {
        return EMPTY_CHILD_NODES_MAP;
    }

    public RegistryNode getChildNode(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isOn() {
        return false;
    }

    public void setOn(boolean on) {
    }

    public void clearOn() {
    }
}
