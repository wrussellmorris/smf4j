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

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class MinOrMaxCounter extends AbstractAccumulator {

    private boolean max;

    public MinOrMaxCounter() {
        max = false;
    }

    public boolean isMax() {
        return max;
    }

    public void setMax(boolean max) {
        this.max = max;
    }

    @Override
    public final void add(long val) {
        if(!isOn()) {
            return;
        }

        // Check for new bound
        AtomicLongValue inst = getInst();
        long current = inst.localGet();
        if(max ? (val > current) : (val < current)) {
            inst.set(val);
        }
    }

    @Override
    final long combineValues(long nanos, long input, AccumulatorThread accThread,
            boolean scavenging) {
        long newValue = accThread.threadLocalInst.syncGet();
        if(max ? (input > newValue) : (input < newValue)) {
            return input;
        }
        return newValue;
    }
}
