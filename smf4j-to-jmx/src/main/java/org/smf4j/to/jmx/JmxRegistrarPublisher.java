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
package org.smf4j.to.jmx;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smf4j.Accumulator;
import org.smf4j.Calculator;
import org.smf4j.Registrar;
import org.smf4j.RegistrarFactory;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class JmxRegistrarPublisher {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Registrar registrar;
    private final AtomicBoolean published;
    private final MBeanServer server;
    private final Map<String, RegistryNodeDynamicMBean> registeredBeans;

    public JmxRegistrarPublisher() {
        this.published = new AtomicBoolean(false);
        this.server = ManagementFactory.getPlatformMBeanServer();
        this.registeredBeans =
                new ConcurrentHashMap<String, RegistryNodeDynamicMBean>();
        this.registrar = RegistrarFactory.getRegistrar();
    }

    public void publish() {
        if(published.compareAndSet(false, true)) {
            registerNodes(registrar.getRootNode());
        }
    }

    public void unpublish() {
        if(published.compareAndSet(true, false)) {
            unregisterNodes(registrar.getRootNode());
        }
    }

    void registerNodes(RegistryNode node) {
        String name = node.getName();
        if(registeredBeans.containsKey(name)) {
            log.error("Unable to locate a registered MBean for node '{}' "
                    + "because it is already recorded as registered.", name);
            return;
        }

        RegistryNodeDynamicMBean mb = new RegistryNodeDynamicMBean(node);
        try {
            server.registerMBean(mb, mb.getObjectName());
        } catch (InstanceAlreadyExistsException ex) {
            log.error(String.format("Unable to register node '%s' with "
                    + "ObjectName '%s' because the JMX server reports that it "
                    + "is already registered.", name, mb.getObjectName()), ex);
            return;
        } catch (MBeanRegistrationException ex) {
            log.error(String.format("Unable to register node '%s' with "
                    + "ObjectName '%s' because the JMX server reports that a "
                    + "general registration error has occurred.", name,
                    mb.getObjectName()), ex);
            return;
        } catch (NotCompliantMBeanException ex) {
            log.error(String.format("Unable to register node '%s' with "
                    + "ObjectName '%s' because the JMX server reports that "
                    + "it is not a compliant MBean.", name, mb.getObjectName()),
                    ex);
            return;
        }

        registeredBeans.put(node.getName(), mb);
        for(RegistryNode child : node.getChildNodes().values()) {
            registerNodes(child);
        }
    }

    void unregisterNodes(RegistryNode node) {
        String name = node.getName();
        RegistryNodeDynamicMBean mb = registeredBeans.get(name);
        if(mb == null) {
            log.error("Unable to locate a registered MBean for node '{}'",
                    name);
            return;
        }

        try {
            server.unregisterMBean(mb.getObjectName());
        } catch (InstanceNotFoundException ex) {
            log.error("Could not unregister node '{}' with ObjectName "
                    + "'{}' because the JMX reports that it cannot "
                    + "be found.", name, mb.getObjectName().toString());
        } catch (MBeanRegistrationException ex) {
            log.error(String.format(
                    "Could not unregister node '%s' with ObjectName '%s' "
                    + "due to a general error.", name, mb.getObjectName()),
                    ex);
        }

        for(RegistryNode child : node.getChildNodes().values()) {
            registerNodes(child);
        }
    }

    public void nodeAdded(RegistryNode registryNode) {
        registerNodes(registryNode);
    }

    public void nodeRemoved(RegistryNode registryNode) {
        unregisterNodes(registryNode);
    }

    public void accumulatorAdded(RegistryNode registryNode,
            Accumulator accumulator) {
        registerNodes(registryNode);
    }

    public void accumulatorRemoved(RegistryNode registryNode,
            Accumulator accumulator) {
        registerNodes(registryNode);
    }

    public void calculatorAdded(RegistryNode registryNode,
            Calculator calculator) {
        registerNodes(registryNode);
    }

    public void calculatorRemoved(RegistryNode registryNode,
            Calculator calculator) {
        registerNodes(registryNode);
    }
}
