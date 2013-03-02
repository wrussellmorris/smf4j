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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.smf4j.Mutator;
import org.smf4j.core.accumulator.MutatorFactory;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class MutatorRegistry implements Iterable<Mutator>{
    private final MutatorFactory mutatorFactory;
    private final ConcurrentMap<WeakThreadRef, Registration> registrations
            = new ConcurrentHashMap<WeakThreadRef, Registration>();

    public MutatorRegistry(final MutatorFactory mutatorFactory) {
        this.mutatorFactory = mutatorFactory;
    }

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
        WeakThreadRef existingKey = null;
        for(Map.Entry<WeakThreadRef, Registration> existing :
                registrations.entrySet()) {
            if(existing.getValue().acquire()) {
                existingKey = existing.getKey();
                r = existing.getValue();
                break;
            }
        }

        if(existingKey != null) {
            // We acquired a mutator from a dead thread, so we need to
            // re-index it so we can find it again later!
            registrations.remove(existingKey, r);
        }
        if(r == null) {
            // We did not acquire a mutator from a dead thread, so we need
            // to create a new one.
            Mutator mutator = mutatorFactory.createMutator();
            r = new Registration(currentThread, mutator);
        }

        // Re-register under this new thread.
        registrations.put(new WeakThreadRef(currentThread), r);

        return r.mutator;
    }

    public Iterator<Mutator> iterator() {
        return new Iter(registrations.values());
    }

    private static final class WeakThreadRef {
        private final WeakReference<Thread> threadRef;
        private final int hash;

        WeakThreadRef(Thread thread) {
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

    private static final class Registration {
        private volatile WeakReference<Thread> threadRef;
        private final Mutator mutator;
        private final AtomicBoolean available;

        private Registration(Thread thread, Mutator mutator) {
            this.threadRef = new WeakReference<Thread>(thread);
            this.mutator = mutator;
            this.available = new AtomicBoolean(false);
        }

        private boolean acquire() {
            // The current thread is the one that is attempting to acquire this.
            Thread thread = Thread.currentThread();

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
                // We did not win the race - somebody else beat the curren thread
                // to acquiring this.
                return false;
            }

            // The current thread has successfully acquired this, so record it
            // as such.
            this.threadRef = new WeakReference<Thread>(thread);
            this.available.set(false);
            return true;
        }
    }

    private static final class Iter implements Iterator<Mutator> {
        private final Iterator<Registration> inner;

        Iter(Collection<Registration> inner) {
            this.inner = inner.iterator();
        }

        public boolean hasNext() {
            return inner.hasNext();
        }

        public Mutator next() {
            return inner.next().mutator;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
