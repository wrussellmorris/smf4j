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

import org.junit.After;
import static org.junit.Assert.*;

import org.junit.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.smf4j.Accumulator;
import org.smf4j.Registrar;
import org.smf4j.RegistrarFactory;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class RegistrarTest {

    @After
    public void after()
    throws Exception {
        RegistrarFactory.getRegistrar().clear();
    }

    @Test
    public void autoregistration()
    throws Exception {
        ApplicationContext context = loadContext("registration-context.xml");
        assertNotNull(context);

        Registrar r1 = context.getBean("registrar-1", Registrar.class);
        assertNotNull(r1);
        Registrar r2 = context.getBean("registrar-2", Registrar.class);
        assertNotNull(r2);

        RegistryNode foo = r1.getNode("foo");
        RegistryNode bar = r1.getNode("foo.bar");
        RegistryNode baz = r1.getNode("foo.bar.baz");
        RegistryNode bot = r1.getNode("foo.bar.baz.bot");
        Accumulator counter_bar;
        Accumulator counter_baz;

        assertNotNull(foo);
        assertNotNull(bar);
        assertNotNull(baz);
        assertNotNull(bot);

        assertEquals(0, foo.getAccumulators().size());
        assertEquals(0, foo.getCalculators().size());

        assertEquals(1, bar.getAccumulators().size());
        counter_bar = bar.getAccumulator("counter_bar");
        assertNotNull(counter_bar);
        assertEquals(0, bar.getCalculators().size());

        assertEquals(1, baz.getAccumulators().size());
        counter_baz = baz.getAccumulator("counter_baz");
        assertNotNull(baz);
        assertEquals(0, baz.getCalculators().size());

        assertNotSame(counter_bar, counter_baz);

        RegistryNode eep = r2.getNode("eep");
        RegistryNode op = r2.getNode("eep.op");
        RegistryNode ork = r2.getNode("eep.op.ork");
        RegistryNode aa = r2.getNode("eep.op.ork.aa");
        Accumulator counter_op;
        Accumulator counter_ork;

        assertNotNull(eep);
        assertNotNull(op);
        assertNotNull(ork);
        assertNotNull(aa);

        assertEquals(0, eep.getAccumulators().size());
        assertEquals(0, eep.getCalculators().size());

        assertEquals(1, op.getAccumulators().size());
        counter_op = op.getAccumulator("counter_op");
        assertNotNull(counter_op);
        assertEquals(0, op.getCalculators().size());

        assertEquals(1, ork.getAccumulators().size());
        counter_ork = ork.getAccumulator("counter_ork");
        assertNotNull(ork);
        assertEquals(0, ork.getCalculators().size());

        assertNotSame(counter_op, counter_ork);
    }

    @Test
    public void counter()
    throws Exception {
        ApplicationContext context = loadContext("registrar-counter.xml");
        assertNotNull(context);

        Registrar r1 = context.getBean("registrar-1", Registrar.class);
        assertNotNull(r1);

        RegistryNode foobar = r1.getNode("foo.bar");
        assertNotNull(foobar);

        Accumulator counter = foobar.getAccumulator("counter");
        assertNotNull(counter);
    }

    @Test
    public void mincounter()
    throws Exception {
        ApplicationContext context = loadContext("registrar-mincounter.xml");
        assertNotNull(context);

        Registrar r1 = context.getBean("registrar-1", Registrar.class);
        assertNotNull(r1);

        RegistryNode foobar = r1.getNode("foo.bar");
        assertNotNull(foobar);

        Accumulator counter = foobar.getAccumulator("mincounter");
        assertNotNull(counter);
    }

    @Test
    public void maxcounter()
    throws Exception {
        ApplicationContext context = loadContext("registrar-maxcounter.xml");
        assertNotNull(context);

        Registrar r1 = context.getBean("registrar-1", Registrar.class);
        assertNotNull(r1);

        RegistryNode foobar = r1.getNode("foo.bar");
        assertNotNull(foobar);

        Accumulator counter = foobar.getAccumulator("maxcounter");
        assertNotNull(counter);
    }

    @Test
    public void custom()
    throws Exception {
        ApplicationContext context = loadContext("registrar-bean.xml");
        assertNotNull(context);

        Registrar r1 = context.getBean("registrar-1", Registrar.class);
        assertNotNull(r1);

        RegistryNode foobar = r1.getNode("foo.bar");
        assertNotNull(foobar);

        Accumulator counter = context.getBean("customBeanId", Accumulator.class);
        assertNotNull(counter);

        Accumulator refAttr = foobar.getAccumulator("refAttr");
        assertNotNull(refAttr);
        assertSame(counter, refAttr);

        Accumulator beanTagWithId = foobar.getAccumulator("beanTagWithId");
        assertNotNull(beanTagWithId);

        Accumulator beanTagWithoutId = foobar.getAccumulator("beanTagWithoutId");
        assertNotNull(beanTagWithoutId);

        Accumulator refTag = foobar.getAccumulator("refTag");
        assertNotNull(refTag);
        assertSame(counter, refTag);

        Accumulator idrefTag = foobar.getAccumulator("idrefTag");
        assertNotNull(idrefTag);
        assertSame(counter, idrefTag);
    }

    private ApplicationContext loadContext(String path) {
        return new ClassPathXmlApplicationContext(path, getClass());
    }
}
