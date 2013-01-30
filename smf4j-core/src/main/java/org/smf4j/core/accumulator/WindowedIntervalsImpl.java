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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.smf4j.Mutator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class WindowedIntervalsImpl implements WindowedIntervals {

    private final MutatorRegistry mutatorRegistry;
    private final int intervals;

    public WindowedIntervalsImpl(final IntervalStrategy strategy,
            final TimeReporter timeReporter) {
        this.intervals = strategy.intervals();
        this.mutatorRegistry = new MutatorRegistry( new MutatorFactory() {
            @Override
            public Mutator createMutator() {
                return new Intervals(strategy, timeReporter);
            }
        });
    }

    @Override
    public Mutator getMutator() {
        return mutatorRegistry.get();
    }

    @Override
    public long[] buckets(long nanos) {
        // An array to contain our return value
        long[] ret = new long[intervals];

        // Iterate over active buckets contained in our registry
        for(MutatorRegistry.Registration registration : mutatorRegistry) {
            Intervals cur = (Intervals)registration.getMutator();
            if(registration.isDead() && cur.allBucketsStale(nanos)) {
                // If this thread is null AND all of its buckets are stale,
                // let's try to unregister it now.
                mutatorRegistry.unregister(registration);
            } else {
                long[] values = cur.buckets(nanos);
                for(int i=0; i<intervals; i++) {
                    ret[i] += values[i];
                }
            }
        }

        return ret;
    }

    @Override
    public long get(long nanos) {
        long value = 0L;

        // Iterate over all bucket contains in our registry
        for(MutatorRegistry.Registration registration : mutatorRegistry) {
            Intervals cur = (Intervals)registration.getMutator();
            if(registration.isDead() && cur.allBucketsStale(nanos)) {
                // If this thread is null AND all of its buckets are stale,
                // let's try to unregister it now.
                mutatorRegistry.unregister(registration);
            } else {
                value += cur.syncGet();
            }
        }

        return value;
    }
}
