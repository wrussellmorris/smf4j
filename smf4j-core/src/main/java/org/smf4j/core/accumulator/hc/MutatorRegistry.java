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
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.smf4j.Mutator;
import org.smf4j.core.accumulator.MutatorFactory;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class MutatorRegistry implements Iterable<Mutator>{

    private final ThreadLocal<Mutator> threadLocal;
    private final Queue<Registration> registrations
            = new ConcurrentLinkedQueue<Registration>();

    public MutatorRegistry(final MutatorFactory mutatorFactory) {
        this.threadLocal = new ThreadLocal<Mutator>() {
            @Override
            protected Mutator initialValue() {
                Thread thread = Thread.currentThread();
                Registration next = null;
                for(Registration r : registrations) {
                    if(r.isDead() &&
                            r.available.compareAndSet(false, true)) {
                        // Now this one is ours!
                        r.threadRef = new WeakReference<Thread>(thread);
                        r.available.set(false);
                        next = r;
                    }
                }

                if(next == null) {
                    Mutator mutator = mutatorFactory.createMutator();
                    next = new Registration(thread, mutator);
                    registrations.offer(next);
                }

                // Return the registerd mutator
                return next.mutator;
            }
        };
    }

    public Mutator get() {
        return threadLocal.get();
    }

    public Iterator<Mutator> iterator() {
        return new Iter(registrations.iterator());
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

        private boolean isDead() {
            Thread thread = threadRef.get();
            return thread == null || !thread.isAlive();
        }
    }

    private static final class Iter implements Iterator<Mutator> {
        private final Iterator<Registration> inner;

        Iter(Iterator<Registration> inner) {
            this.inner = inner;
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
