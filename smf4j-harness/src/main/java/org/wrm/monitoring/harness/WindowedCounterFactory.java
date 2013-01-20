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
public class WindowedCounterFactory extends CountingWorkerFactory {
    private WindowedCounter counter;

    public WindowedCounterFactory(boolean highContention) {
        super(highContention);
    }

    @Override
    public CountingWorker doCreateWorker(long stopAfter) {
        WindowedCounterWorker worker = new WindowedCounterWorker(stopAfter, sharedCounter ? counter : createCounter());
        return worker;
    }

    @Override
    public void setup() {
        super.setup();
        counter = createCounter();
    }

    @Override
    public void reset() {
        super.reset();
    }

    public WindowedCounter createCounter() {
         WindowedCounter t = new WindowedCounter(2, 10);
         t.setOn(true);
         return t;
    }

    @Override
    public long getCount() {
        long count = 0;
        for(CountingWorker worker : workers) {
            count += worker.getCount();
        }
        return count;
    }
}
