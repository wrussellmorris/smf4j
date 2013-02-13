/*
 * Copyright 2013 Russell Morris (wrussellmorris@gmail.com).
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

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.smf4j.Accumulator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class RatioTest {
    private Ratio c;
    private MockAccumulator n;
    private MockAccumulator d;
    private Map<String, Long> vals;
    private Map<String, Accumulator> as;

    @Before
    public void before() {
        c = new Ratio();
        c.setNumerator("n");
        assertEquals("n", c.getNumerator());
        c.setDenominator("d");
        assertEquals("d", c.getDenominator());
        c.setUnits("u");
        assertEquals("u", c.getUnits());

        n = new MockAccumulator();
        d = new MockAccumulator();
        as = new HashMap<String, Accumulator>();
        as.put("n", n);
        as.put("d", d);
        vals = new HashMap<String, Long>();
    }

    void set(Long numerator, Long denominator) {
        if(numerator != null) {
            n.set(numerator);
            vals.put("n", numerator);
        }
        if(denominator != null) {
            d.set(denominator);
            vals.put("d", denominator);
        }
    }

    @Test
    public void bothNull() {
        set(null, null);
        assertEquals(0.0d, c.calculate(vals, as), 0.0000001d);
    }

    @Test
    public void numeratorNull() {
        set(null, 1L);
        assertEquals(0.0d, c.calculate(vals, as), 0.0000001d);
    }

    @Test
    public void denominatorNull() {
        set(1L, null);
        assertEquals(0.0d, c.calculate(vals, as), 0.0000001d);
    }

    @Test
    public void normal() {
        set(1L, 2L);
        assertEquals(0.5d, c.calculate(vals, as), 0.0000001d);
    }

    @Test
    public void units() {
        assertEquals("u", c.getUnits());
    }
}
