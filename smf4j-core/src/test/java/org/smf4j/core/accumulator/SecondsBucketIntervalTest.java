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
import org.junit.Test;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class SecondsBucketIntervalTest {

    private SecondsIntervalStrategy create(int window, int intervals) {
        return new SecondsIntervalStrategy(window, intervals);
    }

    @Test
    public void initWithBufferIntervals() {
        int window = 1;
        int intervals = 5;
        SecondsIntervalStrategy interval = create(window, intervals);

        long windowInNanos = ((long)window) * 1000000000L;
        assertEquals(windowInNanos, interval.timeWindowInNanos());
        assertEquals(intervals, interval.intervals());
        assertEquals(2, interval.bufferIntervals());
        assertEquals(windowInNanos / ((long)intervals),
                interval.intervalResolutionInNanos());
    }

    @Test
    public void initWithoutBufferIntervals() {
        int window = 10;
        int intervals = 5;
        SecondsIntervalStrategy interval = create(window, intervals);

        long windowInNanos = ((long)window) * 1000000000L;
        assertEquals(windowInNanos, interval.timeWindowInNanos());
        assertEquals(intervals, interval.intervals());
        assertEquals(0, interval.bufferIntervals());
        assertEquals(windowInNanos / ((long)intervals),
                interval.intervalResolutionInNanos());
    }

    @Test( expected = IllegalArgumentException.class )
    public void initZeroWindow() {
        create(0, 1);
    }

    @Test( expected = IllegalArgumentException.class )
    public void initNegWindow() {
        create(-1, 1);
    }

    @Test( expected = IllegalArgumentException.class )
    public void initTooLargeIntervals() {
        create(1, SecondsIntervalStrategy.MAX_INTERVALS + 1);
    }

    @Test( expected = IllegalArgumentException.class )
    public void initZeroIntervals() {
        create(1, 0);
    }

    @Test( expected = IllegalArgumentException.class )
    public void initNegIntervals() {
        create(1, -1);
    }

    @Test
    public void index() {
        IntervalStrategy bi = create(10, 10);

        for(int i=0; i<24; i++) {
            assertEquals(i % 12, bi.intervalIndex(nanos(i)));
        }
    }

    private long nanos(int seconds) {
        return 1000000000L * (long)seconds;
    }
}
