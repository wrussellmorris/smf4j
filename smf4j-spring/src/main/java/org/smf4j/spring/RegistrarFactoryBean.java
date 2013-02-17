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
import org.smf4j.Calculator;
import org.smf4j.Accumulator;
import org.smf4j.RegistrarFactory;
import org.smf4j.Registrar;
import org.smf4j.RegistryNode;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class RegistrarFactoryBean implements FactoryBean<Registrar>,
        ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private boolean initialized;
    private Registrar registrar;
    private List<RegistryNodeProxy> nodeProxies = Collections.emptyList();

    @Override
    public Class getObjectType() {
        return Registrar.class;
    }

    @Override
    public Registrar getObject() throws Exception {
        initialize();
        return getRegistrar();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Registrar getRegistrar() {
        if(registrar == null) {
            registrar = RegistrarFactory.getRegistrar();
        }
        return registrar;
    }

    public List<RegistryNodeProxy> getNodeProxies() {
        return nodeProxies;
    }

    public void setNodeProxies(List<RegistryNodeProxy> nodeProxies) {
        this.nodeProxies = nodeProxies;
    }

    protected void initialize() {
        if(initialized) {
            return;
        }

        initialized = true;
        Registrar r = RegistrarFactory.getRegistrar();
        for(RegistryNodeProxy nodeProxy : nodeProxies) {
            registerProxy(r, nodeProxy, "");
        }
    }

    protected void registerProxy(Registrar r, RegistryNodeProxy nodeProxy,
            String parentName) {
        String name = nodeProxy.getName();
        if(StringUtils.hasLength(parentName)) {
            name = parentName + "." + name;
        }

        for(RegistryProxy proxy : nodeProxy.getChildren()) {
            if(proxy instanceof RegistryNodeChildProxy) {
                RegistryNodeChildProxy childProxy =
                        (RegistryNodeChildProxy) proxy;
                String beanRef = childProxy.getChild();
                Object obj = applicationContext.getBean(beanRef);
                if(obj instanceof Accumulator) {
                    // An accumulator
                    RegistryNode node = r.getNode(name);
                    node.register(childProxy.getName(),(Accumulator)obj);
                } else if(obj instanceof Calculator) {
                    // A Caclulator
                    RegistryNode node = r.getNode(name);
                    node.register(childProxy.getName(),(Calculator)obj);
                } else {
                    throw new RuntimeException(String.format(
                            "[Node: %s, Child: %s] Node child "
                            + "proxy is neither template, Accumulator, nor "
                            + "Calculator.", name, childProxy.getName()));
                }
            } else if(proxy instanceof RegistryNodeProxy) {
                // An embedded node
                r.getNode(name);
                registerProxy(r, (RegistryNodeProxy)proxy, name);
            }
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void afterPropertiesSet() throws Exception {
        initialize();
    }
}
