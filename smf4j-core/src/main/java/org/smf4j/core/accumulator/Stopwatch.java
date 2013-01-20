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

    private final TimeSliceStack slices = new TimeSliceStack();
    private final TimeReporter timeReporter;

    public Stopwatch() {
        this(SystemNanosTimeReporter.INSTANCE);
    }

    public Stopwatch(TimeReporter timeReporter) {
        this.timeReporter = timeReporter;
    }

    public void start() {
        slices.push(START, timeReporter.nanos());
    }

    public long lap() {
        long nanos = timeReporter.nanos();
        TimeSliceStack.TimeSlice last = slices.peek();
        if(last == null) {
            return 0L;
        }
        slices.push(LAP, nanos);
        return nanos - last.value;
    }

    public long end() {
        long nanos = timeReporter.nanos();
        TimeSliceStack.TimeSlice slice;
        do {
            slice = slices.pop();
            if(slice != null && slice.state == START) {
                return nanos - slice.value;
            }
        } while(slice != null);
        return 0L;
    }
}
