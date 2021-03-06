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
class TestUtils {
    private static final long A_BILLION = 1000000000;
    private static final long BASE_TIME = A_BILLION * 10;

    static long timenanos(int seconds) {
        return BASE_TIME + ((long)seconds * A_BILLION);
    }

    static long[] array(long... values) {
        return values;
    }

}
