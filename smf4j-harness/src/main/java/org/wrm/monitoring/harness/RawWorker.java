/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wrm.monitoring.harness;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class RawWorker extends CountingWorker {

    public RawWorker(long stopAfter) {
        super(stopAfter);
    }

    @Override
    protected long doWork() {
        long localCount = 0;
        while(System.nanoTime() < stopAfter) {
            localCount++;
        }
        return localCount;
    }
}
