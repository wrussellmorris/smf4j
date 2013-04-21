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
import org.smf4j.core.accumulator.PowersOfTwoIntervalStrategy;
import org.smf4j.core.accumulator.SecondsIntervalStrategy;
import org.smf4j.core.accumulator.SystemNanosTimeReporter;
import org.smf4j.core.accumulator.TimeReporter;
import org.smf4j.core.accumulator.WindowedMutatorFactory;

/**
 * {@code WindowedAddMutator} is windowed {@code Mutator} that reports the sum
 * of all values provided to it over a given time interval.
 * <p>
 * The time span and resolution of the time interval are given during
 * construction via an implementation of {@link IntervalStrategy}.
 * </p>
 * <p>
 * This version does not implement any read-modify-write semantics to its
 * internal value, and as such should only be used in concert with
 * {@link MutatorRegistry}.
 * </p>
 *
 * @see IntervalStrategy
 * @see SecondsIntervalStrategy
 * @see PowersOfTwoIntervalStrategy
 * @see MutatorRegistry
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class WindowedAddMutator extends AbstractWindowedMutator {
    /**
     * The initial value of an {@code WindowedAddMutator} - {@code 0}.
     */
    public static final long INITIAL_VALUE = 0L;

    /**
     * Creates an instance of {@code WindowedAddMutator} using the given
     * {@link IntervalStrategy}.
     * @param strategy The {@code IntervalStrategy} used to manage the
     *                 time window for this instance.
     */
    public WindowedAddMutator(IntervalStrategy strategy) {
        this(strategy, SystemNanosTimeReporter.INSTANCE);
    }

    /**
     * Creates an instance of {@code WindowedAddMutator} using the given
     * {@link IntervalStrategy}.
     * <p>
     * This constructor exists largely for testing purposes.
     * </p>
     * @param strategy The {@code IntervalStrategy} used to manage the
     *                 time window for this instance.
     * @param timeReporter The {@code TimeReporter} used to get the current
     *                     system time.
     */
    public WindowedAddMutator(IntervalStrategy strategy,
            TimeReporter timeReporter) {
        super(INITIAL_VALUE, strategy, timeReporter);
    }

    @Override
    public long combine(long local, long delta) {
        return local + delta;
    }

    /**
     * {@code WindowedAddMutator.Factory} is an instance of
     * {@code MutatorFactory} that can create instances of
     * {@code WindowedAddMutator}.
     */
    public static final class Factory extends WindowedMutatorFactory {

        /**
         * Creates a {@code WindowedAddMutator.Factory} instance that uses
         * the given {@code strategy} to create instances of
         * {@code WindowedAddMutator}.
         * @param strategy The {@code IntervalStrategy} used when creating
         *                 instances of {@code WindowedAddMutator}.
         */
        public Factory(IntervalStrategy strategy) {
            super(strategy);
        }

        /**
         * Creates a {@code WindowedAddMutator.Factory} instance that uses
         * the given {@code strategy} and {@code timeReporter} to create
         * instances of {@code WindowedAddMutator}.
         * <p>
         * This constructor exists largely for testing purposes.
         * </p>
         * @param strategy The {@code IntervalStrategy} used when creating
         *                 instances of {@code WindowedAddMutator}.
         * @param timeReporter The {@code TimeReporter} used to get the current
         *                     system time.
         */
        public Factory(IntervalStrategy strategy, TimeReporter timeReporter) {
            super(strategy, timeReporter);
        }

        public Mutator createMutator() {
            return new WindowedAddMutator(getStrategy(), getTimeReporter());
        }

        public long getInitialValue() {
            return INITIAL_VALUE;
        }

        public long combine(long value, Mutator mutator) {
            return value + mutator.get();
        }
    }
}
