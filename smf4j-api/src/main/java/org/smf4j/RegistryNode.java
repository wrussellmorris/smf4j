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
package org.smf4j;

import java.util.Map;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public interface RegistryNode {

    String getName();

    boolean register(String name, Accumulator accumulator);
    boolean register(String name, Calculator calculator);

    boolean unregister(String name, Accumulator accumulator);
    boolean unregister(String name, Calculator calculator);

    Map<String, Accumulator> getAccumulators();
    Map<String, Calculator> getCalculators();

    Accumulator getAccumulator(String name);
    Calculator getCalculator(String name);

    Map<String, Object> snapshot();

    Map<String, RegistryNode> getChildNodes();
    RegistryNode getChildNode(String name);

    boolean isOn();
    void setOn(boolean on);
    void clearOn();
}
