/*
 * Copyright 2012 rmorris.
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

import org.wrm.monitoring.core.AtomicLongValue;

/**
 *
 * @author rmorris
 */
public class LongValueFactory extends CountingWorkerFactory {
    private final AtomicLongValue counter;

    public LongValueFactory(boolean sharedCounter) {
        super(true);
        this.counter = new AtomicLongValue();
    }

    @Override
    public CountingWorker doCreateWorker(long stopAfter) {
        return new AtomicLongValueWorker(stopAfter, sharedCounter ? counter : new AtomicLongValue());
    }

    @Override
    public long getCount() {
        long count = 0;
        if(sharedCounter) {
            for(CountingWorker worker : workers) {
                if(count < worker.getCount()) {
                    count = worker.getCount();
                }
            }
        } else {
            for(CountingWorker worker : workers) {
                count += worker.getCount();
            }
        }
        return count;
    }
}
