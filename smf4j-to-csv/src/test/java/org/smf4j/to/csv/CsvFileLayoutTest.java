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
package org.smf4j.to.csv;

import org.smf4j.to.csv.CsvFileLayout;
import org.smf4j.to.csv.CsvDataColumn;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.BeforeClass;

import org.junit.Test;
import org.smf4j.DynamicFilter;
import org.smf4j.DynamicFilterListener;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class CsvFileLayoutTest {

    private static List<RegistryNode> nodes;

    @Test
    public void gatherNodes() {
        List<RegistryNode> n = new ArrayList<RegistryNode>();
        n.add(nodes.get(0));
        n.add(nodes.get(1));
        n.add(nodes.get(2));

        List<RegistryNode> fn1 = new ArrayList<RegistryNode>();
        fn1.add(nodes.get(2));
        fn1.add(nodes.get(3));
        DynamicFilter f1 = new StaticFilter(fn1);

        List<RegistryNode> fn2 = new ArrayList<RegistryNode>();
        fn2.add(nodes.get(3));
        fn2.add(nodes.get(4));
        DynamicFilter f2 = new StaticFilter(fn2);

        List<DynamicFilter> f = new ArrayList<DynamicFilter>();
        f.add(f1);
        f.add(f2);

        CsvFileLayout l = new CsvFileLayout();
        l.setNodes(n);
        l.setNodeFilters(f);

        List<RegistryNode> collected = l.gatherAllNodes();
        assertEquals(5, collected.size());
        assertTrue(collected.contains(nodes.get(0)));
        assertTrue(collected.contains(nodes.get(1)));
        assertTrue(collected.contains(nodes.get(2)));
        assertTrue(collected.contains(nodes.get(3)));
        assertTrue(collected.contains(nodes.get(4)));
        assertFalse(collected.contains(nodes.get(5)));
        assertFalse(collected.contains(nodes.get(6)));
        assertFalse(collected.contains(nodes.get(7)));
        assertFalse(collected.contains(nodes.get(8)));
        assertFalse(collected.contains(nodes.get(9)));
    }

    @Test
    public void createColumnsNoneTest() {
        CsvFileLayout l = new CsvFileLayout();
        List<RegistryNode> n = new ArrayList<RegistryNode>();

        List<CsvDataColumn> cols = l.createColumns(n);
        assertEquals(0, cols.size());
    }

    @Test
    public void createColumnsSingleTest() {
        CsvFileLayout l = new CsvFileLayout();
        List<RegistryNode> n = new ArrayList<RegistryNode>();

        n.add(nodes.get(0));
        List<CsvDataColumn> cols = l.createColumns(n);
        assertEquals(6, cols.size());

        for(int i=0; i<cols.size(); i++) {
            CsvDataColumn col = cols.get(i);
            assertFalse(col.isUseFullName());
            assertEquals(expectedNames[i], col.getColumnName());
        }
    }

    @Test
    public void createColumnsMultiTest() {
        CsvFileLayout l = new CsvFileLayout();
        List<RegistryNode> n = new ArrayList<RegistryNode>();

        n.add(nodes.get(0));
        n.add(nodes.get(1));
        n.add(nodes.get(2));
        List<CsvDataColumn> cols = l.createColumns(n);
        assertEquals(18, cols.size());

        for(int i=0; i<cols.size(); i++) {
            String nodeName = "node" + (i/6);
            CsvDataColumn col = cols.get(i);
            assertTrue(col.isUseFullName());
            assertEquals(nodeName + "." + expectedNames[i % 6],
                    col.getColumnName());
        }
    }

    private static String[] expectedNames = new String[] {
            "acc1",
            "acc2",
            "calc1.intProperty",
            "calc1.stringProperty",
            "calc2.intProperty",
            "calc2.stringProperty"
        };


    @BeforeClass
    public static void beforeClass() {
        nodes = new ArrayList<RegistryNode>();
        for(int i=0; i<10; i++) {
            nodes.add(createFakeNode("node" + i,
                    new String[] {"acc1", "acc2"},
                    new String[] {"calc1", "calc2"}));
        }
    }

    public static RegistryNode createFakeNode(String nodeName,
            String[] accNames, String[] calcNames) {

        MockRegistryNode mock = new MockRegistryNode();
        for(int i=0; i<accNames.length; i++) {
            mock.register(accNames[i], new MockAccumulator(0));
        }
        for(int i=0; i<calcNames.length; i++) {
            mock.register(calcNames[i], new MockCalculation(0, "A String"));
        }
        mock.setName(nodeName);

        return mock;
    }

    static class StaticFilter implements DynamicFilter {

        private final List<RegistryNode> nodes;

        StaticFilter(List<RegistryNode> nodes) {
            this.nodes = nodes;
        }

        @Override
        public void registerListener(DynamicFilterListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void unregisterListener(DynamicFilterListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Iterator<RegistryNode> iterator() {
            return nodes.iterator();
        }
    }
}
