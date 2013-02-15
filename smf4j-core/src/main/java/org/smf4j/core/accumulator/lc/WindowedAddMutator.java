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
package org.smf4j.core.accumulator.lc;

import org.smf4j.Mutator;
import org.smf4j.core.accumulator.IntervalStrategy;
import org.smf4j.core.accumulator.SystemNanosTimeReporter;
import org.smf4j.core.accumulator.TimeReporter;
import org.smf4j.core.accumulator.WindowedMutatorFactory;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class WindowedAddMutator extends AbstractWindowedMutator {

    public WindowedAddMutator(IntervalStrategy strategy) {
        this(strategy, SystemNanosTimeReporter.INSTANCE);
    }

    public WindowedAddMutator(IntervalStrategy strategy,
            TimeReporter timeReporter) {
        super(0L, strategy, timeReporter);
    }

    @Override
    public long combine(long local, long delta) {
        return local + delta;
    }

    @Override
    public long combine(long other) {
        return get() + other;
    }

    public static final class Factory extends WindowedMutatorFactory {

        public Factory(IntervalStrategy strategy) {
            super(strategy);
        }

        public Factory(IntervalStrategy strategy, TimeReporter timeReporter) {
            super(strategy, timeReporter);
        }

        public Mutator createMutator() {
            return new WindowedAddMutator(getStrategy(), getTimeReporter());
        }
    }
}
