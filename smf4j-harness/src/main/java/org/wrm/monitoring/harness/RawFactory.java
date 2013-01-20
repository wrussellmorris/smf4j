/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wrm.monitoring.harness;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class RawFactory extends CountingWorkerFactory {

    public RawFactory() {
        super(false);
    }

    @Override
    public CountingWorker doCreateWorker(long stopAfter) {
        RawWorker worker = new RawWorker(stopAfter);
        workers.add(worker);
        return worker;
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
