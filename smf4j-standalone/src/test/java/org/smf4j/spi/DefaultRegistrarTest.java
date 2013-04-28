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
package org.smf4j.spi;

import java.util.Map;
import org.smf4j.RegistryNode;
import org.smf4j.Accumulator;
import static org.junit.Assert.*;

import org.junit.Test;
import org.smf4j.Mutator;
import org.smf4j.nop.NopAccumulator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class DefaultRegistrarTest {

    @Test
    public void testHierarchyNameRules() {
        hierarchyTrial(false, "foo..bar", "Empty parts allowed.");
        hierarchyTrial(false, "foo.bar..", "Empty parts allowed.");
        hierarchyTrial(false, ".foo.bar", "Empty parts allowed.");
        hierarchyTrial(false, "foo. .bar", "Whitespace not collapsed.");
        hierarchyTrial(false, "foo.+asdf.bar", "+ allowed.");
        hierarchyTrial(false, "foo.*.asdf", "* allowed.");
        hierarchyTrial(false, "", "Whitespace path allowed.");

        hierarchyTrial(true, "foo", "single part not allowed.");
        hierarchyTrial(true, "foo.bar", "multi-element path not allowed.");
        hierarchyTrial(true, "foo. bar .baz", "multi-element path not allowed");
    }

    private void hierarchyTrial(boolean pass, String hierarchy, String message){
        DefaultRegistrar r = new DefaultRegistrar();
        boolean caught = false;
        if(null == r.splitFullNodeName(hierarchy)) {
            caught = true;
        }
        assertTrue(message, caught != pass);
    }

    @Test
    public void testSplitFullNodeName()
    throws Exception {
        accumulatorTrial(false, "foo..bar", "Empty part allowed.");
        accumulatorTrial(false, "foo+asdf", "+ allowed.");
        accumulatorTrial(false, "foo*asdf", "* allowed.");
        accumulatorTrial(false, "", "Empty allowed.");
        accumulatorTrial(false, "", "All whitespace allowed.");

        accumulatorTrial(true, "foo", "Simple name not allowed.", "foo");
        accumulatorTrial(true, "foo.bar.baz", "Hierarchical name not allowed.",
                "foo", "bar", "baz");

        accumulatorTrial(true, " f", "Whitespace not collapsed", "f");
        accumulatorTrial(true, "f ", "Whitespace not collapsed", "f");
        accumulatorTrial(true, " f ", "Whitespace not collapsed", "f");
    }

    private void accumulatorTrial(boolean pass, String fullNodeName, String message,
            String... parts) {
        DefaultRegistrar r = new DefaultRegistrar();
        boolean caught = false;
        String[] results = r.splitFullNodeName(fullNodeName);
        if(results == null) {
            caught = true;
        }

        assertTrue(message, caught != pass);

        if(pass) {
            assertArrayEquals("Unexpected split results.", parts, results);
        }
    }

    @Test
    public void rootNode()
    throws Exception {
        DefaultRegistrar r = new DefaultRegistrar();
        RegistryNode root = r.getRootNode();
        Accumulator one = createAcc();
        Accumulator two = createAcc();

        assertEquals("Couldn't add one", one, root.register("one", one));
        assertEquals("Duplicate add of one", one, root.register("one", one));
        assertEquals("Couldn't add two", two, root.register("two", two));

        assertEquals("Not 2 accumulators", 2, root.getAccumulators().size());
        assertEquals("one not present", one, root.getAccumulator("one"));
        assertEquals("two not present", two, root.getAccumulator("two"));
        assertSame("three is present", NopAccumulator.INSTANCE,
                root.getAccumulator("three"));
    }

    @Test
    public void multiNodes()
    throws Exception {
        DefaultRegistrar r = new DefaultRegistrar();
        RegistryNode a;
        RegistryNode b;
        RegistryNode a_first;
        RegistryNode b_second;
        Accumulator one = createAcc();
        Accumulator two = createAcc();
        Accumulator three = createAcc();
        Accumulator four = createAcc();

        a = r.getNode("a");
        b = r.getNode("b");
        a_first = r.getNode("a.first");
        b_second = r.getNode("b.second");

        assertNotNull("Couldn't create a.first", a_first);
        assertNotNull("Couldn't create b.second", b_second);

        // a.first one, two
        assertEquals("Couldn't add one", one, a_first.register("one", one));
        assertEquals("Duplicate add of one", one, a_first.register("one", one));
        assertEquals("Couldn't add two", two, a_first.register("two", two));

        // b.second+three, four
        assertEquals("Couldn't add three", three,
                b_second.register("three", three));
        assertEquals("Duplicate add of three", three,
                b_second.register("three", three));
        assertEquals("Couldn't add four", four,
                b_second.register("four", four));

        assertEquals("Accumulators in a", 0, a.getAccumulators().size());
        assertEquals("Accumulators in b", 0, b.getAccumulators().size());

        assertEquals("Not 2 accumulators in a.first", 2,
                a_first.getAccumulators().size());
        assertEquals("one not present", one, a_first.getAccumulator("one"));
        assertEquals("two not present", two, a_first.getAccumulator("two"));
        assertSame("ten is present", NopAccumulator.INSTANCE,
                a_first.getAccumulator("ten"));

        assertEquals("Not 2 accumulators in b.second", 2,
                b_second.getAccumulators().size());
        assertEquals("three not present", three,
                b_second.getAccumulator("three"));
        assertEquals("four not present", four, b_second.getAccumulator("four"));
        assertSame("ten is present", NopAccumulator.INSTANCE,
                b_second.getAccumulator("ten"));
    }

    private Accumulator createAcc() {
        return new Accumulator() {
            private boolean on = false;

            public long get() {
                return 0;
            }

            public boolean isOn() {
                return on;
            }

            public void setOn(boolean on) {
                this.on = on;
            }

            public Mutator getMutator() {
                return this;
            }

            public String getUnits() {
                return null;
            }

            public long getTimeWindow() {
                return 0L;
            }

            public int getIntervals() {
                return 0;
            }

            public Map<Object, Object> getMetadata() {
                return null;
            }

            public void put(long delta) {
            }
        };
    }

    @Test
    public void unregister()
    throws Exception {
        DefaultRegistrar r = new DefaultRegistrar();
        RegistryNode root = r.getRootNode();
        Accumulator one = createAcc();
        Accumulator two = createAcc();

        assertNotNull("Couldn't register root", root);

        assertEquals("Couldn't add one", one, root.register("one", one));
        assertEquals("Duplicate add of one", one, root.register("one", one));
        assertEquals("Couldn't add two", two, root.register("two", two));

        assertEquals("Not 2 accumulators", 2, root.getAccumulators().size());
        assertEquals("one not present", one, root.getAccumulator("one"));
        assertEquals("two not present", two, root.getAccumulator("two"));
        assertSame("three is present", NopAccumulator.INSTANCE,
                root.getAccumulator("three"));

        assertTrue("Couldn't unregister one", root.unregister("one", one));
        assertTrue("Couldn't unregister two", root.unregister("two", two));
        assertEquals("Not 0 accumulators", 0, root.getAccumulators().size());
    }

    @Test
    public void nodeStates()
    throws Exception {
        DefaultRegistrar r = new DefaultRegistrar();
        RegistryNode root = r.getRootNode();
        RegistryNode first = r.getNode("first");

        // Root node defaults to off
        assertFalse(root.isOn());

        // Node state setOn() works
        root.setOn(true);
        assertTrue(root.isOn());

        // Node state set to parent node's state when created
        assertTrue(first.isOn());

        // Node state with unset level follows parent
        root.setOn(false);
        assertFalse(first.isOn());

        // Node state with local level ignores parent
        first.setOn(true);
        assertFalse(root.isOn());
        assertTrue(first.isOn());
        root.setOn(true);
        assertTrue(root.isOn());
        assertTrue(first.isOn());
        root.setOn(false);
        assertFalse(root.isOn());
        assertTrue(first.isOn());

        // Cleared local level follows parent
        first.clearOn();
        assertFalse(root.isOn());
        assertFalse(first.isOn());
        root.setOn(true);
        assertTrue(root.isOn());
        assertTrue(first.isOn());
    }

    @Test
    public void accStates()
    throws Exception {
        DefaultRegistrar r = new DefaultRegistrar();
        RegistryNode root;
        RegistryNode first;
        Accumulator one = createAcc();
        Accumulator two = createAcc();

        root = r.getRootNode();

        // Root node defaults to off
        assertFalse(root.isOn());

        // Accumulator set to node state
        assertEquals(one, root.register("one", one));
        assertFalse(root.getAccumulator("one").isOn());

        // Accumulator follows node state
        root.setOn(true);
        assertTrue(root.getAccumulator("one").isOn());

        // Node state set to parent node's state when created
        first = r.getNode("first");
        assertTrue(first.isOn());

        // Accumulator set to node state
        first.register("two", two);
        assertTrue(first.getAccumulator("two").isOn());

        // Accumulator follows node state
        root.setOn(false);
        assertFalse(one.isOn());
        assertFalse(two.isOn());
        first.setOn(true);
        assertFalse(one.isOn());
        assertTrue(two.isOn());
    }
}
