package org.smf4j.testharness;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.smf4j.Accumulator;
import org.smf4j.Registrar;
import org.smf4j.RegistrarFactory;
import org.smf4j.core.accumulator.hc.HighConcurrencyAccumulator;
import org.smf4j.core.accumulator.IntervalStrategy;
import org.smf4j.core.accumulator.lc.LowConcurrencyAccumulator;
import org.smf4j.core.accumulator.PowersOfTwoIntervalStrategy;
import org.smf4j.core.accumulator.SecondsIntervalStrategy;
import org.smf4j.core.accumulator.hc.UnboundedAddMutator;
import org.smf4j.core.accumulator.hc.UnboundedMaxMutator;
import org.smf4j.core.accumulator.hc.UnboundedMinMutator;
import org.smf4j.core.accumulator.hc.WindowedAddMutator;
import org.smf4j.core.accumulator.hc.WindowedMaxMutator;
import org.smf4j.core.accumulator.hc.WindowedMinMutator;
import org.smf4j.to.jmx.JmxRegistrarPublisher;

/**
 * Hello world!
 *
 */
public class App
{
    BufferedWriter bw;
    ExecutorService tpe;

    public void go(long testIterations, int numThreads) throws IOException, InterruptedException {
        List<TestRunner> runners = getTestRunners(testIterations);
        Registrar r = RegistrarFactory.getRegistrar();
        r.getRootNode().setOn(true);
        JmxRegistrarPublisher publisher = new JmxRegistrarPublisher();
        publisher.publish();

        // Run one pass of lower-concurrency tests to prime the app
        System.out.println("Priming...");
        runLowConcurrencyTests(runners);

        // Now for the real tests
        System.out.println("Starting...");
        runLowConcurrencyTests(runners);
        runHighConcurrencyTests(runners, numThreads);

        System.out.println("Done");
    }

    public void runLowConcurrencyTests(List<TestRunner> runners)
    throws IOException, InterruptedException {
        openFile("lc");

        writeData("Test name");
        writeData("Duration");
        newLine();

        for(TestRunner runner : runners) {
            runLowConcurrencyTest(runner);
        }
        closeFile();
    }

    public void runLowConcurrencyTest(TestRunner runner) throws IOException, InterruptedException {
        writeData(runner.getName());
        System.out.print(runner.getName() + ":");
        runHighConcurrencyTest(runner, 1);
        System.out.println();
        newLine();
    }

    public void runHighConcurrencyTests(List<TestRunner> runners, int numThreads)
    throws IOException, InterruptedException {
        openFile("hc");

        writeData("Test name");
        for(int i=1; i<=numThreads; i++) {
            writeData(i + " Thread(s)");
        }
        newLine();

        for(TestRunner runner : runners) {
            runHighConcurrencyTests(runner, numThreads);
        }
        closeFile();
    }

    public void runHighConcurrencyTests(TestRunner runner, int numThreads) throws IOException, InterruptedException {
        writeData(runner.getName());
        System.out.print(runner.getName() + ":");
        for(int i=1; i<=numThreads; i++) {
            System.out.print(" " + i);
            runHighConcurrencyTest(runner, i);
        }
        System.out.println();
        newLine();
    }

    public void runHighConcurrencyTest(final TestRunner runner, int numThreads) throws InterruptedException, IOException {
        List<Callable<Long>> runners = new ArrayList<Callable<Long>>();
        runner.prepare();
        tpe = Executors.newFixedThreadPool(numThreads);
        for(int i=0; i<numThreads; i++) {
            runners.add(new Callable<Long> () {
                public Long call() throws Exception {
                    runner.run();
                    return -1L;
                }
            });
        }
        tpe.invokeAll(runners);
        tpe.shutdown();
        writeData(runner.getDuration());
        //writeData((double)(runner.getDuration() * 1000000) / (double)(runner.testIterations));
    }

    public List<TestRunner> getTestRunners(long testIterations) {
        List<TestRunner> runners = new ArrayList<TestRunner>();

        // Base case
        runners.add(new NullTestRunner(testIterations));

        // Plain synchronized block
        //runners.add(new SyncTestRunner(testIterations));

        // Plain AtomicLong
        //runners.add(new AtomicLongTestRunner(testIterations));

        createTestRunnerSet(runners, testIterations, false, false);
        createTestRunnerSet(runners, testIterations, false, true);
        createTestRunnerSet(runners, testIterations, true, false);
        createTestRunnerSet(runners, testIterations, true, true);
        return runners;
    }

    public void createTestRunnerSet(List<TestRunner> runners, long testIterations,
            boolean highConcurrency, boolean windowed) {
        String prefix = highConcurrency ? "hc_" : "lc_";
        prefix += windowed ? "w_" : "ub_";

        if(!windowed) {
            runners.add(new AccTestRunner(testIterations, prefix + "counter", createUnboundedCounter(highConcurrency)));
            runners.add(new AccTestRunner(testIterations, prefix + "min", createUnboundedMin(highConcurrency)));
            runners.add(new AccTestRunner(testIterations, prefix + "max", createUnboundedMax(highConcurrency)));
        } else {
            runners.add(new AccTestRunner(testIterations, prefix + "10s_counter", createWindowedCounter(highConcurrency, false, 1, 10)));
            runners.add(new AccTestRunner(testIterations, prefix + "2s_counter", createWindowedCounter(highConcurrency, true, 28, 5)));
            runners.add(new AccTestRunner(testIterations, prefix + "10s_min", createWindowedMin(highConcurrency, false, 1, 10)));
            runners.add(new AccTestRunner(testIterations, prefix + "2s_min", createWindowedMin(highConcurrency, true, 28, 5)));
            runners.add(new AccTestRunner(testIterations, prefix + "10s_max", createWindowedMax(highConcurrency, false, 1, 10)));
            runners.add(new AccTestRunner(testIterations, prefix + "2s_max", createWindowedMax(highConcurrency, true, 28, 5)));
        }
    }

    Accumulator createUnboundedCounter(boolean highConcurrency) {
        if(highConcurrency) {
            return new HighConcurrencyAccumulator(UnboundedAddMutator.MUTATOR_FACTORY);
        } else {
            return new LowConcurrencyAccumulator(org.smf4j.core.accumulator.lc.UnboundedAddMutator.MUTATOR_FACTORY);
        }
    }

    Accumulator createUnboundedMin(boolean highConcurrency) {
        if(highConcurrency) {
            return new HighConcurrencyAccumulator(UnboundedMinMutator.MUTATOR_FACTORY);
        } else {
            return new LowConcurrencyAccumulator(org.smf4j.core.accumulator.lc.UnboundedMinMutator.MUTATOR_FACTORY);
        }
    }

    Accumulator createUnboundedMax(boolean highConcurrency) {
        if(highConcurrency) {
            return new HighConcurrencyAccumulator(UnboundedMaxMutator.MUTATOR_FACTORY);
        } else {
            return new LowConcurrencyAccumulator(org.smf4j.core.accumulator.lc.UnboundedMaxMutator.MUTATOR_FACTORY);
        }
    }

    Accumulator createWindowedCounter(boolean highConcurrency, boolean powersOfTwo, int timeWindow, int intervals) {
        IntervalStrategy strategy;
        if(powersOfTwo) {
            strategy = new PowersOfTwoIntervalStrategy(timeWindow, intervals);
        } else {
            strategy = new SecondsIntervalStrategy(timeWindow, intervals);
        }

        if(highConcurrency) {
            return new HighConcurrencyAccumulator(new WindowedAddMutator.Factory(strategy));
        } else {
            return new LowConcurrencyAccumulator(new org.smf4j.core.accumulator.lc.WindowedAddMutator.Factory(strategy));
        }
    }

    Accumulator createWindowedMin(boolean highConcurrency, boolean powersOfTwo, int timeWindow, int intervals) {
        IntervalStrategy strategy;
        if(powersOfTwo) {
            strategy = new PowersOfTwoIntervalStrategy(timeWindow, intervals);
        } else {
            strategy = new SecondsIntervalStrategy(timeWindow, intervals);
        }

        if(highConcurrency) {
            return new HighConcurrencyAccumulator(new WindowedMinMutator.Factory(strategy));
        } else {
            return new LowConcurrencyAccumulator(new org.smf4j.core.accumulator.lc.WindowedMinMutator.Factory(strategy));
        }
    }

    Accumulator createWindowedMax(boolean highConcurrency, boolean powersOfTwo, int timeWindow, int intervals) {
        IntervalStrategy strategy;
        if(powersOfTwo) {
            strategy = new PowersOfTwoIntervalStrategy(timeWindow, intervals);
        } else {
            strategy = new SecondsIntervalStrategy(timeWindow, intervals);
        }

        if(highConcurrency) {
            return new HighConcurrencyAccumulator(new WindowedMaxMutator.Factory(strategy));
        } else {
            return new LowConcurrencyAccumulator(new org.smf4j.core.accumulator.lc.WindowedMaxMutator.Factory(strategy));
        }
    }

    void openFile(String fileName) throws IOException {
        FileWriter fw = new FileWriter(fileName + ".csv", false);
        bw = new BufferedWriter(fw);
    }

    void closeFile() throws IOException {
        bw.flush();
        bw.close();
    }

    boolean prependWithComma = false;
    void writeData(Object data) throws IOException {
        if(prependWithComma) {
            bw.write(",");
        }
        prependWithComma = true;
        bw.write(data.toString());
        bw.flush();
    }

    static final String newLine = String.format("%n");
    void newLine() throws IOException {
        bw.write(newLine);
        prependWithComma = false;
    }

    public static void main( String[] args ) throws IOException, InterruptedException
    {
        final long iterations = 10000000L;
        final int threads = Runtime.getRuntime().availableProcessors();
        App app = new App();
        app.go(iterations, threads);
    }
}
