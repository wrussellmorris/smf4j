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
package org.wrm.monitoring.harness;

import org.wrm.monitoring.core.Counter;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class CounterWorker extends CountingWorker {
    private final Counter counter;

    public CounterWorker(long startAfter, Counter counter) {
        super(startAfter);
        this.counter = counter;
    }

    @Override
    protected long doWork() {
        while(System.nanoTime() < stopAfter) {
            counter.incr();
        }
        return counter.getValue();
    }
}
