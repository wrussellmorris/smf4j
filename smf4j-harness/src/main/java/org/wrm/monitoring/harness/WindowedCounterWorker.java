/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wrm.monitoring.harness;

import org.wrm.monitoring.core.WindowedCounter;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class WindowedCounterWorker extends CountingWorker {
    private final WindowedCounter counter;

    public WindowedCounterWorker(long stopAfter, WindowedCounter counter) {
        super(stopAfter);
        this.counter = counter;
    }

    @Override
    protected long doWork() {
        long count = 0;
        while(System.nanoTime() < stopAfter) {
            counter.incr(1);
            count++;
        }
        return count;
    }
}
