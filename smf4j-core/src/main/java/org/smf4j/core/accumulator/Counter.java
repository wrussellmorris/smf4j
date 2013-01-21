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

import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class Counter extends AbstractAccumulator {

    final AtomicLong scavengedValue;

    public Counter() {
        this.scavengedValue = new AtomicLong();
    }

    public void add() {
        add(1);
    }

    @Override
    public void add(long delta) {
        if(!isOn()) {
            return;
        }

        getInst().incr(delta);
    }

    @Override
    long combineValues(long nanos, long input, AccumulatorThread accThread,
            boolean scavenging) {
        long combined = input;
        long newValue = accThread.threadLocalInst.syncGet();
        if(!scavenging) {
            combined = combined + newValue;
        } else if(scavenging) {
            while(true) {
                if(!accThread.scavenged.get()) {
                    if(accThread.scavenged.compareAndSet(false, true)) {
                        scavengedValue.addAndGet(newValue);
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        return combined + scavengedValue.get();
    }
}
