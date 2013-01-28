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
package org.smf4j.core.accumulator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.smf4j.core.accumulator.TestUtils.*;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class IntervalsTest {

    private Intervals intervals;
    private TestingTimeReporter timeReporter;
    private IntervalStrategy strategy;

    @Before
    public void before() {
        timeReporter = new TestingTimeReporter();
        strategy = new SecondsIntervalStrategy(5, 5, 2);
        intervals = new Intervals(strategy, timeReporter);
    }

    @Test
    public void emptyInitially() {
        assertArrayEquals(array(0,0,0,0,0), intervals.buckets(0));
    }

    @Test
    public void emptyWhenAllStale() {
        for(int i=0; i<7; i++) {
            timeReporter.set(timenanos(i));
            intervals.add(1);
        }
        timeReporter.set(timenanos(13));
        assertArrayEquals(array(0,0,0,0,0), intervals.buckets(timenanos(13)));
    }

    @Test
    public void testSkips() {
        timeReporter.set(timenanos(0));
        intervals.add(1);
        assertArrayEquals(array(0,0,0,0,0), intervals.buckets(timenanos(0)));

        timeReporter.set(timenanos(1));
        intervals.add(1);
        assertArrayEquals(array(0,0,0,0,0), intervals.buckets(timenanos(1)));

        timeReporter.set(timenanos(2));
        intervals.add(1);
        assertArrayEquals(array(1,0,0,0,0), intervals.buckets(timenanos(2)));

        timeReporter.set(timenanos(3));
        assertArrayEquals(array(1,1,0,0,0), intervals.buckets(timenanos(3)));

        timeReporter.set(timenanos(4));
        assertArrayEquals(array(1,1,1,0,0), intervals.buckets(timenanos(4)));

        timeReporter.set(timenanos(5));
        assertArrayEquals(array(0,1,1,1,0), intervals.buckets(timenanos(5)));

        timeReporter.set(timenanos(6));
        assertArrayEquals(array(0,0,1,1,1), intervals.buckets(timenanos(6)));

        timeReporter.set(timenanos(7));
        assertArrayEquals(array(0,0,0,1,1), intervals.buckets(timenanos(7)));

        timeReporter.set(timenanos(8));
        assertArrayEquals(array(0,0,0,0,1), intervals.buckets(timenanos(8)));

        timeReporter.set(timenanos(9));
        assertArrayEquals(array(0,0,0,0,0), intervals.buckets(timenanos(9)));

        timeReporter.set(timenanos(10));
        assertArrayEquals(array(0,0,0,0,0), intervals.buckets(timenanos(10)));
    }

    @Test
    public void testWindowFalloff() {
        timeReporter.set(timenanos(0));
        intervals.add(1);
        assertArrayEquals(array(0,0,0,0,0), intervals.buckets(timenanos(0)));

        timeReporter.set(timenanos(1));
        intervals.add(2);
        assertArrayEquals(array(0,0,0,0,0), intervals.buckets(timenanos(1)));

        timeReporter.set(timenanos(2));
        intervals.add(3);
        assertArrayEquals(array(1,0,0,0,0), intervals.buckets(timenanos(2)));

        timeReporter.set(timenanos(3));
        intervals.add(4);
        assertArrayEquals(array(2,1,0,0,0), intervals.buckets(timenanos(3)));

        timeReporter.set(timenanos(4));
        intervals.add(5);
        assertArrayEquals(array(3,2,1,0,0), intervals.buckets(timenanos(4)));

        timeReporter.set(timenanos(5));
        intervals.add(6);
        assertArrayEquals(array(4,3,2,1,0), intervals.buckets(timenanos(5)));

        timeReporter.set(timenanos(6));
        intervals.add(7);
        assertArrayEquals(array(5,4,3,2,1), intervals.buckets(timenanos(6)));

        timeReporter.set(timenanos(7));
        intervals.add(8);
        assertArrayEquals(array(6,5,4,3,2), intervals.buckets(timenanos(7)));

        timeReporter.set(timenanos(8));
        intervals.add(9);
        assertArrayEquals(array(7,6,5,4,3), intervals.buckets(timenanos(8)));

        timeReporter.set(timenanos(9));
        intervals.add(10);
        assertArrayEquals(array(8,7,6,5,4), intervals.buckets(timenanos(9)));

        timeReporter.set(timenanos(10));
        intervals.add(11);
        assertArrayEquals(array(9,8,7,6,5), intervals.buckets(timenanos(10)));

        timeReporter.set(timenanos(11));
        assertArrayEquals(array(10,9,8,7,6), intervals.buckets(timenanos(11)));

        timeReporter.set(timenanos(12));
        assertArrayEquals(array(11,10,9,8,7), intervals.buckets(timenanos(12)));

        timeReporter.set(timenanos(13));
        assertArrayEquals(array(0,11,10,9,8), intervals.buckets(timenanos(13)));

        timeReporter.set(timenanos(14));
        assertArrayEquals(array(0,0,11,10,9), intervals.buckets(timenanos(14)));

        timeReporter.set(timenanos(15));
        assertArrayEquals(array(0,0,0,11,10), intervals.buckets(timenanos(15)));

        timeReporter.set(timenanos(16));
        assertArrayEquals(array(0,0,0,0,11), intervals.buckets(timenanos(16)));

        timeReporter.set(timenanos(17));
        assertArrayEquals(array(0,0,0,0,0), intervals.buckets(timenanos(17)));
    }

}
