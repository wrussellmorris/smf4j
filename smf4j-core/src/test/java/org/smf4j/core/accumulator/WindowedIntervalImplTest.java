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

import org.smf4j.core.accumulator.WindowedIntervalsImpl;
import org.smf4j.core.accumulator.IntervalStrategy;
import static org.junit.Assert.*;
import org.junit.Before;
import static org.smf4j.core.accumulator.TestUtils.*;

import org.junit.Test;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class WindowedIntervalImplTest {

    private volatile TestingBucketInterval interval;
    private volatile WindowedIntervalsImpl storage;

    protected TestingBucketInterval createInterval(int intervals,
            int bufferIntervals, long timeWindowInNanos,
            long intervalResolutionInNanos) {
        return new TestingBucketInterval(intervals, bufferIntervals,
                timeWindowInNanos, intervalResolutionInNanos);
    }

    private WindowedIntervalsImpl createStorage(IntervalStrategy strategy) {
        return new WindowedIntervalsImpl(strategy);
    }

    @Before
    public void before() {
        interval = createInterval(5, 2, 5, 1);
        storage = createStorage(interval);
    }

    @Test
    public void emptyInitially() {
        assertArrayEquals(array(0,0,0,0,0), storage.buckets(0));
    }

    @Test
    public void emptyWhenAllStale() {
        for(int i=0; i<7; i++) {
            interval.setIndex(i);
            storage.incr(i, 1);
        }

        assertArrayEquals(array(0,0,0,0,0), storage.buckets(12));
    }

    @Test
    public void testSkips() {
        interval.setIndex(0);
        storage.incr(0, 1);
        assertArrayEquals(array(0,0,0,0,0), storage.buckets(0));

        interval.setIndex(1);
        storage.incr(1, 1);
        assertArrayEquals(array(0,0,0,0,0), storage.buckets(1));

        interval.setIndex(2);
        storage.incr(2, 1);
        assertArrayEquals(array(1,0,0,0,0), storage.buckets(2));

        interval.setIndex(3);
        assertArrayEquals(array(1,1,0,0,0), storage.buckets(3));

        interval.setIndex(4);
        assertArrayEquals(array(1,1,1,0,0), storage.buckets(4));

        interval.setIndex(5);
        assertArrayEquals(array(0,1,1,1,0), storage.buckets(5));

        interval.setIndex(6);
        assertArrayEquals(array(0,0,1,1,1), storage.buckets(6));

        interval.setIndex(0);
        assertArrayEquals(array(0,0,0,1,1), storage.buckets(7));

        interval.setIndex(1);
        assertArrayEquals(array(0,0,0,0,1), storage.buckets(8));

        interval.setIndex(2);
        assertArrayEquals(array(0,0,0,0,0), storage.buckets(9));

        interval.setIndex(3);
        assertArrayEquals(array(0,0,0,0,0), storage.buckets(10));
    }

    @Test
    public void testWindowFalloff() {
        interval.setIndex(0);
        storage.incr(0, 1);
        assertArrayEquals(array(0,0,0,0,0), storage.buckets(0));

        interval.setIndex(1);
        storage.incr(1, 2);
        assertArrayEquals(array(0,0,0,0,0), storage.buckets(1));

        interval.setIndex(2);
        storage.incr(2, 3);
        assertArrayEquals(array(1,0,0,0,0), storage.buckets(2));

        interval.setIndex(3);
        storage.incr(3, 4);
        assertArrayEquals(array(2,1,0,0,0), storage.buckets(3));

        interval.setIndex(4);
        storage.incr(4, 5);
        assertArrayEquals(array(3,2,1,0,0), storage.buckets(4));

        interval.setIndex(5);
        storage.incr(5, 6);
        assertArrayEquals(array(4,3,2,1,0), storage.buckets(5));

        interval.setIndex(6);
        storage.incr(6, 7);
        assertArrayEquals(array(5,4,3,2,1), storage.buckets(6));

        interval.setIndex(0);
        storage.incr(7, 8);
        assertArrayEquals(array(6,5,4,3,2), storage.buckets(7));

        interval.setIndex(1);
        storage.incr(8, 9);
        assertArrayEquals(array(7,6,5,4,3), storage.buckets(8));

        interval.setIndex(2);
        storage.incr(9, 10);
        assertArrayEquals(array(8,7,6,5,4), storage.buckets(9));

        interval.setIndex(3);
        storage.incr(10, 11);
        assertArrayEquals(array(9,8,7,6,5), storage.buckets(10));

        interval.setIndex(4);
        assertArrayEquals(array(10,9,8,7,6), storage.buckets(11));

        interval.setIndex(5);
        assertArrayEquals(array(11,10,9,8,7), storage.buckets(12));

        interval.setIndex(6);
        assertArrayEquals(array(0,11,10,9,8), storage.buckets(13));

        interval.setIndex(0);
        assertArrayEquals(array(0,0,11,10,9), storage.buckets(14));

        interval.setIndex(1);
        assertArrayEquals(array(0,0,0,11,10), storage.buckets(15));

        interval.setIndex(2);
        assertArrayEquals(array(0,0,0,0,11), storage.buckets(16));

        interval.setIndex(3);
        assertArrayEquals(array(0,0,0,0,0), storage.buckets(17));
    }
}
