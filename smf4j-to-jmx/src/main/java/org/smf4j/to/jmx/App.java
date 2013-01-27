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

import org.smf4j.Registrar;
import org.smf4j.RegistrarFactory;
import org.smf4j.RegistryNode;
import org.smf4j.core.accumulator.Counter;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class App {

    public static void main(String[] args) throws Exception {
        Registrar r = RegistrarFactory.getRegistrar();
        RegistryNode a = r.register("a");
        a.register("a_acc", new Counter());

        RegistryNode a_b = r.register("a.b");
        a_b.register("a_b_acc", new Counter());

        RegistryNode a_b_c = r.register("a.b.c");
        a_b_c.register("a_b_c_acc", new Counter());

        RegistryNode _1 = r.register("1");
        _1.register("1_acc", new Counter());

        RegistryNode _1_2 = r.register("1.2");
        _1_2.register("1_2_acc", new Counter());

        JmxRegistrarPublisher jmx = new JmxRegistrarPublisher(r);
        jmx.publish();

        System.in.read();
    }
}
