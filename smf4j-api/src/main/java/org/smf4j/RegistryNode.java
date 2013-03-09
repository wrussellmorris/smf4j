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
import org.smf4j.nop.NopAccumulator;
import org.smf4j.nop.NopCalculator;
import org.smf4j.nop.NopRegistryNode;

/**
 * {@code  RegistryNode}s contain a collection of {@link Accumulator}s and
 * {@link Calculator}s at a <a href="Registrar.html#NodeNameAndPath">named</a>
 * location in a {@link Registrar}s managed hierarchy.
 * <p>
 * {@code RegistryNode}s maintain an <em>on</em> or <em>off</em> state which
 * is propagated to their contained {@link Accumulator}s.  This <em>on</em> or
 * <em>off</em> state is either set directly
 * (via {@link #setOn(boolean) setOn}), or is inferred from their parent
 * {@code RegistryNode}'s state ({@link #clearOn() clearOn}).
 * </p>
 * <p>
 * {@code RegistryNode} implementations <strong>must</strong> be thread-safe.
 * </p>
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public interface RegistryNode {

    /**
     * Gets the <a href="Registrar.html#NodeNameAndPath">name</a> of this
     * node.
     * <p>
     * The {@code RegistryNode} returned by {@link Registrar#getRootNode()}
     * <strong>must</strong> return the empty string ({@code ""}).
     * </p>
     * @return The <a href="Registrar.html#NodeNameAndPath">name</a> of this
     *         node.
     */
    String getName();

    /**
     * Gets a {@code boolean} value indicating whether or not this
     * {@code RegistryNode} is <em>on</em> or <em>off</em>.
     * @return A {@code boolean} value indicating whether or not this
     *         {@code RegistryNode} is <em>on</em> or <em>off</em>.
     */
    boolean isOn();

    /**
     * Sets this {@code RegistryNode}'s state, and propagates that state to
     * all of its contained {@link Accumulator}s.
     * @param on A {@code boolean} indicating whether this {@code RegistryNode}
     *           should be <em>on</em> ({@code true}) or <em>off</em>
     *           ({@code false}).
     */
    void setOn(boolean on);

    /**
     * Clears this {@code RegistryNode}'s state, forcing it to infer it from
     * its parent's state, and propagates that state to all of its contained
     * {@link Accumulator}s.
     */
    void clearOn();

    /**
     * Attempts to register {@code accumulator} under the
     * <a href="Registrar.html#AccumulatorAndCalculatorName">name</a>
     * {@code name}, and returns the {@link Accumulator} that is ultimately
     * registered under {@code name}.
     * <p>
     * If there is another {@link Accumulator} or {@link Calculator} already
     * registered under this name, or if {@code name} does not conform to the
     * <a href="Registrar.html#AccumulatorAndCalculatorName">requirements</a>
     * of an {@code Accumulator} name, then the registration will not succeed.
     * If there is an {@link Accumulator} already registered with this name,
     * then it is returned.  If there is a {@link Calculator} registered with
     * this name, or {@code name} is malformed, then
     * {@link NopAccumulator#INSTANCE} is returned instead.
     * </p>
     *
     * @param name The <a href="Registrar.html#AccumulatorAndCalculatorName">name</a>
     *             to register the {@code Accumulator}.
     * @param accumulator The {@code Accumulator} to register.
     * @return Returns the {@link Accumulator} ultimately registered with this
     *         {@code name}, or {@link NopAccumulator#INSTANCE} if a
     *         {@link Calculator} is already registered with {@code name}, or
     *         if {@code name} is malformed.
     */
    Accumulator register(String name, Accumulator accumulator);

    /**
     * Attempts to register {@code calculator} under the
     * <a href="Registrar.html#AccumulatorAndCalculatorName">name</a>
     * {@code name}, and returns the {@link Calculator} that is ultimately
     * registered under {@code name}.
     * <p>
     * If there is another {@link Calculator} or {@link Accumulator} already
     * registered under this name, or if {@code name} does not conform to the
     * <a href="Registrar.html#AccumulatorAndCalculatorName">requirements</a>
     * of a {@code Calculator} name, then the registration will not succeed.
     * If there is a {@link Calculator} already registered with this name,
     * then it is returned.  If there is a {@link Calculator} registered with
     * this name, or {@code name} is malformed, then
     * {@link NopCalculator#INSTANCE} is returned instead.
     * </p>
     *
     * @param name The <a href="Registrar.html#AccumulatorAndCalculatorName">name</a>
     *             to register the {@code Calculator}.
     * @param calculator The {@code Calculator} to register.
     * @return Returns the {@link Calculator} ultimately registered with this
     *         {@code name}, or {@link NopCalculator#INSTANCE} if an
     *         {@link Accumulator} is already registered with {@code name}, or
     *         if {@code name} is malformed.
     */
    Calculator register(String name, Calculator calculator);

    /**
     * Unregisters the {@link Accumulator} {@code accumulator} under the name
     * {@code name}.
     * <p>
     * If the {@code RegistryNode} does not have the {@link Accumulator}
     * {@code accumulator} registered under the name {@code name}, then the
     * unregistration fails.
     * </p>
     * @param name The name of the {@code Accumulator} to unregister.
     * @param accumulator The {@code Accumulator} to unregister.
     * @return Returns a {@code boolean} value indicating whether or not the
     *         unregistration succeeded.
     */
    boolean unregister(String name, Accumulator accumulator);

    /**
     * Unregisters the {@link Calculator} {@code calculator} under the name
     * {@code name}.
     * <p>
     * If the {@code RegistryNode} does not have the {@link Calculator}
     * {@code calculator} registered under the name {@code name}, then the
     * unregistration fails.
     * </p>
     * @param name The name of the {@code Calculator} to unregister.
     * @param calculator The {@code Calculator} to unregister.
     * @return Returns a {@code boolean} value indicating whether or not the
     *         unregistration succeeded.
     */
    boolean unregister(String name, Calculator calculator);

    /**
     * Gets a {@code Map} of {@code name->Accumulator} representing the
     * {@link Accumulator}s that are part of this {@code RegistryNode}.
     * @return A {@code Map} of {@code name->Accumulator} representing the
     * {@link Accumulator}s that are part of this {@code RegistryNode}.
     */
    Map<String, Accumulator> getAccumulators();

    /**
     * Gets a {@code Map} of {@code name->Calculator} representing the
     * {@link Calculator}s that are part of this {@code RegistryNode}.
     * @return A {@code Map} of {@code name->Calculator} representing the
     * {@link Calculator}s that are part of this {@code RegistryNode}.
     */
    Map<String, Calculator> getCalculators();

    /**
     * Gets the {@link Accumulator} registered under the name {@code name}.
     * @param name The {@code name} under which the {@link Accumulator} is
     *             registered.
     * @return The {@link Accumulator} registered under the name {@code name},
     *             or {@link NopAccumulator#INSTANCE} if there is no
     *             {@code Accumulator} under this name, or if {@code name} does
     *             not conform to the
     *             <a href="Registrar.html#AccumulatorAndCalculatorName">
     *             proper format</a>.
     */
    Accumulator getAccumulator(String name);

    /**
     * Gets the {@link Calculator} registered under the name {@code name}.
     * @param name The {@code name} under which the {@link Calculator} is
     *             registered.
     * @return The {@link Calculator} registered under the name {@code name},
     *             or {@link NopCalculator#INSTANCE} if there is no
     *             {@code Calculator} under this name, or if {@code name} does
     *             not conform to the
     *             <a href="Registrar.html#AccumulatorAndCalculatorName">
     *             proper format</a>.
     */
    Calculator getCalculator(String name);

    /**
     * Returns a 'snapshot' of this {@code RegistryNode} in a map that maps
     * all {@link Accumulator} names to the value returned by their
     * {@link Accumulator#get() get()}, and all {@link Calculator} names to
     * the value returned by their
     * {@link Calculator#calculate(java.util.Map, java.util.Map) calculate}.
     * <p>
     * This method is responsible for calling all {@link Calculator}s'
     * {@link Calculator#calculate(java.util.Map, java.util.Map) calculate}
     * methods.
     * </p>
     * @return A 'snapshot' of this {@code RegistryNode} in a map that maps
     *         all {@link Accumulator} names to the value returned by their
     *         {@link Accumulator#get() get()}, and all {@link Calculator}
     *         names to the value returned by their
     *         {@link Calculator#calculate(java.util.Map, java.util.Map) calculate}.
     */
    Map<String, Object> snapshot();

    /**
     * Gets a {@code Map} of {@code name->RegistryNode} representing all
     * child {@code RegistryNode}s.
     * @return A {@code Map} of {@code name->RegistryNode} representing all
     *         child {@code RegistryNode}s.
     */
    Map<String, RegistryNode> getChildNodes();

    /**
     * Gets a {@code RegistryNode} that is the child named {@code name}.
     * @param name The <a href="Registrar.html#NodeNameAndPath">name</a> of the
     *             child {@code RegistryNode}.
     * @return A {@code RegistryNode} that is the child named {@code name}, or
     *         {@link NopRegistryNode#INSTANCE} if there is no child named
     *         {@code name}, or {@code name} does not conform to the
     *         <a href="Registrar.html#NodeNameAndPath">format</a> of
     *         {@code RegistryNode} names.
     */
    RegistryNode getChildNode(String name);
}
