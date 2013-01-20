/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wrm.monitoring.harness;

import java.util.concurrent.Callable;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public abstract class CountingWorker implements Callable<Long> {
    protected final long stopAfter;
    private volatile long count = 0;

    protected CountingWorker(long stopAfter) {
        this.stopAfter = stopAfter;
    }

    @Override
    public final Long call() {
        count = doWork();
        return count;
    }

    protected abstract long doWork();

    protected long getCount() {
        return count;
    }
}
