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

import org.wrm.monitoring.core.MinOrMaxCounter;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class MinOrMaxFactory extends CountingWorkerFactory {
    private final boolean max;
    private final MinOrMaxCounter counter;

    public MinOrMaxFactory(boolean sharedCounter, boolean max) {
        super(sharedCounter);
        this.max = max;
        this.counter = createCounter();
    }

    @Override
    public MinOrMaxWorker doCreateWorker(long stopAfter) {
        return new MinOrMaxWorker(stopAfter, sharedCounter ? counter : createCounter());
    }

    public final MinOrMaxCounter createCounter() {
        MinOrMaxCounter t = new MinOrMaxCounter();
        t.setMax(max);
        t.setOn(true);
        return t;
    }

    public long getCount() {
        long count = 0;
        for(CountingWorker worker : workers) {
            count += worker.getCount();
        }
        return count;
    }
}
