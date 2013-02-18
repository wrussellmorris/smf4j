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
import org.smf4j.RegistryNode;

/**
 * {@code CalculatorHelper} is a utility class that helps investigating and
 * querying {@link Calculator} instances, and the calculations they return.
 * <p>
 * This class is intended to serve as a utility class that {@code smf4j}
 * exporters can use as a core 'comprehension' facility for the results of
 * {@link Calculator}s configured in a {@link RegistryNode}.
 * </p>
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class CalculatorHelper {

    /**
     * The logger we use
     */
    private static final Logger log =
            LoggerFactory.getLogger(CalculatorHelper.class);

    /**
     * A set of types considered 'integral' types.
     */
    private static final Set<Class<?>> integralTypes = new HashSet<Class<?>>();
    static {
        integralTypes.add(byte.class);
        integralTypes.add(short.class);
        integralTypes.add(int.class);
        integralTypes.add(long.class);
        integralTypes.add(float.class);
        integralTypes.add(double.class);

        integralTypes.add(Byte.class);
        integralTypes.add(Short.class);
        integralTypes.add(Integer.class);
        integralTypes.add(Long.class);
        integralTypes.add(Float.class);
        integralTypes.add(Double.class);
        integralTypes.add(String.class);
    }

    /**
     * {@code CalculatorHelper} is a static singleton.
     */
    private CalculatorHelper() {
    }

    /**
     * A {@link Comparable} implementation that can sort
     * {@link CalculatorProperty}s.
     */
    private static final Comparator<CalculatorProperty> SORTER =
            new Comparator<CalculatorProperty>() {
        public int compare(CalculatorProperty o1, CalculatorProperty o2) {
            if(o1 != null  && o2 != null) {
                return o1.getName().compareTo(o2.getName());
            }
            return 0;
        }
    };

    /**
     * Gets a {@code List} of {@link CalculatorProperty}s for the given
     * {@code calculator}, using {@code rootName} as the root name for all
     * discovered {@link CalculatorProperty}s.
     * <p>
     * This method uses reflection to inspect the type returned by
     * {@code calculator}'s implementation of
     * {@link Calculator#calculate(java.util.Map, java.util.Map) Calculator.calculate}.
     * </p>
     * <p>
     * If {@code calculate}'s return type is one of the
     * {@code integralTypes}, then a single {@link CalculatorProperty} is
     * returned, of that type, with the name {@code rootName}.
     * </p>
     * <p>
     * If {@code calculate}'s return type is not one of the integral types,
     * then reflection is used to find all of the getters of that type that are
     * themselves of integral types, and for each of these a
     * {@link CalculatorProperty} is synthesized.  If there are no getters of
     * integral type, then the returned {@code List} will be empty.
     * </p>
     * @param rootName The root name used to synthesize all property names.
     * @param calculator The {@link Calculator} whose {@code calculate} return
     *                   value is to be inspected.
     * @return A {@code List} of {@link CalculatorProperty}s for the given
     *         {@code calculator}'s {@code calculate} implementation.
     */
    public static List<CalculatorProperty> getCalculatorAttributes(
            String rootName, Calculator calculator) {
        if(rootName == null || calculator == null) {
            return Collections.emptyList();
        }

        List<CalculatorProperty> attrs = new ArrayList<CalculatorProperty>();
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
        if(integralTypes.contains(calculationClass)) {
            // It's an integral type.
            attrs.add(new CalculatorProperty(rootName, calculator.getUnits(),
                    calculationClass));
        } else {
            collectLeafProperties(rootName, calculationClass, attrs);
        }

        Collections.sort(attrs, SORTER);
        return attrs;
    }

    /**
     * Resolves the value {@code calcAndPropertyName} against the data in
     * {@code snapshot}.
     * <p>
     * {@code calcAndPropertyName} is in the format
     * {@code calcName.property.name}.  First the value for the key
     * {@code calcName} is looked up in {@code snapshot}, and from this object
     * the {@code property.name} getters are walked recursively to resolve the
     * final value.
     * </p>
     * <p>
     * If {@code null} is encountered along the way, or if the
     * {@code calculator} result or a getter is not found along the way, this
     * method returns {@code null}.
     * </p>
     * @param snapshot The snapshot used to find the calculation result.
     * @param calcAndPropertyName The name and path of the value to be found.
     * @return The value {@code calcAndPropertyName}, resolved against the
     *         calculation result stored in {@code snapshot}.
     */
    public static Object resolveValue(Map<String, Object> snapshot,
            String calcAndPropertyName) {
        if(snapshot == null || calcAndPropertyName == null) {
            return null;
        }

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

    /**
     * Resolves the value {@code propertyName} against the calculation result
     * {@code calculation}.
     * <p>
     * {@code propertyName} is in the format {@code property.name}.  The
     * {@code propertyName} is walked from {@code calculation} recursively to
     * obtain the final value.
     * </p>
     * <p>
     * If {@code null} is encountered along the way, or if the a getter is not
     * found along the way, this method returns {@code null}.
     * </p>
     * @param calculation The result of calculation.
     * @param propertyName The path of the value to be found.
     * @return The value {@code propertyName}, resolved against the
     *         calculation result {@code calculation}.
     */
    public static Object resolveValue(Object calculation, String propertyName) {
        if(calculation == null || propertyName == null) {
            return null;
        }

        String[] nameParts = getNameParts(propertyName);
        if(nameParts.length == 0) {
            return calculation;
        } else {
            return doResolveValue(calculation, nameParts, false);
        }
    }

    /**
     * Performs the process of resolving the value identified by the path
     * {@code nameParts} against {@code root}.
     * @param root The root object that servers as the starting point for
     *             resolution.
     * @param nameParts An array of the property names that are to be
     *                 recursively resolved, starting with {@code nameParts[0]}
     *                 on {@code root}.
     * @param namePartsIncludesCalcName A flag indicating that resolution should
     *                                  start with {@code nameParts[1]} on
     *                                  root.
     * @return Returns the value found by recursively calling the getters
     *         identified by {@code nameParts} on {@code root}, or {@code null}
     *         if an error or other unrecoverable situation is encountered.
     */
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

    /**
     * Splits {@code name} into an array of property names.
     * @param name The property name that needs to be split.
     * @return An array of {@code String}s of all property names in
     *         {@code name}, split by '.'.
     */
    private static String[] getNameParts(String name) {
        return name.split("\\.");
    }

    /**
     * Finds all integral-typed getters on {@code calcResultClass}, and builds
     * a list of {@link CalculatorProperty}s using {@code rootName} as their
     * root name, adding them to {@code attrs}.
     * @param rootName The root name from which all properties are accessed.
     * @param calcResultClass The class of the calculation result.
     * @param attrs The {@code List} of {@link CalculatorProperty}s found.
     */
    private static void collectLeafProperties(String rootName, Class<?>
            calcResultClass, List<CalculatorProperty> attrs) {
        try {
            BeanInfo bi = Introspector.getBeanInfo(calcResultClass);
            for(PropertyDescriptor pd : bi.getPropertyDescriptors()) {
                if(pd.getReadMethod() == null ||
                        !integralTypes.contains(pd.getPropertyType())) {
                    continue;
                }

                Class<?> propertyType = pd.getPropertyType();
                attrs.add(new CalculatorProperty(rootName + "." + pd.getName(),
                        getUnitsString(pd.getReadMethod()), propertyType));
            }
        } catch(IntrospectionException e) {
            log.error(String.format(
                    "Failed to enumerate PropertyDescriptors for the "
                    + "Calculator.calculate(..) return type class '%s'.",
                    calcResultClass.getCanonicalName()), e);
        }
    }

    /**
     * Finds a {@link Units} annotation bound to {@code method}, if one
     * exists.
     * @param method The {@code method} to inspect.
     * @return A {@code String} identifying the units associated with this
     *         method via {@link Unit}, or {@code null} if there is no
     *         {@link Unit} annotation, or if the {@link Unit} annotation
     *         returns only the empty {@code String ""}.
     */
    private static String getUnitsString(Method method) {
        Units unitsAttr = method.getAnnotation(Units.class);
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
