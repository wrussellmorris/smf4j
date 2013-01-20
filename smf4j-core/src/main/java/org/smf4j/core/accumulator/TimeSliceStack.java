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

import java.util.ArrayList;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
class TimeSliceStack {
    private static final int INITIAL_SIZE = 500;
    private int head;
    private final ArrayList<TimeSlice> slices;

    public TimeSliceStack() {
        slices = new ArrayList<TimeSlice>(INITIAL_SIZE);
        slices.add(new TimeSlice(0, 0L));
        head = -1;
    }

    public void push(int state, long value) {
        int next = head + 1;
        if(next < slices.size()) {
            // We've got an old slice just laying around
            TimeSlice slice = slices.get(next);
            slice.state = state;
            slice.value = value;
        } else {
            // We need to create a new slice
            slices.add(new TimeSlice(state, value));
        }
        head = next;
    }

    public TimeSlice peek() {
        if(head < 0) {
            return null;
        }
        return slices.get(head);
    }

    public TimeSlice pop() {
        if(head < 0) {
            return null;
        }

        return slices.get(head--);
    }

    public static final class TimeSlice {
        int state;
        long value;

        TimeSlice(int state, long value) {
            this.state = state;
            this.value = value;
        }
    }
}
