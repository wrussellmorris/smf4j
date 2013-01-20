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
package org.smf4j.core.calculator;

import org.smf4j.core.calculator.PowerBaseCalculator;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class PowerBaseCalculatorTest {

    private PowerBaseCalculator c;
    private MockAccumulator a;

    @Before
    public void before() {
        c = new PowerBaseCalculator();
        a = new MockAccumulator();
        //c.setAccumulator(a);
    }

    @Test
    public void noGroupings() {
        a.setValue(123456L);
        //assertEquals("123456.00", c.calculate());
    }

    @Test
    public void powerTenByteCount() {

        List<PowerBaseCalculator.Grouping> groups =
                new ArrayList<PowerBaseCalculator.Grouping>();
        groups.add(createGroup(" B",1));
        groups.add(createGroup(" KB",1000));
        groups.add(createGroup(" MB",1000000));
        groups.add(createGroup(" GB",1000000000));
        c.setGroupings(groups);

        a.setValue(1);
        //assertEquals("1.00 B", c.calculate());

        a.setValue(750);
        //assertEquals("750.00 B", c.calculate());

        a.setValue(900);
        //assertEquals("0.90 KB", c.calculate());

        a.setValue(900000000);
        //assertEquals("0.90 GB", c.calculate());

        a.setValue(900000000000L);
        //assertEquals("900.00 GB", c.calculate());
    }

    @Test
    public void powerTwoByteCount() {

        List<PowerBaseCalculator.Grouping> groups =
                new ArrayList<PowerBaseCalculator.Grouping>();
        groups.add(createGroup(" B",1));
        groups.add(createGroup(" KiB",1<<10));
        groups.add(createGroup(" MiB",1<<20));
        groups.add(createGroup(" GiB",1<<30));
        c.setGroupings(groups);

        a.setValue(1);
        //assertEquals("1.00 B", c.calculate());

        a.setValue(750);
        //assertEquals("750.00 B", c.calculate());

        a.setValue(900);
        //assertEquals("0.88 KiB", c.calculate());

        a.setValue(900000000);
        //assertEquals("858.31 MiB", c.calculate());

        a.setValue(900000000000L);
        //assertEquals("838.19 GiB", c.calculate());
    }

    private PowerBaseCalculator.Grouping createGroup(String label, long mult) {
        PowerBaseCalculator.Grouping g = new PowerBaseCalculator.Grouping();
        g.setLabel(label);
        g.setMultiple(mult);
        return g;
    }
}
