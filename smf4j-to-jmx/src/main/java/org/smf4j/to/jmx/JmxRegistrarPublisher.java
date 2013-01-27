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
import java.util.concurrent.atomic.AtomicBoolean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import org.smf4j.Registrar;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class JmxRegistrarPublisher {

    private final Registrar registrar;
    private final AtomicBoolean registered;
    private final MBeanServer server;

    public JmxRegistrarPublisher(Registrar registrar) {
        this.registrar = registrar;
        this.registered = new AtomicBoolean(false);
        this.server = ManagementFactory.getPlatformMBeanServer();
    }

    public void publish() {
        while(!registered.get()) {
            if(registered.compareAndSet(false, true)) {
                registerNodes(registrar.getRootNode());
            }
        }
    }

    public void unpublish() {

    }

    void registerNodes(RegistryNode node) {
        RegistryNodeDynamicMBean mb = new RegistryNodeDynamicMBean(node);
        try {
            server.registerMBean(mb, mb.getObjectName());
        } catch (InstanceAlreadyExistsException ex) {
        } catch (MBeanRegistrationException ex) {
        } catch (NotCompliantMBeanException ex) {
        }

        for(RegistryNode child : node.getChildNodes().values()) {
            registerNodes(child);
        }
    }
}
