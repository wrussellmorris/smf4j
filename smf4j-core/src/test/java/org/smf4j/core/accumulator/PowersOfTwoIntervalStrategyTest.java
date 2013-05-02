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
public class PowersOfTwoIntervalStrategyTest {

    private IntervalStrategy create(int windowExp, int intervalExp) {
        return new PowersOfTwoIntervalStrategy(windowExp, intervalExp);
    }

    @Test
    public void initWithoutBufferIntervals() {
        int windowExp = 34;
        int intervalExp = 4;
        IntervalStrategy interval = create(windowExp, intervalExp);

        long expectedIntervalRes = 1L<<(windowExp-intervalExp);
        long expectedIntervals = 1L<<(intervalExp);
        int expectedBufferIntervals = 0;
        long expectedWindow = expectedIntervalRes * expectedIntervals;

        assertEquals(expectedWindow, interval.timeWindowInNanos());
        assertEquals(expectedIntervalRes, interval.intervalResolutionInNanos());
        assertEquals(expectedIntervals, interval.intervals());
        assertEquals(expectedBufferIntervals, interval.bufferIntervals());

    }

    @Test
    public void initWithBufferIntervals() {
        int windowExp = 10;
        int intervalExp = 4;
        IntervalStrategy interval = create(windowExp, intervalExp);

        long expectedIntervalRes = 64;
        int expectedIntervals = 14;
        int expectedBufferIntervals = 2;
        long expectedWindow =
                1024 - (expectedBufferIntervals * expectedIntervalRes);

        assertEquals(expectedWindow, interval.timeWindowInNanos());
        assertEquals(expectedIntervalRes, interval.intervalResolutionInNanos());
        assertEquals(expectedIntervals, interval.intervals());
        assertEquals(expectedBufferIntervals, interval.bufferIntervals());

    }

    @Test( expected = IllegalArgumentException.class )
    public void initZeroWindowExp() {
        create(0, 1);
    }

    @Test( expected = IllegalArgumentException.class )
    public void initNegWindowExp() {
        create(-1, 1);
    }

    @Test( expected = IllegalArgumentException.class )
    public void initTooSmallIntervalExp() {
        create(1, 1);
    }

    @Test( expected = IllegalArgumentException.class )
    public void initTooLargeIntervalExp() {
        create(1, PowersOfTwoIntervalStrategy.MAX_NUM_INTERVALS_EXP + 1);
    }

    @Test( expected = IllegalArgumentException.class )
    public void initNegIntervalExp() {
        create(1, -1);
    }

    @Test( expected = IllegalArgumentException.class )
    public void initTooManyIntervals() {
        create(10, 10);
    }

    @Test
    public void index() {
        IntervalStrategy bi = create(10, 4);

        long intervalRes = bi.intervalResolutionInNanos();
        long windowRes = bi.timeWindowInNanos();

        int i=0;
        for(long t=0; t<(5*windowRes); t+=intervalRes) {
            assertEquals(i, bi.intervalIndex(t));
            if(i < 15) {
                i++;
            } else {
                i = 0;
            }
        }
    }
}
