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
package org.smf4j.core.accumulator;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class StopwatchTest {

    private TestingTimeReporter t = new TestingTimeReporter();
    private Stopwatch w;

    @Before
    public void before() {
        t.set(0);
        w = new Stopwatch(t);
    }

    @Test
    public void startStopTest() {
        t.set(100);
        w.start();
        t.set(500);
        assertEquals(400L, w.end());
    }

    @Test
    public void startLapsStopTest() {
        t.set(100);
        w.start();
        t.set(200);
        assertEquals(100L, w.lap());
        t.set(500);
        assertEquals(300L, w.lap());
        t.set(600);
        assertEquals(500L, w.end());
    }

    @Test
    public void startLapsStopStartStopTest() {
        t.set(100);
        w.start();
        t.set(200);
        assertEquals(100L, w.lap());
        t.set(500);
        assertEquals(300L, w.lap());
        t.set(600);
        assertEquals(500L, w.end());

        t.set(1000);
        w.start();
        t.set(1030);
        assertEquals(30L, w.lap());
        t.set(1100);
        assertEquals(70L, w.lap());
        t.set(1200);
        assertEquals(200L, w.end());
    }

    @Test
    public void badLap() {
        assertEquals(0L, w.lap());
    }

    @Test
    public void badEnd() {
        assertEquals(0L, w.end());
    }

}
