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

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import org.junit.Test;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class CsvAccumulatorColumnTest {
    @Test
    public void ctorParams() {
        RegistryNode node = createNiceMock(RegistryNode.class);
        String dataName = "";

        try {
            new CsvAccumulatorColumn(node, dataName, "");
            fail();
        } catch(DataException e) {
            // Success
        } catch(Throwable t) {
            fail("Caught unexpected exception " + t.toString());
        }

        dataName = "foo.bar";
        try {
            new CsvAccumulatorColumn(node, dataName, "");
            fail();
        } catch(DataException e) {
            // Success
        } catch(Throwable t) {
            fail("Caught unexpected exception " + t.toString());
        }


        dataName = null;
        try {
            new CsvAccumulatorColumn(node, dataName, "");
            fail();
        } catch(NullPointerException e) {
            // Success
        } catch(Throwable t) {
            fail("Caught unexpected exception " + t.toString());
        }

        node = null;
        try {
            new CsvAccumulatorColumn(node, dataName, "");
            fail();
        } catch(NullPointerException e) {
            // Success
        } catch(Throwable t) {
            fail("Caught unexpected exception " + t.toString());
        }
    }

    @Test
    public void datum() {
        RegistryNode node = createNiceMock(RegistryNode.class);
        String dataName = "data";
        Long val = 1234L;

        Map<String, Object> snapshot = new HashMap<String, Object>();
        snapshot.put(dataName, val);

        CsvDataColumn c = new CsvAccumulatorColumn(node, dataName, "");
        assertEquals(val, c.getDatum(snapshot));

        c = new CsvAccumulatorColumn(node, "foo", "");
        assertNull(c.getDatum(snapshot));
    }
}
