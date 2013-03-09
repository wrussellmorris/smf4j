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
package org.smf4j.util.helpers;

import org.smf4j.Calculator;

/**
 * {@code CalculatorProperty} represents a property found on the type returned
 * by an implementation of
 * {@link Calculator#calculate(java.util.Map, java.util.Map) Calculator.calculate}
 * .
 * <p>
 * Instances of this class are typically created by
 * {@link CalculatorHelper#getCalculatorAttributes(java.lang.String, org.smf4j.Calculator)}.
 * </p>
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class CalculatorProperty {

    /**
     * The name of this property.
     */
    private final String name;

    /**
     * The units of this property, potentially null.
     */
    private final String units;

    /**
     * The type of this property.
     */
    private final Class<?> type;

    /**
     * Creates a new instance of {@code CalculatorProperty}, using the
     * given {@code name}, {@code units}, and {@code type}.
     * @param name The name of this property.
     * @param units The units of this property, possibly {@code null}.
     * @param type  The type of this property.
     */
    CalculatorProperty(String name, String units, Class<?> type) {
        this.name = name;
        this.units = units;
        this.type = type;
    }

    /**
     * Gets the units of this property, potentially {@code null}.
     * @return The units of this property, potentially {@code null}.
     */
    public String getUnits() {
        return units;
    }

    /**
     * Gets the name of this property.
     * @return The name of this property.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type of this property.
     * @return The type of this property.
     */
    public Class<?> getType() {
        return type;
    }
}
