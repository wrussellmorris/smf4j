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

import static org.junit.Assert.*;
import org.junit.Before;

import org.junit.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.smf4j.Accumulator;
import org.smf4j.Calculator;
import org.smf4j.Registrar;
import org.smf4j.RegistrarFactoryForUnitTests;
import org.smf4j.RegistryNode;
import org.smf4j.core.accumulator.hc.HighConcurrencyAccumulator;
import org.smf4j.core.accumulator.lc.LowConcurrencyAccumulator;
import org.smf4j.core.calculator.Frequency;
import org.smf4j.core.calculator.Normalizer;
import org.smf4j.core.calculator.RangeGroupCalculator;
import org.smf4j.core.calculator.Ratio;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class RegistrarTest {

    @Before
    public void before()
    throws Exception {
        RegistrarFactoryForUnitTests.reset(true);
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
    public void nodeTemplates()
    throws Exception {
        ApplicationContext context = loadContext("registration-template.xml");
        assertNotNull(context);

        Registrar r = context.getBean("registrar", Registrar.class);
        assertNotNull(r);

        RegistryNode foo = r.getNode("foo");
        RegistryNode bar = r.getNode("foo.bar");
        RegistryNode baz = r.getNode("foo.bar.baz");
        RegistryNode bot = r.getNode("foo.bar.baz.bot");
        RegistryNode p_foo = r.getNode("parent.foo");
        RegistryNode p_bar = r.getNode("parent.foo.bar");
        RegistryNode p_baz = r.getNode("parent.foo.bar.baz");
        RegistryNode p_bot = r.getNode("parent.foo.bar.baz.bot");
        Accumulator counter_foo;
        Accumulator counter_bar;
        Accumulator counter_baz;
        Accumulator p_counter_foo;
        Accumulator p_counter_bar;
        Accumulator p_counter_baz;

        assertNotNull(foo);
        assertNotNull(bar);
        assertNotNull(baz);
        assertNotNull(bot);
        assertNotNull(p_foo);
        assertNotNull(p_bar);
        assertNotNull(p_baz);
        assertNotNull(p_bot);

        assertEquals(1, foo.getAccumulators().size());
        counter_foo = foo.getAccumulator("counter_foo");
        assertNotNull(counter_foo);
        assertEquals(0, foo.getCalculators().size());

        assertEquals(1, bar.getAccumulators().size());
        counter_bar = bar.getAccumulator("counter_bar");
        assertNotNull(counter_bar);
        assertEquals(0, bar.getCalculators().size());

        assertEquals(1, baz.getAccumulators().size());
        counter_baz = baz.getAccumulator("counter_baz");
        assertNotNull(baz);
        assertEquals(0, baz.getCalculators().size());

        assertEquals(1, p_foo.getAccumulators().size());
        p_counter_foo = p_foo.getAccumulator("counter_foo");
        assertNotNull(p_counter_foo);
        assertEquals(0, p_foo.getCalculators().size());

        assertEquals(1, p_bar.getAccumulators().size());
        p_counter_bar = p_bar.getAccumulator("counter_bar");
        assertNotNull(p_counter_bar);
        assertEquals(0, p_bar.getCalculators().size());

        assertEquals(1, p_baz.getAccumulators().size());
        p_counter_baz = p_baz.getAccumulator("counter_baz");
        assertNotNull(p_counter_baz);
        assertEquals(0, p_baz.getCalculators().size());

        assertNotSame(context, p_baz);

        RegistryNode eep = r.getNode("eep");
        RegistryNode op = r.getNode("eep.op");
        RegistryNode ork = r.getNode("eep.op.ork");
        RegistryNode aa = r.getNode("eep.op.ork.aa");
        assertNotSame(counter_bar, counter_baz);

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
        assertNotNull(counter_ork);
        assertEquals(0, ork.getCalculators().size());

        assertNotSame(counter_foo, p_counter_foo);
        assertNotSame(counter_bar, p_counter_bar);
        assertNotSame(counter_baz, p_counter_bar);
    }

    @Test
    public void counter()
    throws Exception {
        ApplicationContext context = loadContext("registrar-counter.xml");
        assertNotNull(context);

        Registrar r1 = context.getBean("registrar-1", Registrar.class);
        assertNotNull(r1);

        RegistryNode counters = r1.getNode("counters");
        assertNotNull(counters);

        Accumulator c;
        c = counters.getAccumulator("c_");
        assertCounterMakeup(c, true, false, false);

        c = counters.getAccumulator("c_l");
        assertCounterMakeup(c, false, false, false);

        c = counters.getAccumulator("c_h");
        assertCounterMakeup(c, true, false, false);

        c = counters.getAccumulator("c_l_u");
        assertCounterMakeup(c, false, false, false);

        c = counters.getAccumulator("c_l_w_s");
        assertCounterMakeup(c, false, true, false);

        c = counters.getAccumulator("c_l_w_2");
        assertCounterMakeup(c, false, true, true);

        c = counters.getAccumulator("c_h_u");
        assertCounterMakeup(c, true, false, false);

        c = counters.getAccumulator("c_h_w_s");
        assertCounterMakeup(c, true, true, false);

        c = counters.getAccumulator("c_h_w_2");
        assertCounterMakeup(c, true, true, true);
    }

    @Test
    public void maxcounter()
    throws Exception {
        ApplicationContext context = loadContext("registrar-maxcounter.xml");
        assertNotNull(context);

        Registrar r1 = context.getBean("registrar-1", Registrar.class);
        assertNotNull(r1);

        RegistryNode counters = r1.getNode("counters");
        assertNotNull(counters);

        Accumulator m;
        m = counters.getAccumulator("m_");
        assertMaxMakeup(m, true, false, false);

        m = counters.getAccumulator("m_l");
        assertMaxMakeup(m, false, false, false);

        m = counters.getAccumulator("m_h");
        assertMaxMakeup(m, true, false, false);

        m = counters.getAccumulator("m_l_u");
        assertMaxMakeup(m, false, false, false);

        m = counters.getAccumulator("m_l_w_s");
        assertMaxMakeup(m, false, true, false);

        m = counters.getAccumulator("m_l_w_2");
        assertMaxMakeup(m, false, true, true);

        m = counters.getAccumulator("m_h_u");
        assertMaxMakeup(m, true, false, false);

        m = counters.getAccumulator("m_h_w_s");
        assertMaxMakeup(m, true, true, false);

        m = counters.getAccumulator("m_h_w_2");
        assertMaxMakeup(m, true, true, true);
    }

    @Test
    public void mincounter()
    throws Exception {
        ApplicationContext context = loadContext("registrar-mincounter.xml");
        assertNotNull(context);

        Registrar r1 = context.getBean("registrar-1", Registrar.class);
        assertNotNull(r1);

        RegistryNode counters = r1.getNode("counters");
        assertNotNull(counters);

        Accumulator m;
        m = counters.getAccumulator("m_");
        assertMinMakeup(m, true, false, false);

        m = counters.getAccumulator("m_l");
        assertMinMakeup(m, false, false, false);

        m = counters.getAccumulator("m_h");
        assertMinMakeup(m, true, false, false);

        m = counters.getAccumulator("m_l_u");
        assertMinMakeup(m, false, false, false);

        m = counters.getAccumulator("m_l_w_s");
        assertMinMakeup(m, false, true, false);

        m = counters.getAccumulator("m_l_w_2");
        assertMinMakeup(m, false, true, true);

        m = counters.getAccumulator("m_h_u");
        assertMinMakeup(m, true, false, false);

        m = counters.getAccumulator("m_h_w_s");
        assertMinMakeup(m, true, true, false);

        m = counters.getAccumulator("m_h_w_2");
        assertMinMakeup(m, true, true, true);
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

        Accumulator counter = context
                .getBean("customBeanId", Accumulator.class);
        assertNotNull(counter);

        Accumulator refAttr = foobar.getAccumulator("refAttr");
        assertNotNull(refAttr);
        assertSame(counter, refAttr);

        Accumulator beanTagWithId = foobar.getAccumulator("beanTagWithId");
        assertNotNull(beanTagWithId);

        Accumulator beanTagWithoutId = foobar
                .getAccumulator("beanTagWithoutId");
        assertNotNull(beanTagWithoutId);

        Accumulator refTag = foobar.getAccumulator("refTag");
        assertNotNull(refTag);
        assertSame(counter, refTag);

        Accumulator idrefTag = foobar.getAccumulator("idrefTag");
        assertNotNull(idrefTag);
        assertSame(counter, idrefTag);
    }

    @Test
    public void calculators()
    throws Exception {
        ApplicationContext context = loadContext("registrar-calculators.xml");
        assertNotNull(context);

        Registrar r = context.getBean("registrar-1", Registrar.class);
        assertNotNull(r);

        RegistryNode foobar = r.getNode("foo.bar");
        assertNotNull(foobar);

        Accumulator test = foobar.getAccumulator("test");
        assertNotNull(test);

        Normalizer normalize = (Normalizer)foobar.getCalculator("normalize");
        assertNotNull(normalize);
        assertEquals("test", normalize.getAccumulator());
        assertEquals(Frequency.MILLIS, normalize.getFrequency());

        Ratio ratio = (Ratio)foobar.getCalculator("ratio");
        assertNotNull(ratio);
        assertEquals("units", ratio.getUnits());
        assertEquals("test", ratio.getNumerator());
        assertEquals("test", ratio.getDenominator());

        RangeGroupCalculator rg = (RangeGroupCalculator)
                foobar.getCalculator("rangegroup");
        assertNotNull(rg);
        assertEquals("test", rg.getAccumulator());
        assertEquals(0.75d, rg.getThreshold(), 0.00001d);
        assertEquals("units", rg.getUnits());
        assertTrue(rg.isNormalize());
        assertEquals(Frequency.MILLIS, rg.getFrequency());
        assertEquals("%.3f%s", rg.getFormatString());
    }

    private ApplicationContext loadContext(String path) {
        return new ClassPathXmlApplicationContext(path, getClass());
    }

    private void assertCounterMakeup(Accumulator accumulator,
            boolean highConcurrency, boolean windowed, boolean powersOfTwo) {
        assertNotNull(accumulator);

        if(highConcurrency) {
            if(windowed) {
                assertTrue(accumulator instanceof HighConcurrencyAccumulator);
                assertTrue(accumulator.getMutator() instanceof
                        org.smf4j.core.accumulator.hc.WindowedAddMutator);
                if(powersOfTwo) {
                    assertEquals((1<<28)-(1<<24), accumulator.getTimeWindow());
                    assertEquals((1<<5)-2, accumulator.getIntervals());
                } else {
                    assertEquals(1000000000L, accumulator.getTimeWindow());
                    assertEquals(10, accumulator.getIntervals());
                }
            } else {
                assertTrue(accumulator instanceof HighConcurrencyAccumulator);
                assertTrue(accumulator.getMutator() instanceof
                        org.smf4j.core.accumulator.hc.UnboundedAddMutator);
            }
        } else {
            if(windowed) {
                assertTrue(accumulator instanceof LowConcurrencyAccumulator);
                assertTrue(accumulator.getMutator() instanceof
                        org.smf4j.core.accumulator.lc.WindowedAddMutator);
                if(powersOfTwo) {
                    assertEquals((1<<28)-(1<<24), accumulator.getTimeWindow());
                    assertEquals((1<<5)-2, accumulator.getIntervals());
                } else {
                    assertEquals(1000000000L, accumulator.getTimeWindow());
                    assertEquals(10, accumulator.getIntervals());
                }
            } else {
                assertTrue(accumulator instanceof LowConcurrencyAccumulator);
                assertTrue(accumulator.getMutator() instanceof
                        org.smf4j.core.accumulator.lc.UnboundedAddMutator);
            }
        }
    }

    private void assertMaxMakeup(Accumulator accumulator,
            boolean highConcurrency, boolean windowed, boolean powersOfTwo) {
        assertNotNull(accumulator);

        if(highConcurrency) {
            if(windowed) {
                assertTrue(accumulator instanceof HighConcurrencyAccumulator);
                assertTrue(accumulator.getMutator() instanceof
                        org.smf4j.core.accumulator.hc.WindowedMaxMutator);
                if(powersOfTwo) {
                    assertEquals((1<<28)-(1<<24), accumulator.getTimeWindow());
                    assertEquals((1<<5)-2, accumulator.getIntervals());
                } else {
                    assertEquals(1000000000L, accumulator.getTimeWindow());
                    assertEquals(10, accumulator.getIntervals());
                }
            } else {
                assertTrue(accumulator instanceof HighConcurrencyAccumulator);
                assertTrue(accumulator.getMutator() instanceof
                        org.smf4j.core.accumulator.hc.UnboundedMaxMutator);
            }
        } else {
            if(windowed) {
                assertTrue(accumulator instanceof LowConcurrencyAccumulator);
                assertTrue(accumulator.getMutator() instanceof
                        org.smf4j.core.accumulator.lc.WindowedMaxMutator);
                if(powersOfTwo) {
                    assertEquals((1<<28)-(1<<24), accumulator.getTimeWindow());
                    assertEquals((1<<5)-2, accumulator.getIntervals());
                } else {
                    assertEquals(1000000000L, accumulator.getTimeWindow());
                    assertEquals(10, accumulator.getIntervals());
                }
            } else {
                assertTrue(accumulator instanceof LowConcurrencyAccumulator);
                assertTrue(accumulator.getMutator() instanceof
                        org.smf4j.core.accumulator.lc.UnboundedMaxMutator);
            }
        }
    }

    private void assertMinMakeup(Accumulator accumulator,
            boolean highConcurrency, boolean windowed, boolean powersOfTwo) {
        assertNotNull(accumulator);

        if(highConcurrency) {
            if(windowed) {
                assertTrue(accumulator instanceof HighConcurrencyAccumulator);
                assertTrue(accumulator.getMutator() instanceof
                        org.smf4j.core.accumulator.hc.WindowedMinMutator);
                if(powersOfTwo) {
                    assertEquals((1<<28)-(1<<24), accumulator.getTimeWindow());
                    assertEquals((1<<5)-2, accumulator.getIntervals());
                } else {
                    assertEquals(1000000000L, accumulator.getTimeWindow());
                    assertEquals(10, accumulator.getIntervals());
                }
            } else {
                assertTrue(accumulator instanceof HighConcurrencyAccumulator);
                assertTrue(accumulator.getMutator() instanceof
                        org.smf4j.core.accumulator.hc.UnboundedMinMutator);
            }
        } else {
            if(windowed) {
                assertTrue(accumulator instanceof LowConcurrencyAccumulator);
                assertTrue(accumulator.getMutator() instanceof
                        org.smf4j.core.accumulator.lc.WindowedMinMutator);
                if(powersOfTwo) {
                    assertEquals((1<<28)-(1<<24), accumulator.getTimeWindow());
                    assertEquals((1<<5)-2, accumulator.getIntervals());
                } else {
                    assertEquals(1000000000L, accumulator.getTimeWindow());
                    assertEquals(10, accumulator.getIntervals());
                }
            } else {
                assertTrue(accumulator instanceof LowConcurrencyAccumulator);
                assertTrue(accumulator.getMutator() instanceof
                        org.smf4j.core.accumulator.lc.UnboundedMinMutator);
            }
        }
    }
}
