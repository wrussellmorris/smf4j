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
package org.smf4j.core.accumulator.hc;

import org.smf4j.Mutator;
import org.smf4j.core.accumulator.IntervalStrategy;
import org.smf4j.core.accumulator.MutatorFactory;
import org.smf4j.core.accumulator.SystemNanosTimeReporter;
import org.smf4j.core.accumulator.TimeReporter;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class WindowedMaxMutator extends AbstractWindowedMutator {

    public WindowedMaxMutator(IntervalStrategy strategy) {
        this(strategy, SystemNanosTimeReporter.INSTANCE);
    }

    public WindowedMaxMutator(IntervalStrategy strategy,
            TimeReporter timeReporter) {
        super(Long.MIN_VALUE, strategy, timeReporter);
    }

    @Override
    public long combine(long local, long delta) {
        return local >= delta ? local : delta;
    }

    public long combine(long other) {
        long val = get();
        return val >= other ? val : other;
    }

    public static final class Factory implements MutatorFactory {
        private final IntervalStrategy strategy;
        private final TimeReporter timeReporter;

        public Factory(IntervalStrategy strategy) {
            this(strategy, SystemNanosTimeReporter.INSTANCE);
        }

        public Factory(IntervalStrategy strategy, TimeReporter timeReporter) {
            this.strategy = strategy;
            this.timeReporter = timeReporter;
        }

        public Mutator createMutator() {
            return new WindowedMaxMutator(strategy, timeReporter);
        }

        public long getTimeWindow() {
            return strategy.timeWindowInNanos();
        }

        public int getIntervals() {
            return strategy.intervals();
        }
    }
}
