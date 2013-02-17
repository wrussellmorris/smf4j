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
package org.smf4j;

/**
 * {@code Mutator} instances are returned by {@link Accumulator}s and allow
 * clients to modify the {@code Accumulator}, or combine the values of
 * {@code Mutator}s that were created by the same {@link Accumulator}.
 * <p>
 * In code that will modify an {@code Accumulator} multiple times in one pass,
 * the typical usage pattern is to tear off a {@code Mutator} once and call it
 * multiple times, instead of repeatedly calling
 * {@code Accumulator.getMutator()}.  This allows {@code Accumulator}s built
 * for high-contention multi-threaded usage to create {@code Mutator} instances
 * that are thread-local to the caller, and thus avoid resource contention
 * introduced in high-contention read-modify-write scenarios.
 * <pre>
 * // ...
 * Mutator msgsProcMtr = msgsProcAcc.getMutator();
 * Mutator msgsSuccessMtr = msgsSuccessAcc.getMutator();
 * Mutator msgsFailedMtr = msgsFailedAcc.getMutator();
 *
 * for(Msg msg : hunkOfMsgs) {
 *     msgsProcMtr.put(1);
 *     if(processMsg(msg) {
 *         msgsSuccessMtr.put(1);
 *     } else {
 *         msgsFailedMtr.put(1);
 *     }
 * // ...
 * </pre>
 * </p>
 * @author Russell Morris (wrussellmorris@gmail.com)
 * @see Accumulator
 */
public interface Mutator {

    /**
     * Modifies the {@link Accumulator} who produced this {@code Mutator} in
     * some way with {@code delta}.
     * <p>
     * The actual semantics of the modification are up to the implementation of
     * {@code Accumulator} that produced this {@code Mutator} instance.
     * </p>
     *
     * @param delta The value used to modify the {@code Accumulator} who
     *              produced this {@code Mutator}.
     */
    void put(long delta);

    /**
     * Returns the internal value of this {@code Mutator} combined with
     * {@code other}, but <b>does not</b> modify the internal state of this
     * {@code Mutator}.
     * <p>
     * This method exists principally to allow {@code Accumulator}s to easily
     * combine the values of their {@code Mutator}s in order to report the
     * {@code Accumulator}s own value.
     * </p>
     * <p>
     * The actual semantics of the combination are up to the specifics of
     * the implementation of {@code Mutator}.
     * </p>
     * @param other The other value that is to be combined with this
     *              {@code Mutator}'s internal value.
     * @return Returns the internal value of this {@code Mutator} combined with
     *         {@code other}.
     */
    long combine(long other);

    /**
     * Gets the internal value of this {@code Mutator}.
     * <p>
     * The returned value does not necessarily equal the return value of
     * {@link Accumulator#get()} of the {@code Accumulator} who produced this
     * {@code Mutator}.
     * </p>
     *
     * @return The internal value of this {@code Mutator}.
     */
    long get();
}
