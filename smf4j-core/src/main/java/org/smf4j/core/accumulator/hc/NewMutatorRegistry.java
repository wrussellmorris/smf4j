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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.smf4j.Mutator;
import org.smf4j.core.accumulator.MutatorFactory;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public final class NewMutatorRegistry {

    private static final int MAX_CAPACITY = 1<<30;
    private static final sun.misc.Unsafe UNSAFE;
    private static final long TAIL_OFFSET;
    private static final long TABLE_BASE;
    private static final long TABLE_SCALE;
    private static final long THREADREF_OFFSET;

    /**
     * hashSeed - copied from hashing in {@code ConcurrentHashMap (jdk 1.7)}
     */
    private final int hashSeed = sun.misc.Hashing.randomHashSeed(this);

    private final MutatorFactory mutatorFactory;
    private transient volatile Slot tail = null;
    private transient volatile Entry[] table;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final float loadFactor = 0.75f;
    private volatile int threshold;

    static {
        try {
            UNSAFE = getUnsafe();
            TAIL_OFFSET = UNSAFE.objectFieldOffset(
                    NewMutatorRegistry.class.getDeclaredField("tail"));
            Class es = Entry[].class;
            TABLE_BASE = UNSAFE.arrayBaseOffset(es);
            TABLE_SCALE = UNSAFE.arrayIndexScale(es);
            THREADREF_OFFSET = UNSAFE.objectFieldOffset(
                    Slot.class.getDeclaredField("threadRef"));
        } catch(Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Returns a sun.misc.Unsafe.  Suitable for use in a 3rd party package.
     * Replace with a simple call to Unsafe.getUnsafe when integrating
     * into a jdk.
     *
     * @return a sun.misc.Unsafe
     */
    private static sun.misc.Unsafe getUnsafe() {
        try {
            return sun.misc.Unsafe.getUnsafe();
        } catch (SecurityException se) {
            try {
                return java.security.AccessController.doPrivileged
                    (new java.security
                     .PrivilegedExceptionAction<sun.misc.Unsafe>() {
                        public sun.misc.Unsafe run() throws Exception {
                            java.lang.reflect.Field f = sun.misc
                                .Unsafe.class.getDeclaredField("theUnsafe");
                            f.setAccessible(true);
                            return (sun.misc.Unsafe) f.get(null);
                        }});
            } catch (java.security.PrivilegedActionException e) {
                throw new RuntimeException("Could not initialize intrinsics",
                                           e.getCause());
            }
        }
    }

    public NewMutatorRegistry(MutatorFactory mutatorFactory) {
        this.table = new Entry[2];
        this.mutatorFactory = mutatorFactory;
        this.threshold =
                (int)Math.min(table.length * loadFactor, MAX_CAPACITY+1);
    }

    public long getCombinedValue() {
        long value = mutatorFactory.getInitialValue();
        Slot s = tail;
        while(s != null) {
            value += s.mutator.get();
            s = s.prev;
        }

        return value;
    }

    public Mutator get() {
        final Thread thread = Thread.currentThread();
        final Entry[] tab = table;

        // Get index via hash
        int hash = hash(thread);
        int index = (tab.length - 1) & hash;
        Entry e = tab[index];
        Entry cur = e;
        while(cur != null) {
            if(cur.hash == hash && cur.slot.threadRef.get() == thread) {
                return cur.slot.mutator;
            }
            cur = cur.prev;
        }

        // This thread did not have a slot yet, so we'll get one for it.
        // We'll first try to scavenge one from a dead thread.
        int rebuildIndex = 0;
        Slot ourSlot = null;
        for(rebuildIndex=0; rebuildIndex<tab.length; rebuildIndex++) {
            e = tab[rebuildIndex];
            while(e != null) {
                WeakReference<Thread> w = e.slot.threadRef;
                Thread t = e.slot.threadRef.get();
                if(t == null || !t.isAlive()) {
                    WeakReference<Thread> wnew =
                            new WeakReference<Thread>(thread);
                    if(UNSAFE.compareAndSwapObject(e.slot, THREADREF_OFFSET,
                            w, wnew)) {
                        // By successfully setting a new WeakThreadRef, we've
                        // scavenged this slot in this entry for the given
                        // thread.
                        ourSlot = e.slot;
                    }
                }
                e = e.prev;
            }
        }

        if(ourSlot == null) {
            // If we didn't scavenge a slot from a dead thread, we'll need
            // to create one from scratch
            ourSlot = createSlot(thread);
        }

        // At this point we'll need to insert our new slot in the appropriate
        // location in the table, and also remember to rebuild the lead table
        // Entry that used to be able to reach our newly scavenged slot.

        // The reader lock controls modification of Entries in the existing
        // table
        Entry newEntry = null;
        readWriteLock.readLock().lock();
        try {
            // Push the new Entry into the table at the appropriate index
            while(true) {
                newEntry = new Entry(hash, ourSlot, (e = tab[index]));
                if(UNSAFE.compareAndSwapObject(tab,
                        (index*TABLE_SCALE) + TABLE_BASE, e, newEntry)) {
                    break;
                }
            }

            // Reconstruct the lead Entry that used to point to the entry that
            // contains our now-scavenged slot (if necessary)
            if(rebuildIndex < tab.length) {
                Entry oldLead;
                Entry newTail;
                do {
                    oldLead = tab[rebuildIndex];
                    newTail = null;
                    Entry o = oldLead;
                    while(o != null) {
                        if(o.slot != ourSlot) {
                            newTail = new Entry(o.hash, o.slot, newTail);
                        }
                        o = o.prev;
                    }
                } while(UNSAFE.compareAndSwapObject(tab,
                        (rebuildIndex*TABLE_SCALE) + TABLE_BASE, oldLead, newTail));
            }
        } finally {
            readWriteLock.readLock().unlock();
        }

        if(newEntry.size > threshold) {
            // We're above our threshold, so we need to resize ourselves.
            resize();
        }

        return ourSlot.mutator;
    }

    private void resize() {
        readWriteLock.writeLock().lock();
        try {
            Entry[] oldTab = table;
            Entry[] newTab = new Entry[oldTab.length<<1];
            threshold =
                    (int)Math.min(newTab.length * loadFactor, MAX_CAPACITY+1);

            int newMask = newTab.length-1;
            for(int i=0; i<oldTab.length; i++) {
                Entry e = oldTab[i];
                while(e != null) {
                    int newIndex = newMask & e.hash;
                    newTab[newIndex] =
                            new Entry(e.hash, e.slot, newTab[newIndex]);
                    e = e.prev;
                }
            }

            table = newTab;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Copied from {@code ConcurrentHashMap (jdk 1.7)}
     */
    private int hash(Thread t) {
        int h = hashSeed ^ t.hashCode();

        // Spread bits to regularize both segment and index locations,
        // using variant of single-word Wang/Jenkins hash.
        h += (h <<  15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h <<   3);
        h ^= (h >>>  6);
        h += (h <<   2) + (h << 14);
        return h ^ (h >>> 16);
    }

    private Slot createSlot(Thread thread) {
        Mutator mutator = mutatorFactory.createMutator();
        Slot s, prevtail;
        do {
            // Grab our current tail and create a new slot
            prevtail = tail;
            s = new Slot(prevtail, thread, mutator);
        } while(!UNSAFE.compareAndSwapObject(this, TAIL_OFFSET, prevtail, s));

        return s;
    }

    static final class Slot {
        final Mutator mutator;
        final Slot prev;
        volatile WeakReference<Thread> threadRef;

        Slot(Slot prev, Thread thread, Mutator mutator) {
            this.mutator = mutator;
            this.prev = prev;
            this.threadRef = new WeakReference<Thread>(thread);
        }
    }

    static final class Entry {
        final int hash;
        final Slot slot;
        final Entry prev;
        final int size;

        Entry(int hash, Slot item, Entry prev) {
            this.hash = hash;
            this.slot = item;
            this.prev = prev;
            this.size = prev == null ? 1 : 1+prev.size;
        }
    }
}
