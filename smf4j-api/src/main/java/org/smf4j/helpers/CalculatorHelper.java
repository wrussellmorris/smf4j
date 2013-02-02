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
package org.smf4j.helpers;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smf4j.Calculator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class CalculatorHelper {

    private static final Logger log =
            LoggerFactory.getLogger(CalculatorHelper.class);

    private static final Set<Class<?>> leafTypes = new HashSet<Class<?>>();
    static {
        leafTypes.add(byte.class);
        leafTypes.add(short.class);
        leafTypes.add(int.class);
        leafTypes.add(long.class);
        leafTypes.add(float.class);
        leafTypes.add(double.class);

        leafTypes.add(Byte.class);
        leafTypes.add(Short.class);
        leafTypes.add(Integer.class);
        leafTypes.add(Long.class);
        leafTypes.add(Float.class);
        leafTypes.add(Double.class);
        leafTypes.add(String.class);
    }

    private static final Comparator<CalculatorAttribute> SORTER =
            new Comparator<CalculatorAttribute>() {
        public int compare(CalculatorAttribute o1, CalculatorAttribute o2) {
            if(o1 != null  && o2 != null) {
                return o1.getName().compareTo(o2.getName());
            }
            return 0;
        }
    };

    public static List<CalculatorAttribute> getCalculatorAttributes(
            String rootName, Calculator calculator) {
        List<CalculatorAttribute> attrs = new ArrayList<CalculatorAttribute>();
        Class<?> calculatorClass = calculator.getClass();

        // Get the return type of the calculate() method
        Method m;
        try {
            m = calculatorClass.getMethod("calculate", Map.class, Map.class);
        } catch(Throwable t) {
            log.error(String.format(
                    "Failed to determine return value of the 'calculate' method"
                    + " for the Calculator-implementing class '%s'.",
                    calculatorClass.getCanonicalName()), t);
            return attrs;
        }

        Class<?> calculationClass = m.getReturnType();
        if(leafTypes.contains(calculationClass)) {
            // It's a leaf type.
            attrs.add(new CalculatorAttribute(rootName,
                    getUnitsString(calculatorClass), calculationClass));
        } else {
            collectLeafProperties(rootName, calculationClass, attrs);
        }

        Collections.sort(attrs, SORTER);
        return attrs;
    }

    public static Object resolveValue(Map<String, Object> snapshot,
            String calcAndPropertyName) {
        String[] nameParts = getNameParts(calcAndPropertyName);
        if(nameParts.length == 0) {
            return null;
        }
        if(nameParts.length == 1) {
            return snapshot.get(nameParts[0]);
        }
        Object calc = snapshot.get(nameParts[0]);
        if(calc != null) {
            return doResolveValue(calc, nameParts, true);
        }
        return null;
    }

    public static Object resolveValue(Object calculation, String propertyName) {
        return doResolveValue(calculation, getNameParts(propertyName), false);
    }

    private static Object doResolveValue(Object root, String[] nameParts,
            boolean namePartsIncludesCalcName) {
        StringBuilder walkedPath = new StringBuilder();
        int firstNamePart = 0;
        if(namePartsIncludesCalcName) {
            walkedPath.append(nameParts[firstNamePart++]);
        }

        Object cur = root;
        try {
            for(int i=firstNamePart; i<nameParts.length; i++) {
                if(cur == null) {
                    log.warn("null value encountered at '{}'", walkedPath);
                    return null;
                }
                String part = nameParts[i];

                // Find property getting for this portion of the path
                BeanInfo bi = Introspector.getBeanInfo(cur.getClass());
                Method m = null;
                for(PropertyDescriptor pd : bi.getPropertyDescriptors()) {
                    if(!pd.getName().equals(part)) {
                        continue;
                    }
                    m = pd.getReadMethod();
                }

                if(m == null) {
                    log.warn(String.format(
                            "Unable to get property read method for %s.[%s]",
                            walkedPath.toString(), part));
                    return null;
                }

                Object val = null;
                try {
                    val = m.invoke(cur);
                } catch (Throwable t) {
                    log.warn(String.format(
                            "Unable to get property %s.[%s]",
                            walkedPath.toString(), part), t);
                }

                // Next!
                if(i > 0) {
                    walkedPath.append(".");
                }
                walkedPath.append(part);
                cur = val;
            }
        } catch(IntrospectionException e) {
            log.warn(String.format(
                    "Unable to introspect bean from '%s'",
                    walkedPath.toString()),e);
            cur = null;
        }

        return cur;
    }

    private static String[] getNameParts(String name) {
        return name.split("\\.");
    }

    private static void collectLeafProperties(String rootName, Class<?>
            calcResultClass, List<CalculatorAttribute> attrs) {
        try {
            BeanInfo bi = Introspector.getBeanInfo(calcResultClass);
            for(PropertyDescriptor pd : bi.getPropertyDescriptors()) {
                if(pd.getReadMethod() == null ||
                        !leafTypes.contains(pd.getPropertyType())) {
                    continue;
                }

                // Check to see if they've attached a Units hint...
                Class<?> propertyType = pd.getPropertyType();
                attrs.add(new CalculatorAttribute(rootName + "." + pd.getName(),
                        getUnitsString(propertyType), propertyType));
            }
        } catch(IntrospectionException e) {
            log.error(String.format(
                    "Failed to enumerate PropertyDescriptors for the "
                    + "Calculator.calculate(..) return type class '%s'.",
                    calcResultClass.getCanonicalName()), e);
        }
    }

    private static String getUnitsString(Class<?> clazz) {
        Units unitsAttr = clazz.getAnnotation(Units.class);
        if(unitsAttr == null) {
            return null;
        }

        String units = unitsAttr.value();
        if("".equals(units)) {
            return null;
        }

        return units;
    }
}
