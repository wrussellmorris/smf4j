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

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.smf4j.RegistryNode;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class CsvCalculatorColumnTest {
    @Test
    public void ctorParams() {
        RegistryNode node = createNiceMock(RegistryNode.class);
        String dataName = "";

        try {
            new CsvCalculatorColumn(node, dataName);
            fail();
        } catch(DataException e) {
            // Success
        } catch(Throwable t) {
            fail("Caught unexpected exception " + t.toString());
        }

        dataName = "foo.bar";
        try {
            new CsvCalculatorColumn(node, dataName);
        } catch(Throwable t) {
            fail("Caught unexpected exception " + t.toString());
        }


        dataName = null;
        try {
            new CsvCalculatorColumn(node, dataName);
            fail();
        } catch(NullPointerException e) {
            // Success
        } catch(Throwable t) {
            fail("Caught unexpected exception " + t.toString());
        }

        node = null;
        try {
            new CsvCalculatorColumn(node, dataName);
            fail();
        } catch(NullPointerException e) {
            // Success
        } catch(Throwable t) {
            fail("Caught unexpected exception " + t.toString());
        }
    }

    @Test
    public void datumValid() {
        RegistryNode node = createNiceMock(RegistryNode.class);
        Composite smiley = getSmileyShape();

        Map<String, Object> snapshot = new HashMap<String, Object>();
        snapshot.put("smiley", smiley);

        // Grab the root object
        String dataName = "smiley";
        CsvDataColumn c = new CsvCalculatorColumn(node, dataName);
        assertEquals(smiley, c.getDatum(snapshot));

        // Grab property
        dataName = "smiley.name";
        c = new CsvCalculatorColumn(node, dataName);
        assertEquals("Smiley", c.getDatum(snapshot));

        // Grab property of property
        dataName = "smiley.shape1.name";
        c = new CsvCalculatorColumn(node, dataName);
        assertEquals("Eyes", c.getDatum(snapshot));

        // Grab property of property of property
        dataName = "smiley.shape1.shape1.name";
        c = new CsvCalculatorColumn(node, dataName);
        assertEquals("Left", c.getDatum(snapshot));

        // Non-existant property
        dataName = "smiley.shape1.foo";
        c = new CsvCalculatorColumn(node, dataName);
        assertNull(c.getDatum(snapshot));
    }

    Composite getSmileyShape() {
        Composite smiley = new Composite();
        smiley.setName("Smiley");

        Composite eyes = new Composite();
        eyes.setName("Eyes");

        Shape left = new Shape();
        left.setName("Left");

        Shape right = new Shape();
        right.setName("Right");

        Shape mouth = new Shape();
        mouth.setName("Mouth");

        eyes.setShape1(left);
        eyes.setShape2(right);

        smiley.setShape1(eyes);
        smiley.setShape2(mouth);

        return smiley;
    }

    static class Shape {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    static class Composite extends Shape {
        private Shape shape1;
        private Shape shape2;

        public Shape getShape1() { return shape1; }
        public void setShape1(Shape shape1) { this.shape1 = shape1; }
        public Shape getShape2() { return shape2; }
        public void setShape2(Shape shape2) { this.shape2 = shape2; }
    }
}
