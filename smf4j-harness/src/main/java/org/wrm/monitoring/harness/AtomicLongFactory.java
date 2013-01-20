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
public class AtomicLongFactory extends CountingWorkerFactory {
    private AtomicLong counter = new AtomicLong();

    public AtomicLongFactory(boolean highContention) {
        super(highContention);
    }

    @Override
    public CountingWorker doCreateWorker(long stopAfter) {
        return new AtomicLongWorker(stopAfter, sharedCounter ? counter : new AtomicLong());
    }

    @Override
    public void reset() {
        super.reset();
        counter = new AtomicLong();
    }

    @Override
    public long getCount() {
        long count = 0;
        if(sharedCounter) {
            for(CountingWorker worker : workers) {
                if(count < worker.getCount()) {
                    count = worker.getCount();
                }
            }
        } else {
            for(CountingWorker worker : workers) {
                count += worker.getCount();
            }
        }
        return count;
    }
}
