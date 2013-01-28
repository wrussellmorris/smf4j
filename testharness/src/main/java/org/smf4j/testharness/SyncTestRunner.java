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

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class SyncTestRunner extends TestRunner {
    private final Object lock = new Object();
    private long count = 0;

    public SyncTestRunner(long testIterations) {
        super(testIterations, "synchronized");
    }

    @Override
    public void run() {
        long localCount = 0;
        long start = System.currentTimeMillis();
        while(localCount < testIterations) {
            synchronized(lock) {
                count++;
            }
            localCount++;
        }
        duration.getAndSet(System.currentTimeMillis() - start);
    }

}
