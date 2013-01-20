/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wrm.monitoring.harness;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public abstract class CountingWorkerFactory {
    protected final List<CountingWorker> workers = new ArrayList<CountingWorker>();
    protected final boolean sharedCounter;

    public CountingWorkerFactory(boolean sharedCounter) {
        this.sharedCounter = sharedCounter;
    }

    public final CountingWorker createWorker(long stopAfter) {
        CountingWorker worker = doCreateWorker(stopAfter);
        workers.add(worker);
        return worker;
    }

    public abstract CountingWorker doCreateWorker(long stopAfter);

    public void setup() {
    }

    public void reset() {
        workers.clear();
    }

    public abstract long getCount();}
