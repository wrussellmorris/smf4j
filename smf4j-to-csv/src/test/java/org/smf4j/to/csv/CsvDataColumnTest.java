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

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.Map;
import org.junit.Test;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class CsvDataColumnTest {

    static class Stub extends CsvDataColumn {
        Stub(RegistryNode node, String dataName) {
            super(node, dataName);
        }

        @Override
        public Object getDatum(Map<String, Object> snapshot) {
            return null;
        }
    }

    @Test
    public void ctorParams() {
        RegistryNode node = createNiceMock(RegistryNode.class);
        String dataName = "";

        try {
            new Stub(node, dataName);
            fail();
        } catch(DataException e) {
            // Success
        } catch(Throwable t) {
            fail("Caught unexpected exception " + t.toString());
        }

        dataName = null;
        try {
            new Stub(node, dataName);
            fail();
        } catch(NullPointerException e) {
            // Success
        } catch(Throwable t) {
            fail("Caught unexpected exception " + t.toString());
        }

        node = null;
        try {
            new Stub(node, dataName);
            fail();
        } catch(NullPointerException e) {
            // Success
        } catch(Throwable t) {
            fail("Caught unexpected exception " + t.toString());
        }
    }

    @Test
    public void getters() {
        RegistryNode node = createNiceMock(RegistryNode.class);
        String dataName = "dataName";

        Stub stub = new Stub(node, dataName);
        assertEquals(dataName, stub.getDataName());
        assertEquals(node, stub.getNode());
        assertFalse(stub.isUseFullName());
    }

    @Test
    public void shortName() {
        RegistryNode node = createNiceMock(RegistryNode.class);
        String dataName = "dataName.data";

        Stub stub = new Stub(node, dataName);
        assertEquals(dataName, stub.getColumnName());
    }

    @Test
    public void fullName() {
        RegistryNode node = createNiceMock(RegistryNode.class);
        String nodeName = "a.node.name";
        String dataName = "dataName.data";

        expect(node.getName()).andStubReturn(nodeName);
        replay(node);

        Stub stub = new Stub(node, dataName);
        stub.setUseFullName(true);
        assertEquals(nodeName + "." + dataName, stub.getColumnName());
    }
}
