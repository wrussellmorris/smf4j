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
package org.smf4j.spring;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.smf4j.Accumulator;
import org.smf4j.Registrar;
import org.smf4j.RegistrarFactory;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class LookupFactoryBean implements FactoryBean<Accumulator> {

    private String node;
    private String accumulator;

    @Override
    public Class<?> getObjectType() {
        return Accumulator.class;
    }

    @Override
    public Accumulator getObject() throws Exception {
        RegistryNode regNode = getRegistrar().getNode(node);
        if(regNode == null) {
            throw new BeanCreationException(String.format(
                    "The node '%s' does not exist.", node));
        }
        Accumulator acc = regNode.getAccumulator(accumulator);
        if(acc == null) {
            throw new BeanCreationException(String.format(
                    "The node '%s' does not have an accumulator named '%s'.",
                    node, accumulator));
        }

        return acc;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public String getAccumulator() {
        return accumulator;
    }

    public void setAccumulator(String accumulator) {
        this.accumulator = accumulator;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public Registrar getRegistrar() {
        return RegistrarFactory.getRegistrar();
    }
}
