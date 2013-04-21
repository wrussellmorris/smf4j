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
package org.smf4j.core.accumulator.hc;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.smf4j.Mutator;
import org.smf4j.core.accumulator.MutatorFactory;

/**
 * {@code MutatorRegistry} creates and manages a growable set of {@link Mutator}
 * instances and guarantees thread affinity for instances of {@link Mutator}
 * returned by {@link #get()}.
 * <p>
 * At any given point in time, each {@code Mutator} instance is associated with
 * exactly 0 or 1 live threads ({@link Thread#isAlive()}).  By convention,
 * {@link Mutator#put(long) Mutator.put} should only be called on instances
 * of {@code Mutator} that are returned to the caller via {@link #get()}.
 * Calling {@code Mutator.put} on a {@code Mutator} instance that was not
 * returned by {@code get} risks data loss.
 * </p>
 * <p>
 * {@code Mutator} instances will be re-used when the {@code Thread} they were
 * associated with dies. This means that the total number of {@code Mutator}
 * instances managed by a {@code MutatorRegistry} instance will never exceed
 * the maximum number of live {@code Thread}s that have called {@code get}.
 * </p>
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class MutatorRegistry {
    private final MutatorFactory mutatorFactory;
    private final ConcurrentMap<WeakThreadRef, Registration> registrations
            = new ConcurrentHashMap<WeakThreadRef, Registration>();

    /**
     * Creates a new {@code MutatorRegistry}.
     * @param mutatorFactory  The {@code MutatorFactory} that creates new
     *                        instances of {@link Mutator} when necessary.
     */
    public MutatorRegistry(MutatorFactory mutatorFactory) {
        this.mutatorFactory = mutatorFactory;
    }

    /**
     * Gets a {@code Mutator} instance that is bound to the calling
     * {@code Thread} for the lifetime of that {@code Thread}.  The
     * calling {@code Thread} can safely call {@code put} on the returned
     * {@code Mutator} instance without incurring a read-modify-write penalty
     * for concurrent access to the {@code Mutator}s internal data.
     * @return An instance of {@code Mutator} that is bound to the calling
     *         {@code Thread} for that {@code Thread}'s lifetime.
     */
    public Mutator get() {
        Thread currentThread = Thread.currentThread();

        // Our implementation of WeakThreadRef is such that instances of
        // Thread (or a subclass) can be used to find it in the map.
        @SuppressWarnings("element-type-mismatch")
        Registration r = registrations.get(currentThread);
        if(r != null) {
            return r.mutator;
        }

        // We don't have a registration yet - let's scan for an existing one
        // on a dead thread and attempt to acquire it.
        WeakThreadRef key = null;
        for(Map.Entry<WeakThreadRef, Registration> existing :
                registrations.entrySet()) {
            if(existing.getValue().acquire(currentThread)) {
                key = existing.getKey();
                r = existing.getValue();
                break;
            }
        }

        if(key != null) {
            // We acquired a mutator from a dead thread, so we need to
            // re-index it so we can find it again later!
            registrations.remove(key, r);
            key.reset(currentThread);
        } else {
            // We did not acquire a mutator from a dead thread, so we need
            // to create a new one.
            Mutator mutator = mutatorFactory.createMutator();
            r = new Registration(currentThread, mutator);
            key = new WeakThreadRef(currentThread);
        }

        // Re-register under this new thread.
        registrations.put(key, r);

        return r.mutator;
    }

    /**
     * Gets the combined value of all {@code Mutator} instances registered
     * internally, regardless of whether or not the {@code Thread}s they were
     * associated with are still alive.
     * @return The combined value of all {@code Mutator} instances registered
     *         in this {@code MutatorRegistry}.
     */
    public long getCombinedValue() {
        long value = mutatorFactory.getInitialValue();
        for (Registration registration : registrations.values()) {
            value = mutatorFactory.combine(value, registration.mutator);
        }

        return value;
    }

    /**
     * {@code WeakThreadRef} is a helper class used as a key in the
     * {@code MutatorRegistry}'s internal map of
     * {@code WeakThreadRef}->{@code Mutator}.
     * <p>
     * {@code WeakThreadRef}s mimic their associated {@code Thread} in a such
     * a way as to ensure that a value stored in a {@code Map} with a
     * {@code WeakThreadRef} key can be found by searching with the
     * {@code Thread} instance associated with the original
     * {@code WeakThreadRef}.
     * </p>
     * <pre>
     * Thread t = Thread.currentThread();
     * WeakThreadRef w = new WeakThreadRef(t);
     * map.put(w, "hello");
     * map.get(t); // Returns "hello"
     * </pre>
     * <p>
     * Using {@code WeakThreadRef} allows us to keep a weak reference to the
     * {@code Thread} that holds a specific {@code Mutator}, and further allows
     * us to find that {@code Mutator} instance in our map by using the value
     * of {@code Thread.currentThread()} as a key.
     * </p>
     */
    private static final class WeakThreadRef {
        private WeakReference<Thread> threadRef;
        private int hash;

        /**
         * Creates a new {@code WeakThreadRef}, referring to {@code thread}.
         * @param thread The {@code Thread} this {@code WeakThreadRef} mimics.
         */
        WeakThreadRef(Thread thread) {
            reset(thread);
        }

        /**
         * Resets the {@code WeakThreadRef} to mimic a different thread
         * {@code thread}.
         * <p>
         * This will alter the return values of {@code hashCode()} and
         * {@code equals}, and as such requires re-indexing any values
         * previously using this {@code WeakThreadRef} as a key.
         * </p>
         * @param thread The {@code Thread} to mimic.
         */
        void reset(Thread thread) {
            if(thread == null) {
                throw new NullPointerException();
            }
            threadRef = new WeakReference<Thread>(thread);
            hash = thread.hashCode();
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if(obj == this) {
                return true;
            }

            if(obj instanceof Thread) {
                Thread other = (Thread)obj;
                return threadRef.get() == other;
            }

            return false;
        }
    }

    /**
     * A {@code Registration} represents the pairing of a {@code Thread}
     * instance with a {@code Mutator} instance.
     */
    private static final class Registration {
        private volatile WeakReference<Thread> threadRef;
        private final Mutator mutator;
        private final AtomicBoolean available;

        /**
         * Creates a new {@code Registration}, pairing {@code thread} with
         * {@code mutator}.
         * @param thread The {@code Thread} that initally owns the
         *               {@code mutator}.
         * @param mutator The {@code Mutator} owned by {@code thread}.
         */
        private Registration(Thread thread, Mutator mutator) {
            this.threadRef = new WeakReference<Thread>(thread);
            this.mutator = mutator;
            this.available = new AtomicBoolean(false);
        }

        /**
         * Attempts to have {@code thread} acquire the {@code Mutator} instance
         * of this {@code Registration}.
         * <p>
         * Acquisition will succeed only if the {@code Thread} already
         * associated with this {@code Registration} is dead
         * ({@code !Thread.isAlive()}).
         * </p>
         * @param thread The new {@code Thread} that's trying to acquire this
         *               {@code Registration}.
         * @return Returns a boolean value indicating whether or not
         *         {@code thread} successfully acquired this
         *         {@code Registration}.
         */
        private boolean acquire(Thread thread) {
            // Check to see which Thread currently owns this.
            Thread cur = threadRef.get();
            if(cur == thread) {
                // It's already acquired by the current thread.
                return true;
            }

            // Check on the status of the currently-owning Thread.
            if(cur != null && cur.isAlive()) {
                // It's currently owned by another thread that is still alive.
                return false;
            }

            // Try to force this into an 'available' state.  The thread that
            // successfully sets 'available' from false to true will then
            // continue on below to acquire this guy, and then set 'available'
            // back to false.
            if(!available.compareAndSet(false, true)) {
                // We did not win the race - somebody else beat the current
                // thread to acquiring this guy.
                return false;
            }

            // The current thread has successfully acquired this, so record it
            // as such.
            this.threadRef = new WeakReference<Thread>(thread);
            this.available.set(false);
            return true;
        }
    }
}
