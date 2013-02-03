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
public class TestWindowNormalizer {

    private static final double epsilon = 0.0000001d;
    private MockAccumulator a;
    private WindowNormalizer n;
    private Map<String, Long> vals;
    private Map<String, Accumulator> as;

    @Before
    public void before() {
        a = new MockAccumulator();
        a.setTimeWindow(1000000000L);
        a.setIntervals(1);
        n = new WindowNormalizer();
        n.setWindowedCounter("a");
        as = new HashMap<String, Accumulator>();
        as.put("a", a);
        vals = new HashMap<String, Long>();
    }

    private void set(long val) {
        vals.put("a", val);
    }

    @Test
    public void nanos() {
        n.setFrequency(WindowNormalizer.Frequency.NANOS);

        set(0L);
        assertEquals(0.0d, n.calculate(vals, as), epsilon);

        set(1000000L);
        assertEquals(0.001d, n.calculate(vals, as), epsilon);

        set(5000000000L);
        assertEquals(5.0d, n.calculate(vals, as), epsilon);

        set(123456789L);
        assertEquals(0.123456789d, n.calculate(vals, as), epsilon);
    }

    @Test
    public void millis() {
        n.setFrequency(WindowNormalizer.Frequency.MILLIS);

        set(0L);
        assertEquals(0.0d, n.calculate(vals, as), epsilon);

        set(1000000L);
        assertEquals(1000.0d, n.calculate(vals, as), epsilon);

        set(5000000000L);
        assertEquals(5000000.0d, n.calculate(vals, as), epsilon);

        set(123456789L);
        assertEquals(123456.789d, n.calculate(vals, as), epsilon);
    }


    @Test
    public void seconds() {
        n.setFrequency(WindowNormalizer.Frequency.SECONDS);

        set(0L);
        assertEquals(0.0d, n.calculate(vals, as), epsilon);

        set(1000000L);
        assertEquals(1000000.0d, n.calculate(vals, as), epsilon);

        set(5000000000L);
        assertEquals(5000000000.0d, n.calculate(vals, as), epsilon);

        set(123456789L);
        assertEquals(123456789.0d, n.calculate(vals, as), epsilon);
    }

    @Test
    public void minutes() {
        n.setFrequency(WindowNormalizer.Frequency.MINUTES);

        set(0L);
        assertEquals(0.0d, n.calculate(vals, as), epsilon);

        set(1000000L);
        assertEquals(1000000.0d * 60.0d, n.calculate(vals, as), epsilon);

        set(5000000000L);
        assertEquals(5000000000.0d * 60.0d, n.calculate(vals, as), epsilon);

        set(123456789L);
        assertEquals(123456789.0d * 60.0d, n.calculate(vals, as), epsilon);
    }

    @Test
    public void hours() {
        n.setFrequency(WindowNormalizer.Frequency.HOURS);

        set(0L);
        assertEquals(0.0d, n.calculate(vals, as), epsilon);

        set(1000000L);
        assertEquals(1000000.0d * 60.0d * 60.0d, n.calculate(vals, as), epsilon);

        set(5000000000L);
        assertEquals(5000000000.0d * 60.0d * 60.0d, n.calculate(vals, as), epsilon);

        set(123456789L);
        assertEquals(123456789.0d * 60.0d * 60.0d, n.calculate(vals, as), epsilon);
    }

    @Test
    public void days() {
        n.setFrequency(WindowNormalizer.Frequency.DAYS);

        set(0L);
        assertEquals(0.0d, n.calculate(vals, as), epsilon);

        set(1000000L);
        assertEquals(1000000.0d * 60.0d * 60.0d * 24.0d, n.calculate(vals, as), epsilon);

        set(5000000000L);
        assertEquals(5000000000.0d * 60.0d * 60.0d * 24.0d, n.calculate(vals, as), epsilon);

        set(123456789L);
        assertEquals(123456789.0d * 60.0d * 60.0d * 24.0d, n.calculate(vals, as), epsilon);
    }

    @Test
    public void weeks() {
        n.setFrequency(WindowNormalizer.Frequency.WEEKS);

        set(0L);
        assertEquals(0.0d, n.calculate(vals, as), epsilon);

        set(1000000L);
        assertEquals(1000000.0d * 60.0d * 60.0d * 24.0d * 7.0d, n.calculate(vals, as), epsilon);

        set(5000000000L);
        assertEquals(5000000000.0d * 60.0d * 60.0d * 24.0d * 7.0d, n.calculate(vals, as), epsilon);

        set(123456789L);
        assertEquals(123456789.0d * 60.0d * 60.0d * 24.0d * 7.0d, n.calculate(vals, as), epsilon);
    }
}
