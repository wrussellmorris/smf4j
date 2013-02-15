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

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public abstract class WindowedMutatorFactory extends AbstractMutatorFactory {
    private final IntervalStrategy strategy;
    private final TimeReporter timeReporter;
    private final Map<Object, Object> metadata;

    public WindowedMutatorFactory(IntervalStrategy strategy) {
        this(strategy, SystemNanosTimeReporter.INSTANCE);
    }

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

    public IntervalStrategy getStrategy() {
        return strategy;
    }

    public TimeReporter getTimeReporter() {
        return timeReporter;
    }
}
