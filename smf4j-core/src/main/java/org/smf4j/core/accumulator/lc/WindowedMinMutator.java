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
public final class WindowedMinMutator extends AbstractWindowedMutator {
    public static final long INITIAL_VALUE = Long.MAX_VALUE;

    public WindowedMinMutator(IntervalStrategy strategy) {
        this(strategy, SystemNanosTimeReporter.INSTANCE);
    }

    public WindowedMinMutator(IntervalStrategy strategy,
            TimeReporter timeReporter) {
        super(INITIAL_VALUE, strategy, timeReporter);
    }

    @Override
    public long combine(long local, long delta) {
        return local <= delta ? local : delta;
    }

    public static final class Factory extends WindowedMutatorFactory {

        public Factory(IntervalStrategy strategy) {
            super(strategy);
        }

        public Factory(IntervalStrategy strategy, TimeReporter timeReporter) {
            super(strategy, timeReporter);
        }

        public Mutator createMutator() {
            return new WindowedMinMutator(getStrategy(), getTimeReporter());
        }

        public long getInitialValue() {
            return INITIAL_VALUE;
        }

        public long combine(long value, Mutator mutator) {
            long other = mutator.get();
            return value <= other ? value : other;
        }
    }
}
