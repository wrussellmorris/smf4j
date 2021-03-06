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

import java.util.Map;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public abstract class CsvDataColumn {

    private final RegistryNode node;
    private final String dataName;
    private final String units;
    private boolean useFullName;
    private String shortName;
    private String fullName;

    CsvDataColumn(RegistryNode node, String dataName, String units) {
        if (node == null) {
            throw new NullPointerException("node");
        }
        if (dataName == null) {
            throw new NullPointerException("dataName");
        }
        if (dataName.length() == 0) {
            throw new DataException("dataName cannot be zero-length");
        }

        this.node = node;
        this.dataName = dataName;
        this.units = units;
    }

    public RegistryNode getNode() {
        return node;
    }

    public String getDataName() {
        return dataName;
    }

    public String getColumnName() {
        return useFullName ? getFullName() : getShortName();
    }

    public boolean isUseFullName() {
        return useFullName;
    }

    public void setUseFullName(boolean useFullName) {
        this.useFullName = useFullName;
    }

    public abstract Object getDatum(Map<String, Object> snapshot);

    protected String getFullName() {
        if (fullName == null) {
            fullName = computeFullName();
            String u = getUnits();
            if (u != null && !u.equals("")) {
                fullName += " (" + u + ")";
            }
        }
        return fullName;
    }

    protected String getShortName() {
        if (shortName == null) {
            shortName = computeShortName();
            String u = getUnits();
            if (u != null && !u.equals("")) {
                shortName += " (" + u + ")";
            }
        }
        return shortName;
    }

    protected String computeFullName() {
        return node.getName() + "." + dataName;
    }

    protected String computeShortName() {
        return dataName;
    }

    public String getUnits() {
        if (units == null) {
            return "";
        }
        return units;
    }
}
