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
public final class Stopwatch {
    static final int START = 0;
    static final int LAP = 1;

    private final TimeReporter timeReporter;
    private long total = 0L;
    private long laps = 0L;
    private long mark = 0L;

    public Stopwatch() {
        this(SystemNanosTimeReporter.INSTANCE);
    }

    public Stopwatch(TimeReporter timeReporter) {
        this.timeReporter = timeReporter;
    }

    public void start() {
        mark = timeReporter.nanos();
        total = 0L;
        laps = 0L;
    }

    public long lap() {
        if(mark == 0L) {
            return 0L;
        }

        long nanos = timeReporter.nanos();
        long delta = nanos - mark;
        mark = nanos;
        total += delta;
        laps++;
        return delta;
    }

    public long stop() {
        if(mark == 0L) {
            return 0L;
        }

        long delta = timeReporter.nanos() - mark;
        total += delta;
        laps++;
        mark = 0L;
        return delta;
    }

    public long pause() {
        if(mark == 0L) {
            return 0L;
        }

        long delta = timeReporter.nanos() - mark;
        total += delta;
        mark = 0L;
        return delta;
    }

    public void resume() {
        mark = timeReporter.nanos();
    }

    public long getLaps() {
        return laps;
    }

    public long getTotal() {
        return total;
    }
}
