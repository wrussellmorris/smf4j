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
 * {@code TimeReporter} provides a notion of the 'current time' in nanoseconds.
 * <p>
 * This interface is used by all code interested in the current time so that
 * they can be unit tested deterministically.
 * </p>
 *
 * @see SystemNanosTimeReporter#INSTANCE
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public interface TimeReporter {

    /**
     * Reports the current time, in nanoseconds.
     * @return The current time, in nanoseconds.
     */
    long nanos();
}
