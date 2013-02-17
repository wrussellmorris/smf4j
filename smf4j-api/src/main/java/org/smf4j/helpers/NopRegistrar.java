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
import java.util.List;
import org.smf4j.Accumulator;
import org.smf4j.Calculator;
import org.smf4j.Registrar;
import org.smf4j.RegistrarFactory;
import org.smf4j.RegistryNode;

/**
 * {@code NopRegistrar} is a no-operation (nop) implementation of
 * {@link Registrar} that can be returned in instances where an actual
 * {@link Registrar} instance cannot be found, or is otherwise inappropriate.
 * <p>
 * This is returned by {@link RegistrarFactory} when a binding failure occurs.
 * </p>
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class NopRegistrar implements Registrar {

    /**
     * The static singleton instance of {@code NopRegistrar}.
     */
    public static final Registrar INSTANCE = new NopRegistrar();

    /**
     * The empty list returned when {@code match} is called.
     */
    private static final List<GlobMatch> empty = Collections.emptyList();

    /**
     * {@code NopRegistrar} is a static singleton.
     */
    private NopRegistrar() {
    }

    /**
     * Always returns {@link NopRegistryNode#INSTANCE}.
     * @param fullNodeName Ignored.
     * @return {@link NopRegistryNode#INSTANCE}.
     */
    public RegistryNode getNode(String fullNodeName) {
        return NopRegistryNode.INSTANCE;
    }

    /**
     * Always returns {@link NopRegistryNode#INSTANCE}.
     * @return {@link NopRegistryNode#INSTANCE}.
     */
    public RegistryNode getRootNode() {
        return NopRegistryNode.INSTANCE;
    }

    /**
     * Always returns {@link Collections#emptyList()}.
     * @param globPattern Ignored.
     * @return {@link Collections#emptyList()}.
     */
    public Iterable<GlobMatch> match(String globPattern) {
        return empty;
    }

    /**
     * Takes to action.
     * @param fullNodeName Ignored.
     * @param on Ignored.
     */
    public void setOn(String fullNodeName, boolean on) {
    }

    /**
     * Takes no action.
     * @param fullNodeName Ignored.
     */
    public void clearOn(String fullNodeName) {
    }

    /**
     * Always returns {@link NopAccumulator#INSTANCE}.
     * @param path Ignored.
     * @return {@link NopAccumulator#INSTANCE}.
     */
    public Accumulator getAccumulator(String path) {
        return NopAccumulator.INSTANCE;
    }

    /**
     * Always returns {@link NopCalculator#INSTANCE}.
     * @param path Ignored.
     * @return {@link NopCalculator#INSTANCE}.
     */
    public Calculator getCalculator(String path) {
        return NopCalculator.INSTANCE;
    }

}
