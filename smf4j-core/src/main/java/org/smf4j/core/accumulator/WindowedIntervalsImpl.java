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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class WindowedIntervalsImpl implements WindowedIntervals {

    private final ThreadLocal<Intervals> threadLocalBuckets;
    private final ConcurrentMap<Long, WeakReference<Intervals>> registry;
    private final int intervals;

    public WindowedIntervalsImpl(final IntervalStrategy strategy) {
        this.intervals = strategy.intervals();
        this.registry = new ConcurrentHashMap<Long, WeakReference<Intervals>>();
        this.threadLocalBuckets = new ThreadLocal<Intervals>() {
            @Override
            protected Intervals initialValue() {
                Intervals tlb = new Intervals(strategy);
                registry.put(Thread.currentThread().getId(),
                        new WeakReference(tlb));
                return tlb;
            }
        };
    }

    @Override
    public void incr(long nanos, long val) {
        threadLocalBuckets.get().incr(nanos, val);
    }

    @Override
    public long[] buckets(long nanos) {
        // An array to contain our return value
        long[] ret = new long[intervals];

        // Iterate over active buckets contained in our registry
        for(Intervals tlb : getActiveBuckets()) {
            long[] values = tlb.buckets(nanos);
            for(int i=0; i<intervals; i++) {
                ret[i] += values[i];
            }
        }

        return ret;
    }

    @Override
    public List<Intervals> getActiveBuckets() {
        List<Intervals> activeBuckets = new ArrayList<Intervals>();

        // A list of apparently-dead registry entries
        List<Map.Entry<Long, WeakReference<Intervals>>> toRemove =
            new ArrayList<Map.Entry<Long, WeakReference<Intervals>>>();

        // Iterate over all bucket contains in our registry
        for(Map.Entry<Long, WeakReference<Intervals>> entry
                : registry.entrySet()) {

            WeakReference<Intervals> ref = entry.getValue();
            Intervals tlb = ref.get();
            if(tlb == null) {
                // If tlb is null, we'll consider this entry dead
                toRemove.add(entry);
                continue;
            }
            activeBuckets.add(tlb);
        }

        // Before returning, we'll clean up our registry by removing dead
        // registrations
        for(int i=0; i<toRemove.size(); i++) {
            Map.Entry<Long, WeakReference<Intervals>> entry = toRemove.get(i);
            registry.remove(entry.getKey(), entry.getValue());
        }

        return activeBuckets;
    }
}
