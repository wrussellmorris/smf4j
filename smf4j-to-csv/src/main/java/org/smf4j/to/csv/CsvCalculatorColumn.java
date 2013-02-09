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
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class CsvCalculatorColumn extends CsvDataColumn {

    private static Logger log =
            LoggerFactory.getLogger(CsvCalculatorColumn.class);

    private final String calcName;
    private final List<String> path;

    public CsvCalculatorColumn(RegistryNode node, String dataName,
            String units) {
        super(node, dataName, units);

        String[] parts = dataName.split("\\.");
        for(String part : parts) {
            if(part.length() == 0) {
                throw new DataException(String.format(
                        "Calculator '%s' of node '%s' has a data path "
                        + "that contains an empty path part.",
                        dataName, node.getName()));
            }
        }

        calcName = parts[0];
        path = new ArrayList<String>();
        for(int i=1; i<parts.length; i++) {
            path.add(parts[i]);
        }

    }

    @Override
    public Object getDatum(Map<String, Object> snapshot) {
        Object calc = snapshot.get(calcName);

        try {
            return walkPath(calc);
        } catch(Throwable t) {
            log.warn("Unabled to determine calcuation value.", t);
            return null;
        }
    }

    protected Object walkPath(Object calc)
    throws IntrospectionException {
        StringBuilder walkedPath = new StringBuilder();
        walkedPath.append(calcName);

        Object cur = calc;
        for(String part : path) {
            if(cur == null) {
                log.info("null value encountered at '{}'", walkedPath);
                return null;
            }

            // Find property getting for this portion of the path
            BeanInfo bi = Introspector.getBeanInfo(cur.getClass());
            Method m = null;
            for(PropertyDescriptor pd : bi.getPropertyDescriptors()) {
                if(!pd.getName().equals(part)) {
                    continue;
                }
                m = pd.getReadMethod();
            }

            if(m == null) {
                throw new DataException(String.format(
                        "Unable to get property read method for %s.[%s]",
                        walkedPath.toString(), part));
            }

            Object val = null;
            try {
                val = m.invoke(cur);
            } catch (Throwable t) {
                throw new DataException(String.format(
                        "Unable to get property %s.[%s]",
                        walkedPath.toString(), part), t);
            }

            // Next!
            walkedPath.append(".");
            walkedPath.append(part);
            cur = val;
        }

        return cur;
    }
}
