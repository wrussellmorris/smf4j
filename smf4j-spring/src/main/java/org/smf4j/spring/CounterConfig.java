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
package org.smf4j.spring;

import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
class CounterConfig {
    public static final String TIMEWINDOW_ATTR = "time-window";
    public static final String INTERVALS_ATTR = "intervals";

    private CounterType counterType = CounterType.NA;
    private ContentionType contentionType = ContentionType.NA;
    private DurationType durationType = DurationType.NA;
    private IntervalsType intervalsType = IntervalsType.NA;
    private Integer timeWindow = null;
    private Integer numIntervals = null;

    CounterConfig(CounterType counterType, Element element) {
        this.counterType = counterType;
        this.contentionType = ContentionType.from(element);
        this.durationType = DurationType.from(element);
        this.intervalsType = IntervalsType.from(element);

        String tmp = element.getAttribute(TIMEWINDOW_ATTR);
        if(StringUtils.hasLength(tmp)) {
            try {
                timeWindow = Integer.parseInt(tmp);
            } catch(NumberFormatException e) {
                timeWindow = null;
            }
        }
        tmp = element.getAttribute(INTERVALS_ATTR);
        if(StringUtils.hasLength(tmp)) {
            try {
                numIntervals = Integer.parseInt(tmp);
            } catch(NumberFormatException e) {
                numIntervals = null;
            }
        }
    }

    CounterType getCounterType() {
        return counterType;
    }

    void setCounterType(CounterType counterType) {
        this.counterType = counterType;
    }

    ContentionType getContentionType() {
        return contentionType;
    }

    void setConcurrencyType(ContentionType concurrencyType) {
        this.contentionType = concurrencyType;
    }

    DurationType getDurationType() {
        return durationType;
    }

    void setDurationType(DurationType durationType) {
        this.durationType = durationType;
    }

    IntervalsType getIntervalsType() {
        return intervalsType;
    }

    void setIntervalsType(IntervalsType intervalsType) {
        this.intervalsType = intervalsType;
    }

    Integer getTimeWindow() {
        return timeWindow;
    }

    void setTimeWindow(Integer timeWindow) {
        this.timeWindow = timeWindow;
    }

    Integer getNumIntervals() {
        return numIntervals;
    }

    void setNumIntervals(Integer numIntervals) {
        this.numIntervals = numIntervals;
    }

    enum ContentionType {
        NA      (null),
        UNKNOWN (null),
        HIGH    ("high"),
        LOW     ("low");

        public static final String ATTR_NAME = "contention";
        private final String str;
        private ContentionType(String str) {
            this.str = str;
        }
        public static ContentionType from(Element element) {
            String attr = element.getAttribute(ATTR_NAME);
            if(StringUtils.hasLength(attr)) {
                for(ContentionType tmp : ContentionType.values()) {
                    if(tmp.str != null && tmp.str.equals(attr) ) {
                        return tmp;
                    }
                }
            }
            return attr == null ? NA : UNKNOWN;
        }
    }

    enum CounterType {
        NA,
        ADD,
        MIN,
        MAX
    }

    enum DurationType {
        NA          (null),
        UNKNOWN     (null),
        UNBOUNDED   ("unbounded"),
        WINDOWED    ("windowed");

        public static final String ATTR_NAME = "duration";
        private final String str;
        private DurationType(String str) {
            this.str = str;
        }
        public static DurationType from(Element element) {
            String attr = element.getAttribute(ATTR_NAME);
            if(StringUtils.hasLength(attr)) {
                for(DurationType tmp : DurationType.values()) {
                    if(tmp.str != null && tmp.str.equals(attr) ) {
                        return tmp;
                    }
                }
            }
            return attr == null ? NA : UNKNOWN;
        }
    }

    enum IntervalsType {
        NA          (null),
        UNKNOWN     (null),
        SECONDS     ("seconds"),
        POWERSOFTWO ("nanos-powers-of-2");

        public static final String ATTR_NAME = "intervals-type";
        private final String str;
        private IntervalsType(String str) {
            this.str = str;
        }
        public static IntervalsType from(Element element) {
            String attr = element.getAttribute(ATTR_NAME);
            if(StringUtils.hasLength(attr)) {
                for(IntervalsType tmp : IntervalsType.values()) {
                    if(tmp.str != null && tmp.str.equals(attr) ) {
                        return tmp;
                    }
                }
            }
            return attr == null ? NA : UNKNOWN;
        }
    }
}

