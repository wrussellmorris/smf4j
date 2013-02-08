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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.smf4j.Accumulator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class RangeGroupCalculatorTest {

    private RangeGroupCalculator c;
    private MockAccumulator a;
    private Map<String, Long> vals;
    private Map<String, Accumulator> as;

    @Before
    public void before() {
        c = new RangeGroupCalculator();
        c.setAccumulator("a");
        a = new MockAccumulator();
        as = new HashMap<String, Accumulator>();
        as.put("a", a);
        vals = new HashMap<String, Long>();
    }

    private void set(long val) {
        a.set(val);
        vals.put("a", val);
    }

    @Test
    public void noGroupings() {
        set(123456L);
        assertEquals("123456.00", c.calculate(vals, as));
    }

    @Test
    public void powerTenByteCount() {

        List<RangeGroupCalculator.Grouping> groups =
                new ArrayList<RangeGroupCalculator.Grouping>();
        groups.add(createGroup(" B",1));
        groups.add(createGroup(" KB",1000));
        groups.add(createGroup(" MB",1000000));
        groups.add(createGroup(" GB",1000000000));
        c.setGroupings(groups);

        set(1);
        assertEquals("1.00 B", c.calculate(vals, as));

        set(750);
        assertEquals("750.00 B", c.calculate(vals, as));

        set(900);
        assertEquals("0.90 KB", c.calculate(vals, as));

        set(900000000);
        assertEquals("0.90 GB", c.calculate(vals, as));

        set(900000000000L);
        assertEquals("900.00 GB", c.calculate(vals, as));
    }

    @Test
    public void powerTwoByteCount() {

        List<RangeGroupCalculator.Grouping> groups =
                new ArrayList<RangeGroupCalculator.Grouping>();
        groups.add(createGroup(" B",1));
        groups.add(createGroup(" KiB",1<<10));
        groups.add(createGroup(" MiB",1<<20));
        groups.add(createGroup(" GiB",1<<30));
        c.setGroupings(groups);

        set(1);
        assertEquals("1.00 B", c.calculate(vals, as));

        set(750);
        assertEquals("750.00 B", c.calculate(vals, as));

        set(900);
        assertEquals("0.88 KiB", c.calculate(vals, as));

        set(900000000);
        assertEquals("858.31 MiB", c.calculate(vals, as));

        set(900000000000L);
        assertEquals("838.19 GiB", c.calculate(vals, as));
    }

    private RangeGroupCalculator.Grouping createGroup(String label, long mult) {
        RangeGroupCalculator.Grouping g = new RangeGroupCalculator.Grouping();
        g.setSuffix(label);
        g.setRange(mult);
        return g;
    }
}
