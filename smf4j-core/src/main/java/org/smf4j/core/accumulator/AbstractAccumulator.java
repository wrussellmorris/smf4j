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

import org.smf4j.Accumulator;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public abstract class AbstractAccumulator implements Accumulator {

    private volatile boolean on;
    private final ThreadLocal<AtomicLongValue> threadLocal;
    private final ConcurrentMap<Long, AccumulatorThread> participatingThreads;

    protected AbstractAccumulator() {
        this.participatingThreads =
                new ConcurrentHashMap<Long, AccumulatorThread>();
        this.threadLocal = new ThreadLocal<AtomicLongValue>() {
            @Override
            protected final AtomicLongValue initialValue() {
                AtomicLongValue threadLocalInst = new AtomicLongValue();
                Thread cur = Thread.currentThread();
                participatingThreads.put(cur.getId(), new AccumulatorThread(cur,
                        threadLocalInst));
                return threadLocalInst;
            }
        };
    }

    @Override
    public final boolean isOn() {
        return on;
    }

    @Override
    public final void setOn(boolean on) {
        this.on = on;
    }

    protected final AtomicLongValue getInst() {
        return threadLocal.get();
    }

    @Override
    public final long getValue() {
        List<Map.Entry<Long, AccumulatorThread>> deadThreads =
                new ArrayList<Map.Entry<Long, AccumulatorThread>>();

        long nanos = System.nanoTime();
        long value = 0;
        for (Map.Entry<Long, AccumulatorThread> entry
                : participatingThreads.entrySet()) {

            AccumulatorThread accThread = entry.getValue();
            Thread t = accThread.threadRef.get();
            if(t == null || !t.isAlive()) {
                // Thread's dead, baby.  Thread's dead...
                if(participatingThreads.remove(entry.getKey(), accThread)) {
                    deadThreads.add(entry);
                }
                continue;
            }
            value = combineValues(nanos, value, accThread, false);
        }

        // Scavenge values from now-dead threads
        for(Map.Entry<Long, AccumulatorThread> entry : deadThreads) {
            Long id = entry.getKey();
            AccumulatorThread accThread = entry.getValue();
            value = combineValues(nanos, value, accThread, true);
            if(accThread.scavenged.get()) {
                participatingThreads.remove(id, accThread);
            }
        }

        return value;
    }

    abstract long combineValues(long nanos, long input,
            AccumulatorThread accThread, boolean scavenging);

    static final class AccumulatorThread {
        final WeakReference<Thread> threadRef;
        final AtomicLongValue threadLocalInst;
        final AtomicBoolean scavenged;

        AccumulatorThread(Thread thread, AtomicLongValue threadLocalInst) {
            this.threadRef = new WeakReference<Thread>(thread);
            this.threadLocalInst = threadLocalInst;
            this.scavenged = new AtomicBoolean();
        }
    }
}
