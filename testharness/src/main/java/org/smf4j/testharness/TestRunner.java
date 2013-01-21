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
package org.smf4j.testharness;

import org.smf4j.Accumulator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class TestRunner implements Runnable {
    private long count = 0L;
    private int testLengthSeconds;
    private Accumulator accumulator;

    public void run() {
        long endAfter = System.nanoTime() + testLengthSeconds * 1000000000L;
        while(System.nanoTime() < endAfter) {
            accumulator.add(1);
            count++;
        }
    }

    public int getTestLengthSeconds() {
        return testLengthSeconds;
    }

    public void setTestLengthSeconds(int testLengthSeconds) {
        this.testLengthSeconds = testLengthSeconds;
    }

    public Accumulator getAccumulator() {
        return accumulator;
    }

    public void setAccumulator(Accumulator accumulator) {
        this.accumulator = accumulator;
    }

    public long getCount() {
        return count;
    }
}
