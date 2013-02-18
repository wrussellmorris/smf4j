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
 * {@code NopRegistryNode} is a no-operation (nop) implementation of
 * {@link RegistryNode} that can be returned in instances where an actual
 * {@link RegistryNode} instance cannot be found, or is otherwise inappropriate.
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class NopRegistryNode implements RegistryNode {
    /**
     * The static singleton {@code NopRegistryNode}.
     */
    public static final RegistryNode INSTANCE = new NopRegistryNode();

    private static final Map<String, Accumulator> emptyAccMap =
            Collections.emptyMap();
    private static final Map<String, Calculator> emptyCalcMap =
            Collections.emptyMap();
    private static final Map<String, Object> emptySnapshot =
            Collections.emptyMap();
    private static final Map<String, RegistryNode> emptyNodesMap =
            Collections.emptyMap();

    /**
     * {@code NopRegistryNode} is a static singleton.
     */
    private NopRegistryNode() {
    }

    /**
     * Always returns {@code ""}.
     * @return {@code ""}.
     */
    public String getName() {
        return "";
    }

    /**
     * Takes no action and returns {@link NopAccumulator#INSTANCE}.
     * @param name Ignored.
     * @param accumulator Ignored.
     * @return {@link NopAccumulator#INSTANCE}.
     */
    public Accumulator register(String name, Accumulator accumulator) {
        return NopAccumulator.INSTANCE;
    }

    /**
     * Takes no action and returns {@link NopCalculator#INSTANCE}.
     * @param name Ignored.
     * @param calculator Ignored.
     * @return {@link NopCalculator#INSTANCE}.
     */
    public Calculator register(String name, Calculator calculator) {
        return NopCalculator.INSTANCE;
    }

    /**
     * Takes no action and returns {@code false}.
     * @param name Ignored.
     * @param accumulator Ignored.
     * @return {@code false}.
     */
    public boolean unregister(String name, Accumulator accumulator) {
        return true;
    }

    /**
     * Takes no action and returns {@code false}.
     * @param name Ignored.
     * @param calculator Ignored.
     * @return {@code false}.
     */
    public boolean unregister(String name, Calculator calculator) {
        return true;
    }

    /**
     * Always returns {@link Collections#emptyMap()}.
     * @return {@link Collections#emptyMap()}.
     */
    public Map<String, Accumulator> getAccumulators() {
        return emptyAccMap;
    }

    /**
     * Always returns {@link Collections#emptyMap()}.
     * @return {@link Collections#emptyMap()}.
     */
    public Map<String, Calculator> getCalculators() {
        return emptyCalcMap;
    }

    /**
     * Always returns {@link NopAccumulator#INSTANCE}.
     * @param name Ignored.
     * @return {@link NopAccumulator#INSTANCE}.
     */
    public Accumulator getAccumulator(String name) {
        return NopAccumulator.INSTANCE;
    }

    /**
     * Always returns {@link NopCalculator#INSTANCE}.
     * @param name Ignored.
     * @return {@link NopCalculator#INSTANCE}.
     */
    public Calculator getCalculator(String name) {
        return NopCalculator.INSTANCE;
    }

    /**
     * Always returns {@link Collections#emptyMap()}.
     * @return {@link Collections#emptyMap()}.
     */
    public Map<String, Object> snapshot() {
        return emptySnapshot;
    }

    /**
     * Always returns {@link Collections#emptyMap()}.
     * @return {@link Collections#emptyMap()}.
     */
    public Map<String, RegistryNode> getChildNodes() {
        return emptyNodesMap;
    }

    /**
     * Always returns {@link NopRegistryNode#INSTANCE}.
     * @param name Ignored.
     * @return {@link NopRegistryNode#INSTANCE}.
     */
    public RegistryNode getChildNode(String name) {
        return INSTANCE;
    }

    /**
     * Always returns {@code false}.
     * @return {@code false}.
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
     * Takes no action.
     */
    public void clearOn() {
    }
}
