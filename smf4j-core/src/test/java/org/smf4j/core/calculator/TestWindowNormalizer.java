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

import org.smf4j.core.calculator.WindowNormalizer;
import static org.junit.Assert.*;

import org.junit.Test;
import org.smf4j.core.accumulator.WindowedCounter;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class TestWindowNormalizer {

    private final double epsilon = 0.0000001d;

    @Test
    public void basic() {
        WindowNormalizer w = new WindowNormalizer();
        w.setFrequency(WindowNormalizer.Frequency.MILLIS);
        //assertEquals(0.0d, w.calc(0.0d, 2000000000.0d), epsilon);
        //assertEquals(0.001d, w.calc(2.0d, 2000000000.0d), epsilon);
        //assertEquals(0.0005d, w.calc(1.0d, 2000000000.0d), epsilon);
        //assertEquals(0.002d, w.calc(4.0d, 2000000000.0d), epsilon);

        w.setFrequency(WindowNormalizer.Frequency.SECONDS);
        //assertEquals(0.0d, w.calc(0.0d, 2000000000.0d), epsilon);
        //assertEquals(1.0d, w.calc(2.0d, 2000000000.0d), epsilon);
        //assertEquals(0.5d, w.calc(1.0d, 2000000000.0d), epsilon);
        //assertEquals(2.0d, w.calc(4.0d, 2000000000.0d), epsilon);

        w.setFrequency(WindowNormalizer.Frequency.MINUTES);
        //assertEquals(60.0d, w.calc(1.0d, 1000000000.0d), epsilon);

        w.setFrequency(WindowNormalizer.Frequency.HOURS);
        //assertEquals(3600.0d, w.calc(1.0d, 1000000000.0d), epsilon);

        w.setFrequency(WindowNormalizer.Frequency.DAYS);
        //assertEquals(86400.0d, w.calc(1.0d, 1000000000.0d), epsilon);
    }
}
