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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.smf4j.Calculator;
import org.smf4j.Accumulator;
import org.smf4j.Registrar;
import org.smf4j.RegistrarFactory;
import org.smf4j.helpers.CalculatorHelper;
import org.smf4j.RegistryNode;
import org.smf4j.helpers.CalculatorAttribute;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class CsvFileLayout {
    private List<String> nodeFilters = new ArrayList<String>();
    private List<CsvDataColumn> columns;

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
                CsvAccumulatorColumn col = new CsvAccumulatorColumn(node,
                        name, entry.getValue().getUnits());

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
                List<CalculatorAttribute> attrs =
                        CalculatorHelper.getCalculatorAttributes(entry.getKey(),
                        entry.getValue());

                for(CalculatorAttribute attr : attrs) {
                    String calcName = attr.getName();
                    // Create column
                    CsvCalculatorColumn col =
                            new CsvCalculatorColumn(node, calcName,
                            attr.getUnits());

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

        Registrar r = RegistrarFactory.getRegistrar();
        for(String nodeFilter : nodeFilters) {
            for(RegistryNode node : r.match(nodeFilter)) {
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

    public List<String> getNodeFilters() {
        return nodeFilters;
    }

    public void setNodeFilters(List<String> nodeFilters) {
        if(nodeFilters == null) {
            nodeFilters = new ArrayList<String>();
        }
        this.nodeFilters = nodeFilters;
    }
}
