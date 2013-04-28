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

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public abstract class TestRunner implements Runnable {
    private final String name;
    final ConcurrentLinkedQueue<Long> durations;
    final long testIterations;

    public TestRunner(long testIterations, String name) {
        this.testIterations = testIterations;
        this.name = name;
        this.durations = new ConcurrentLinkedQueue<Long>();
    }

    public void prepare() {
        durations.clear();
    }

    public String getName() {
        return name;
    }

    public final long getDuration() {
        double result = 0d;
        int i = 1;
        for(Long duration : durations) {
            result += ((double)duration - result) / (double)i;
            i++;
        }
        return (long)result;
    }
}
