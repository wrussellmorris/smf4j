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
package org.smf4j.core.calculator;

import java.util.Map;
import org.smf4j.Accumulator;
import org.smf4j.core.accumulator.WindowedCounter;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class WindowNormalizer extends AbstractCalculator {

    private String windowedCounter;
    private Frequency frequency = Frequency.SECONDS;

    @Override
    public Double calculate(Map<String, Long> values,
        Map<String, Accumulator> accumulators) {

        Long val = values.get(getWindowedCounter());
        Object wc = accumulators.get(getWindowedCounter());
        if(wc instanceof WindowedCounter && val != null ) {
            double window = ((WindowedCounter)wc).getTimeWindow();
            return (frequency.getNanos() / window) * val;
        }

        return 0.0d;
    }

    public String getWindowedCounter() {
        return windowedCounter;
    }

    public void setWindowedCounter(String windowedCounter) {
        this.windowedCounter = windowedCounter;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        if(frequency != null) {
            this.frequency = frequency;
        }
    }

    @Override
    public String getUnits() {
        return frequency.units;
    }

    public enum Frequency {
        NANOS       ("1/ns", 1),
        MICROS      ("1/us", NANOS.getNanos() * 1000),
        MILLIS      ("1/ms", MICROS.getNanos() * 1000L),
        SECONDS     ("1/s", MILLIS.getNanos() * 1000L),
        MINUTES     ("1/m", SECONDS.getNanos() * 60L),
        HOURS       ("1/h", MINUTES.getNanos() * 60L),
        DAYS        ("1/d", HOURS.getNanos() * 24L),
        WEEKS       ("1/w", DAYS.getNanos() * 7L);

        private final String units;
        private final long nanos;

        Frequency(String units, long nanos) {
            this.nanos = nanos;
            this.units = units;
        }

        public long getNanos() {
            return nanos;
        }
    }
}
