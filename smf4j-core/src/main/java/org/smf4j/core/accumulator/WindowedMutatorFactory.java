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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.smf4j.Mutator;

/**
 * {@code WindowedMutatorFactory} is a base class for all
 * {@link MutatorFactory} implementations that create <em>windowed</em>
 * {@link Mutator}s.
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public abstract class WindowedMutatorFactory extends AbstractMutatorFactory {
    /**
     * The {@code IntervalStrategy} used for all {@link Mutator}s created by
     * this {@code WindowedMutatorFactory}.
     */
    private final IntervalStrategy strategy;

    /**
     * The {@code TimeReporter} used to report the current time.
     */
    private final TimeReporter timeReporter;

    /**
     * The metadata associated with each {@link Mutator} created by this
     * {@code WindowedMutatorFactory}.
     */
    private final Map<Object, Object> metadata;

    /**
     * Creates an instance of {@code WindowedMutatorFactory}, using the given
     * {@code strategy} for all created {@link Mutator}s.
     * @param strategy The {@code IntervalStrategy} to use for all created
     *                 {@link Mutator}s.
     */
    public WindowedMutatorFactory(IntervalStrategy strategy) {
        this(strategy, SystemNanosTimeReporter.INSTANCE);
    }

    /**
     * Creates an instance of {@code WindowedMutatorFactory}, using the given
     * {@code strategy} for all created {@link Mutator}s, and the given
     * {@code timeReporter} for getting the current time.
     * <p>
     * This constructor is intended for unit testing scenarios.
     * </p>
     *
     * @param strategy The {@link IntervalStrategy} to use for all created
     *                 {@link Mutator}s.
     * @param timeReporter The {@link TimeReporter} to use to get the current
     *                     time.
     */
    public WindowedMutatorFactory(IntervalStrategy strategy,
            TimeReporter timeReporter) {
        this.strategy = strategy;
        this.timeReporter = timeReporter;

        Map<Object, Object> tmp = new HashMap<Object, Object>(2);
        tmp.put(IntervalStrategy.METADATA_TIME_WINDOW,
                strategy.timeWindowInNanos());
        tmp.put(IntervalStrategy.METADATA_INTERVALS, strategy.intervals());
        this.metadata = Collections.unmodifiableMap(tmp);
    }

    @Override
    public Map<Object, Object> getMetadata() {
        return metadata;
    }

    /**
     * Gets the {@code IntervalStrategy} to use for created {@link Mutator}s.
     * @return The {@code IntervalStrategy} to use for created {@link Mutator}s.
     */
    public IntervalStrategy getStrategy() {
        return strategy;
    }

    /**
     * Gets the {@code TimeReporter} to use for created {@link Mutator}s.
     * @return The {@code TimeReporter} to use for created {@link Mutator}s.
     */
    public TimeReporter getTimeReporter() {
        return timeReporter;
    }
}
