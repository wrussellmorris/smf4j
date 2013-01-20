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

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.FactoryBean;
import org.smf4j.Calculator;
import org.smf4j.Accumulator;
import org.smf4j.RegistrarFactory;
import org.smf4j.InvalidNodeNameException;
import org.smf4j.Registrar;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class RegistrarFactoryBean implements FactoryBean<Registrar> {

    private boolean initialized;
    private List<RegistryNodeProxy> nodeProxies = Collections.emptyList();

    @Override
    public Class<?> getObjectType() {
        return Registrar.class;
    }

    @Override
    public Registrar getObject() throws Exception {
        if(!initialized) {
            initialize();
        }
        return getRegistrar();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Registrar getRegistrar() {
        return RegistrarFactory.getRegistrar();
    }

    public List<RegistryNodeProxy> getNodeProxies() {
        return nodeProxies;
    }

    public void setNodeProxies(List<RegistryNodeProxy> nodeProxies) {
        this.nodeProxies = nodeProxies;
    }

    protected void initialize() {
        initialized = true;
        Registrar r = getRegistrar();

        for(RegistryNodeProxy nodeProxy : nodeProxies) {
            RegistryNode node = null;
            try {
                node = r.register(nodeProxy.getName());
            } catch (InvalidNodeNameException ex) {
                throw new RuntimeException(String.format(
                        "[Node: %s] Invalid node name.",
                        nodeProxy.getName()), ex);
            }

            for(RegistryNodeChildProxy childProxy : nodeProxy.getChildren()) {
                Object obj = childProxy.getChild();

                if(obj instanceof Accumulator) {
                    node.register(childProxy.getName(),(Accumulator)obj);
                } else if(obj instanceof Calculator) {
                    node.register(childProxy.getName(),(Calculator)obj);
                } else {
                    throw new RuntimeException(String.format(
                            "[Node: %s, Child: %s] Node child "
                            + "proxy is neither Accumulator nor Calculation.",
                            node.getName(),
                            childProxy.getName()));
                }
            }
        }

        r.initializationComplete();
    }
}
