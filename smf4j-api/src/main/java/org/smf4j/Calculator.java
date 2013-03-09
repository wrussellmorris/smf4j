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
package org.smf4j;

import java.util.Map;

/**
 * {@code Calculator} implementations are responsible for performing some
 * calculation based on the values of {@code Accumulator}s passed and returning
 * some consumable result.
 * <p>
 * {@code Calculator}s are intended to be used in conjunction with
 * {@link Accumulator}s as members of a specific {@link RegistryNode}.  In fact,
 * the parameters passed to {@link #calculate(java.util.Map, java.util.Map)} are
 * built from the results of {@link RegistryNode#getAccumulators()}.
 * </p>
 * <p>
 * The value returned by {@link #calculate(java.util.Map, java.util.Map)} can
 * be any type of object, but implementors designing their own
 * {@code Calculator} implementations should strive to make the returned
 * object fully comprehendible by the simple
 * {@code org.smf4j.util.helpers.CalculatorHelper} class,
 * which treats the returned class as either an integral type ({@code long},
 * {@code Long}, {@code String}, etc...), or as a bean with standard bean-style
 * property getters <b>for types that are integral types</b>.
 * </p>
 * <p>
 * As an example, an implementation of {@code Calculator} called {@code Ratio}
 * would probably return a {@code Double} from its implementation of
 * {@code calculate}:
 * <pre>
 * public class Ratio implements Calculator {
 *     // ...
 *     public Double calculate(Map&lt;String, Long&gt; values,
 *                             Map&lt;String, Accumulator&gt; accumulators){...}
 *     // ...
 * }
 * </pre>
 * <p>
 * <p>
 * Similarly, an implementation of {@code Calculator} that calculates simple
 * statistics for a set of {@code Accumulator}s might return an instance of
 * a class called {@code Stats}, designed as such:
 * <pre>
 * public final class Stats {
 *     // ...
 *     public Double getMean() { ... }
 *     public Long getMedian() { ... }
 *     public Long getMode() { ... }
 *     public Long getRange() { ... }
 *     // ...
 * }
 * </pre>
 * The {@code Calculator} would be defined like this:
 * <pre>
 * public class StatsCalculator implements Calculator {
 *     // ...
 *     public Stats calculate(Map&lt;String, Long&gt; values,
 *                             Map&lt;String, Accumulator&gt; accumulators){...}
 *     // ...
 * }
 * </pre>
 * </p>
 * <p>
 * {@code Calculator} implementations that return integral types may associate
 * units with their calculation by returning a non-{@code null} string from
 * their {@link #getUnits()} implementation.  {@code Calculator}
 * implementations that return user-defined types (like {@code Stats} above)
 * may associate units with each member via the
 * {@code org.smf4j.util.helpers.Units} annotation.  In either case, code that
 * inspects and exports information from {@code smf4j} may or may not pay
 * attention to this information.
 * </p>
 * <p>
 * {@code Calculator} implementations <strong>must</strong> be thread-safe.
 * </p>
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 * @see Accumulator
 * @see RegistryNode
 */
public interface Calculator {

    /**
     * Performs some calculation based upon the {@code Accumulator} values
     * reported in {@code values}, and also possibly using information
     * obtained about the {@code Accumulator}s who reported those values.
     * @param values A {@code Map}, keyed on the names of {@code Accumulator}s
     *               and holding very recent results of calling these
     *               {@code Accumulator}s {@link Accumulator#get() get()}
     *               methods.
     * @param accumulators A {@code Map}, keyed on the names of
     *                     {@code Accumulator}s, whose values are the actual
     *                     {@code Accumulator} instances.
     * @return Returns an integral type (like {@code long}, {@code Long},
     *         {@code String}, etc...), or an object that can be fully
     *         comprehended by {@code org.smf4j.util.helpers.CalculatorHelper}.
     */
    Object calculate(Map<String, Long> values,
            Map<String, Accumulator> accumulators);

    /**
     * Gets a string representing the units that should be associated with
     * the return integral value of {@code calculate}, or returns {@code null}
     * to indicate that units should not be associated with this integral return
     * value of {@code calculate}.
     * <p>
     * The return value is only meaningful if {@code calculate} returns an
     * integral type value.  If the return value of {@code calculate} is not an
     * integral type, then the {@link Units} annotation should be used on that
     * type's member getters to indicate the units associated with each member.
     * </p>
     * @return  Gets a string representing the units that should be associated
     *          with the return integral value of {@code calculate}, or returns
     *          {@code null} to indicate that units should not be associated
     *          with this integral return value of {@code calculate}.
     */
    String getUnits();
}
