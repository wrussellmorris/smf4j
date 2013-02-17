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
import org.smf4j.helpers.NopMutator;

/**
 * {@code Accumulator} is the core interface of <a href="http://www.smf4j.org">
 * smf4j</a>.  {@code Accumulator}s are responsible for reporting some
 * meaningful {@code long} value, and for providing {@link Mutator}s to modify
 * that value at runtime.  In this regard, {@code Accumulator} is somewhat
 * comparable to logging systems' {@code Logger} interfaces - they define a
 * simple api for simple data acquisition and externalization.
 *
 * <p>
 * {@code Accumulator} instances can be created directly in code that wishes to
 * use them, or can be registered ahead of time in a {@link Registrar}, and
 * retrieved later.  The latter usage pattern allows the developer to plan
 * a set (or better, a hierarchy) of {@code Accumulators} that can - at
 * runtime - provide insight into areas of the application that are usefully
 * quantifiable and indicative of system state, health, and/or performance.
 * </p>
 * <p>
 * {@code Accumulator} implementations are responsible for deciding what the
 * value they report via {@link #get()} means, and how the {@link Mutator}s they
 * produce from {@link #getMutator()} modify the returned value.
 * </p>
 * <p>
 * {@code Accumulator} implementations <strong>must</strong> be thread-safe.
 * </p>
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 * @see Mutator
 * @see Registrar
 */
public interface Accumulator {

    /**
     * Gets a {@code boolean} value that indicates whether or not this
     * {@code Accumulator} is {@code on}.
     * <p>
     * The value reported by {@code isOn} affects the function of {@link #get()}
     * and {@link #getMutator()}.  {@code Accumulator}s are always {@code off}
     * by default, but can be turned on by external code via
     * {@link #setOn(boolean)}.
     * </p>
     * @return Returns a {@code boolean} value indicating whether or not this
     * {@code Accumulator} is {@code on}.
     *
     * @see #setOn(boolean)
     * @see #get()
     * @see #getMutator()
     */
    boolean isOn();

    /**
     * Turns the {@code Accumulator} {@code on} or {@code off}.
     * @param on {@code true} to turn the {@code Accumulator} {@code on},
     * {@code false} to turn the {@code Accumulator} {@code off}.
     *
     * @see #isOn()
     */
    void setOn(boolean on);

    /**
     * Gets a {@link Mutator} instance that can modify this {@code Accumulator}
     * via calls to its {@link Mutator#put(long)}.
     * <p>
     * If the {@code Accumulator} is {@code on}, the returned {@link Mutator}
     * instance will be able to modify the {@code Accumulator}.  If the
     * {@code Accumulator} is {@code off}, the returned {@link Mutator} will be
     * {@link NopMutator#INSTANCE}, which does not modify anything.
     * </p>
     * <p>
     * An implementation of {@code Accumulator} is not required to allow
     * modification by {@link Mutator}s.  In this case, the implementation
     * should always return {@link NopMutator#INSTANCE}.
     * </p>
     * @return An instance of {@code Mutator} that can modify this
     *         {@code Accumulator} via {@link Mutator#put(long)} if the
     *         {@code Accumulator} is {@code on}, or an instance of
     *         {@link NopMutator#INSTANCE} if the {@code Accumulator} is
     *         {@code off}.
     */
    Mutator getMutator();

    /**
     * Gets a {@code long} value that represents the current state of this
     * {@code Accumulator}.  Exactly what this value means, and how it is
     * created and modified, is up to the implementation of {@code Accumulator}.
     * <p>
     * If the {@code Accumulator} is {@code off} ({@link #isOn()}), then the
     * returned value will be {@code 0}.
     * </p>
     * @return Returns the value held by this {@code Accumulator}, or {@code 0}
     * if this {@code Accumulator} is {@code off}.
     *
     * @see #isOn()
     * @see #setOn(boolean)
     */
    long get();

    /**
     * Gets a string representing the units this {@code Accumulator} uses, or
     * {@code null} if this information is not relevant to this
     * {@code Accumulator}.
     * <p>
     * This value should be constant per instance of {@code Accumulator}.
     * However, it is ok to return different values, or {@code null}, for
     * different instances of the same class of {@code Accumulator}.  Often,
     * it is best for an {@code Accumulator} implementation to allow the
     * instance creator to specify the units for that instance.
     * </p>
     * <p>
     * Exporters may or may not pay attention to this value when exporting
     * the {@code Accumulator} instances.
     * </p>
     * @return Gets a string representing the units this {@code Accumulator}
     * uses, or {@code null} if this information is not relevant to this {@code
     * Accumulator} class or instance.
     */
    String getUnits();

    /**
     * Gets implementation-specific metadata about this {@code Accumulator}
     * implementation and/or instance.
     * <p>
     * This method can return {@code null} if the implementor has no need
     * for keeping metadata alongside the {@code Accumulator}.
     * </p>
     * @return Returns implementation-specific metadata about this
     * {@code Accumulator} and/or instance, or {@code null} if no metadata is
     * associated with this {@code Accumulator} and/or instance.
     */
    Map<Object, Object> getMetadata();
}
