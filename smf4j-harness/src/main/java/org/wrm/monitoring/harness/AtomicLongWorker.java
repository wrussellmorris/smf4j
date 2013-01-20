/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wrm.monitoring.harness;

import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class AtomicLongWorker extends CountingWorker {
    private final AtomicLong counter;

    public AtomicLongWorker(long stopAfter, AtomicLong counter) {
        super(stopAfter);
        this.counter = counter;
    }

    @Override
    protected long doWork() {
        while(System.nanoTime() < stopAfter) {
            counter.incrementAndGet();
        }
        return counter.incrementAndGet();
    }
}
