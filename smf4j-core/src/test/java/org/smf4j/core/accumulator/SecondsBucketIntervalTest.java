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

import org.smf4j.core.accumulator.SecondsIntervalStrategy;
import org.smf4j.core.accumulator.IntervalStrategy;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class SecondsBucketIntervalTest {

    private SecondsIntervalStrategy create(int window, int intervals, int buf) {
        return new SecondsIntervalStrategy(window, intervals, buf);
    }

    @Test
    public void initSimple() {
        int window = 10;
        int intervals = 5;
        int buffer = 2;
        SecondsIntervalStrategy interval = create(window, intervals, buffer);

        long windowInNanos = ((long)window) * SecondsIntervalStrategy.ONE_BILLION;
        assertEquals(windowInNanos, interval.timeWindowInNanos());
        assertEquals(intervals, interval.intervals());
        assertEquals(windowInNanos / ((long)intervals),
                interval.intervalResolutionInNanos());
        assertEquals(buffer, interval.bufferIntervals());
    }

    @Test
    public void initWindowExpand() {
        int window = 10;
        int expandedWindow = 12;
        int intervals = 6;
        int buffer = 2;
        SecondsIntervalStrategy interval = create(window, intervals, buffer);

        long windowInNanos = ((long)expandedWindow) *
                SecondsIntervalStrategy.ONE_BILLION;
        assertEquals(windowInNanos, interval.timeWindowInNanos());
        assertEquals(intervals, interval.intervals());
        assertEquals(windowInNanos / ((long)intervals),
                interval.intervalResolutionInNanos());
        assertEquals(buffer, interval.bufferIntervals());
    }

    @Test( expected = IllegalArgumentException.class )
    public void initZeroWindow() {
        create(0, 1, 1);
    }

    @Test( expected = IllegalArgumentException.class )
    public void initNegWindow() {
        create(-1, 1, 1);
    }

    @Test( expected = IllegalArgumentException.class )
    public void initTooLargeIntervals() {
        create(1, SecondsIntervalStrategy.MAX_INTERVALS + 1, 1);
    }

    @Test( expected = IllegalArgumentException.class )
    public void initZeroIntervals() {
        create(1, 0, 1);
    }

    @Test( expected = IllegalArgumentException.class )
    public void initNegIntervals() {
        create(1, -1, 1);
    }

    @Test( expected = IllegalArgumentException.class )
    public void initTooLargeBuffers() {
        create(1, 1, SecondsIntervalStrategy.MAX_BUFFER_INTERVALS + 1);
    }

    @Test( expected = IllegalArgumentException.class )
    public void initNegBuffers() {
        create(1, 1, -1);
    }

    @Test
    public void indexWithoutBuffers() {
        IntervalStrategy bi = create(10, 10, 0);

        for(int i=0; i<20; i++) {
            assertEquals(i % 10, bi.intervalIndex(nanos(i)));
        }
    }

    @Test
    public void indexWithBuffers() {
        IntervalStrategy bi = create(10, 10, 2);

        for(int i=0; i<24; i++) {
            assertEquals(i % 12, bi.intervalIndex(nanos(i)));
        }
    }

    private long nanos(int seconds) {
        return SecondsIntervalStrategy.ONE_BILLION * (long)seconds;
    }
}
