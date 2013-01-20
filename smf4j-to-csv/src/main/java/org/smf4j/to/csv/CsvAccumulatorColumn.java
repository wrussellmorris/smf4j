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
import org.smf4j.Accumulator;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class CsvAccumulatorColumn extends CsvDataColumn {

    public CsvAccumulatorColumn(RegistryNode node, String dataName) {
        super(node, dataName);
        if(dataName.contains(".")) {
            throw new DataException(String.format(
                    "Accumulator name '%s' contains illegal character '.' .",
                    dataName));
        }
    }

    @Override
    public Object getDatum(Map<String, Object> snapshot) {
        Object o = snapshot.get(getDataName());
        if(o == null) {
            // TODO: WARN: Couldn't find value
        }

        return o;
    }
}
