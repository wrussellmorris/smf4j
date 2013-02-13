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

import java.util.*;
import org.smf4j.Accumulator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class RangeGroup extends AbstractCalculator {

    private static final GroupingComparator sorter = new GroupingComparator();
    private static final Frequency DEFAULT_FREQUENCY = Frequency.SECONDS;
    private static final String DEFAULT_FORMAT_STRING = "%.2f%s";

    private String accumulator;
    private List<Grouping> groupings;
    private String formatString = DEFAULT_FORMAT_STRING;
    private double threshold = 0.85d;
    private boolean normalize = false;
    private Normalizer normalizer = null;
    private Frequency frequency = null;

    public RangeGroup() {
        this.groupings = new ArrayList<Grouping>();
    }

    @Override
    public String calculate(Map<String, Long> values,
        Map<String, Accumulator> accumulators) {
        double start;

        if(normalize) {
            Double val = normalizer.calculate(values, accumulators);
            if(val == null) {
                val = 0.0d;
            }
            start = val;
        } else {
            Long val = values.get(getAccumulator());
            if(val == null) {
                val = 0L;
            }
            start = val.doubleValue();
        }

        double sign = Math.signum(start);
        double result = start = Math.abs(start);
        String label = getUnits();
        if(groupings.size() > 0) {
            for(Grouping g : groupings) {
                double test = start / (double)g.getRange();
                if(test < threshold) {
                    break;
                }
                result = test;
                label = g.getSuffix();
            }
        }

        return String.format(getFormatString(), result*sign,
                label == null ? "" : label);
    }

    /**
     * @return the unitsOfMeasure
     */
    public List<Grouping> getGroupings() {
        return groupings;
    }

    /**
     * @param groupings the groupings to set
     */
    public void setGroupings(List<Grouping> groupings) {
        if(groupings == null) {
            groupings = new ArrayList<Grouping>();
        }

        Collections.sort(groupings, sorter);
        this.groupings = groupings;
    }

    /**
     * @return the threshold
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * @param threshold the threshold to set
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    /**
     * @return the accumulator
     */
    public String getAccumulator() {
        return accumulator;
    }

    /**
     * @param accumulator the accumulator to set
     */
    public void setAccumulator(String accumulator) {
        this.accumulator = accumulator;
        initNormalizer();
    }

    public boolean isNormalize() {
        return normalize;
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
        initNormalizer();
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency != null ? frequency : DEFAULT_FREQUENCY;
    }

    private void initNormalizer() {
        if(normalize) {
            if(normalizer == null) {
                normalizer = new Normalizer();
            }
            normalizer.setFrequency(frequency);
            normalizer.setAccumulator(accumulator);
        } else {
            normalizer = null;
        }
    }

    public String getFormatString() {
        return formatString;
    }

    public void setFormatString(String formatString) {
        if(formatString == null || formatString.length() == 0) {
            formatString = DEFAULT_FORMAT_STRING;
        }
        this.formatString = formatString;
    }

    public static class Grouping {
        private String suffix;
        private double range;

        /**
         * @return the suffix
         */
        public String getSuffix() {
            return suffix;
        }

        /**
         * @param suffix the label to set
         */
        public void setSuffix(String suffix) {
            if(suffix == null) {
                this.suffix = "";
            }
            this.suffix = suffix;
        }

        /**
         * @return the range
         */
        public double getRange() {
            return range;
        }

        /**
         * @param range the range to set
         */
        public void setRange(double range) {
            if(range <= 0.0) {
                throw new IllegalArgumentException("range");
            }
            this.range = range;
        }
    }

    static class GroupingComparator implements Comparator<Grouping> {
        @Override
        public int compare(Grouping o1, Grouping o2) {
            double diff = o1.range - o2.range;
            if(diff == 0.0d) {
                return 0;
            }
            return diff < 0.0d ? -1 : 1;
        }
    }
}
