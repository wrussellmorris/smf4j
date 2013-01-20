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
package org.smf4j.to.csv;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smf4j.Calculator;
import org.smf4j.Accumulator;
import org.smf4j.DynamicFilter;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class CsvFileLayout {

    private Logger log = LoggerFactory.getLogger(getClass());

    private List<RegistryNode> nodes = new ArrayList<RegistryNode>();
    private List<DynamicFilter> nodeFilters = new ArrayList<DynamicFilter>();
    private List<CsvDataColumn> columns;

    private static final Set<Class<?>> leafTypes = new HashSet<Class<?>>();
    static {
        leafTypes.add(byte.class);
        leafTypes.add(short.class);
        leafTypes.add(int.class);
        leafTypes.add(long.class);
        leafTypes.add(float.class);
        leafTypes.add(double.class);

        leafTypes.add(Byte.class);
        leafTypes.add(Short.class);
        leafTypes.add(Integer.class);
        leafTypes.add(Long.class);
        leafTypes.add(Float.class);
        leafTypes.add(Double.class);
        leafTypes.add(String.class);
    }

    public void prepare() {
        this.columns = createColumns(gatherAllNodes());
    }

    public List<CsvDataColumn> getColumns() {
        if(columns == null) {
            prepare();
        }
        return columns;
    }

    protected List<CsvDataColumn> createColumns(List<RegistryNode> nodes) {
        List<CsvDataColumn> cols = new ArrayList<CsvDataColumn>();
        Map<String, CsvDataColumn> names = new HashMap<String, CsvDataColumn>();

        for(RegistryNode node : nodes) {
            Map<String, Accumulator> accs = node.getAccumulators();
            for(Map.Entry<String, Accumulator> entry : accs.entrySet()) {
                String name = entry.getKey();

                // Create column
                CsvAccumulatorColumn col = new CsvAccumulatorColumn(node, name);

                // If collision, make sure everyone uses full names
                if(names.containsKey(name)) {
                    names.get(name).setUseFullName(true);
                    col.setUseFullName(true);
                } else {
                    names.put(name, col);
                }

                cols.add(col);
            }

            Map<String, Calculator> calcs = node.getCalculators();
            for(Map.Entry<String, Calculator> entry : calcs.entrySet()) {
                List<String> calcNames = getAllCalcNames(
                        entry.getKey(), entry.getValue());

                for(String calcName : calcNames) {
                    // Create column
                    CsvCalculatorColumn col =
                            new CsvCalculatorColumn(node, calcName);

                    // If collision, make sure everyone uses full names
                    if(names.containsKey(calcName)) {
                        names.get(calcName).setUseFullName(true);
                        col.setUseFullName(true);
                    } else {
                        names.put(calcName, col);
                    }

                    cols.add(col);
                }
            }
        }

        Collections.sort(cols, new Comparator<CsvDataColumn>() {
            @Override
            public int compare(CsvDataColumn o1, CsvDataColumn o2) {
                return o1.getColumnName().compareTo(o2.getColumnName());
            }
        });

        return cols;
    }

    protected List<RegistryNode> gatherAllNodes() {
        Set<RegistryNode> all = new HashSet<RegistryNode>();

        all.addAll(nodes);
        for(DynamicFilter nodeFilter : nodeFilters) {
            for(RegistryNode node : nodeFilter) {
                all.add(node);
            }
        }

        List<RegistryNode> list = new ArrayList<RegistryNode>();
        list.addAll(all);

        Collections.sort(list, new Comparator<RegistryNode>() {
            @Override
            public int compare(RegistryNode o1, RegistryNode o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return list;
    }

    protected List<String> getAllCalcNames(String rootName, Calculator calc) {
        List<String> names = new ArrayList<String>();

        // Get the return type of the calculate method
        Method m;
        try {
            m = calc.getClass().getMethod("calculate", Map.class, Map.class);
        } catch(Throwable t) {
            log.error(String.format(
                    "Failed to determine return value of the 'calculate' method"
                    + " for the Calculator implementing class '%s'.",
                    calc.getClass().getCanonicalName()));
            return names;
        }

        Class<?> calcResult = m.getReturnType();

        if(leafTypes.contains(calcResult)) {
            names.add(rootName);
        } else {
            collectLeafProperties(rootName, calcResult, names);
        }

        return names;
    }

    protected void collectLeafProperties(String rootName, Class<?> calcType,
            List<String> names) {
        try {
            BeanInfo bi = Introspector.getBeanInfo(calcType);

            for(PropertyDescriptor pd : bi.getPropertyDescriptors()) {
                if(pd.getReadMethod() == null ||
                        !leafTypes.contains(pd.getPropertyType())) {
                    continue;
                }

                names.add(rootName + "." + pd.getName());
            }
        } catch(IntrospectionException e) {
        }
    }

    public List<RegistryNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<RegistryNode> nodes) {
        this.nodes = nodes;
    }

    public List<DynamicFilter> getNodeFilters() {
        return nodeFilters;
    }

    public void setNodeFilters(List<DynamicFilter> nodeFilters) {
        this.nodeFilters = nodeFilters;
    }
}
