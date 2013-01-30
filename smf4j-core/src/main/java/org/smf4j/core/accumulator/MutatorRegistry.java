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
package org.smf4j.core.accumulator;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.smf4j.Mutator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class MutatorRegistry
        implements Iterable<MutatorRegistry.Registration> {

    private static final Object DUMMY = new Object();
    private final ThreadLocal<Mutator> threadLocal;
    private final ConcurrentMap<Registration, Object> inUse
            = new ConcurrentHashMap<Registration, Object>();

    public MutatorRegistry(final MutatorFactory mutatorFactory) {
        this.threadLocal = new ThreadLocal<Mutator>() {
            @Override
            protected Mutator initialValue() {
                Mutator mutator = mutatorFactory.createMutator();

                // Try to re-use an existing, but unused, registration
                Thread thread = Thread.currentThread();
                Registration next = new Registration(thread, mutator);
                inUse.put(next, DUMMY);

                // Return the registerd mutator
                return mutator;
            }
        };
    }

    public Mutator get() {
        return threadLocal.get();
    }

    public Iterator<Registration> iterator() {
        return inUse.keySet().iterator();
    }

    public boolean unregister(Registration registration) {
        // The thread that owned this mutator is dead, so now we'll try
        // to remove it from the in-use set and signal the caller that it's
        // value (which they should have read immediately before this call)
        // can be safely scavenged.
        if(inUse.remove(registration) == DUMMY) {
            return true;
        }

        // We did not successfully remove the registration, so we assume
        // that somebody else got to it first.
        return false;
    }

    public static final class Registration {
        private final WeakReference<Thread> threadRef;
        private final Mutator mutator;

        private Registration(Thread thread, Mutator mutator) {
            this.threadRef = new WeakReference<Thread>(thread);
            this.mutator = mutator;
        }

        public Thread getThread() {
            return threadRef.get();
        }

        public Mutator getMutator() {
            return mutator;
        }

        public boolean isDead() {
            Thread thread = threadRef.get();
            return thread == null || !thread.isAlive();
        }
    }
}
