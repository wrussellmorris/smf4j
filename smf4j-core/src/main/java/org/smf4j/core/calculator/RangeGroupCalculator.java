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

import org.smf4j.Calculator;
import java.util.*;
import org.smf4j.Accumulator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class RangeGroupCalculator implements Calculator {

    private static final GroupingComparator sorter = new GroupingComparator();

    private String accumulator;
    private List<Grouping> groupings;
    private double threshold = 0.85d;

    public RangeGroupCalculator() {
        this.groupings = new ArrayList<Grouping>();
    }

    @Override
    public Object calculate(Map<String, Long> values,
        Map<String, Accumulator> accumulators) {
        Long val = values.get(getAccumulator());
        if(val == null) {
            return "NaN";
        }

        double result = val;
        String label = "";
        if(groupings.size() > 0) {
            label = groupings.get(0).getLabel();
        }
        for(Grouping g : groupings) {
            double test = val / (double)g.getMultiple();
            if(test < threshold) {
                break;
            }
            result = test;
            label = g.getLabel();
        }

        return String.format("%.2f%s", result, label);
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
            this.groupings = new ArrayList<Grouping>();
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
    public void setThreshhold(double threshold) {
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
    }

    public static class Grouping {
        private String label;
        private long multiple;

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * @param label the label to set
         */
        public void setLabel(String label) {
            this.label = label;
        }

        /**
         * @return the multiple
         */
        public long getMultiple() {
            return multiple;
        }

        /**
         * @param multiple the multiple to set
         */
        public void setMultiple(long multiple) {
            this.multiple = multiple;
        }
    }

    static class GroupingComparator implements Comparator<Grouping> {
        @Override
        public int compare(Grouping o1, Grouping o2) {
            long diff = o1.multiple - o2.multiple;
            if(diff == 0L) {
                return 0;
            }
            return diff < 0L ? -1 : 1;
        }
    }
}
