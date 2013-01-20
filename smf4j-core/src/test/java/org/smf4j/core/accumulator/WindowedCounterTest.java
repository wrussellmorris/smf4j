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

import org.smf4j.core.accumulator.IntervalStrategy;
import org.smf4j.core.accumulator.WindowedCounter;
import org.smf4j.core.accumulator.WindowedIntervals;
import org.smf4j.core.accumulator.TimeReporter;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;
import static org.smf4j.core.accumulator.TestUtils.*;

import org.junit.Before;
import org.junit.Test;

/**
 * {@code WindowedCounterTest} tests the {@link WindowedCounter} class with
 * the testing-only {@link DerivedDataInstant} type.
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class WindowedCounterTest {
    private TimeReporter t;
    private WindowedIntervals intervals;
    private IntervalStrategy strategy;
    private WindowedCounter counter;

    @Before
    public void before() {
        t = createMock(TimeReporter.class);
        strategy = createMock(IntervalStrategy.class);
        intervals = createMock(WindowedIntervals.class);
        counter = new WindowedCounter(strategy, intervals, t);
    }

    @Test
    public void getTimeWindowTest() {
        expect(strategy.timeWindowInNanos()).andReturn(1234L);
        replay(t, intervals, strategy);
        assertEquals(1234, counter.getTimeWindow());
        verify(t, intervals, strategy);
    }

    @Test
    public void getIntervalsTest() {
        expect(strategy.intervals()).andReturn(12);
        replay(t, intervals, strategy);
        assertEquals(12, counter.getIntervals());
        verify(t, intervals, strategy);
    }

    @Test
    public void getValueOnTest() {
        long[] result = array(1,2,3,4,5);
        long nanos = 0L;

        expect(t.nanos()).andReturn(nanos);
        expect(intervals.buckets(nanos)).andReturn(result);

        replay(t, intervals, strategy);
        counter.setOn(true);
        assertEquals(15, counter.getValue());
        verify(t, intervals, strategy);
    }

    @Test
    public void getValueOffTest() {
        counter.setOn(false);
        replay(t, intervals, strategy);
        assertEquals(0L, counter.getValue());
        verify(t, intervals, strategy);
    }

    @Test
    public void addOnTest() {
        long nanos = 0L;
        long delta = 1234L;

        expect(t.nanos()).andReturn(nanos);
        intervals.incr(nanos,delta);
        expectLastCall();

        replay(t, intervals, strategy);
        counter.setOn(true);
        counter.incr(delta);
        verify(t, intervals, strategy);
    }

    @Test
    public void addOffTest() {
        long delta = 1234L;

        replay(t, intervals, strategy);
        counter.setOn(false);
        counter.incr(delta);
        verify(t, intervals, strategy);
    }

    @Test
    public void incrOnTest() {
        long nanos = 0L;

        expect(t.nanos()).andReturn(nanos);
        intervals.incr(nanos, 1);
        expectLastCall();

        replay(t, intervals, strategy);
        counter.setOn(true);
        counter.incr();
        verify(t, intervals, strategy);
    }

    @Test
    public void incrOffTest() {
        replay(t, intervals, strategy);
        counter.setOn(false);
        counter.incr();
        verify(t, intervals, strategy);
    }
}
