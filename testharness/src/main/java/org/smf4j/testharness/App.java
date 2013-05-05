package org.smf4j.testharness;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.smf4j.Accumulator;
import org.smf4j.Registrar;
import org.smf4j.RegistrarFactory;
import org.smf4j.core.accumulator.hc.HighContentionAccumulator;
import org.smf4j.core.accumulator.IntervalStrategy;
import org.smf4j.core.accumulator.lc.LowContentionAccumulator;
import org.smf4j.core.accumulator.PowersOfTwoIntervalStrategy;
import org.smf4j.core.accumulator.SecondsIntervalStrategy;
import org.smf4j.core.accumulator.hc.UnboundedAddMutator;
import org.smf4j.core.accumulator.hc.UnboundedMaxMutator;
import org.smf4j.core.accumulator.hc.UnboundedMinMutator;
import org.smf4j.core.accumulator.hc.WindowedAddMutator;
import org.smf4j.core.accumulator.hc.WindowedMaxMutator;
import org.smf4j.core.accumulator.hc.WindowedMinMutator;
import org.smf4j.testharness.temp.HPLAccumulator;
import org.smf4j.to.jmx.JmxRegistrarPublisher;

/**
 * Hello world!
 *
 */
public class App
{
    BufferedWriter bw;
    ExecutorService tpe;

    public void go(long testIterations, Integer[] numThreads) throws IOException, InterruptedException {
        List<TestRunner> runners = getTestRunners(testIterations);
        Registrar r = RegistrarFactory.getRegistrar();
        r.getRootNode().setOn(true);
        JmxRegistrarPublisher publisher = new JmxRegistrarPublisher();
        publisher.publish();

        // Run one pass of lower-concurrency tests to prime the app
        System.out.println("Priming...");
        runHighConcurrencyTests(runners, new Integer[] {1});

        // Now for the real tests
        System.out.println("Starting...");
        runHighConcurrencyTests(runners, numThreads);

        System.out.println("Done");
    }

    public void runHighConcurrencyTests(List<TestRunner> runners, Integer[] numThreads)
    throws IOException, InterruptedException {
        openFile("hc");

        writeData("writers");
        for(TestRunner runner : runners) {
            writeData(runner.getName());
        }
        newLine();

        for(int t : numThreads) {
            System.out.print(t + " threads");
            writeData(t);
            for(TestRunner runner : runners) {
                System.out.print(".");
                runHighConcurrencyTest(runner, t);
            }
            System.out.println();
            newLine();
        }
        closeFile();
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

        // Cliff Click's HPL Counter
        runners.add(new AccTestRunner(testIterations, "hpl_counter", new HPLAccumulator(), false));
        runners.add(new AccTestRunner(testIterations, "hc_ub_g_counter", new HighContentionAccumulator(UnboundedAddMutator.MUTATOR_FACTORY), false));
        runners.add(new AccTestRunner(testIterations, "hc_ub_p_counter", new HighContentionAccumulator(UnboundedAddMutator.MUTATOR_FACTORY), true));

        // Our smf4j-core counter implementations
        /*
        createTestRunnerSet(runners, testIterations, false, false, true);
        createTestRunnerSet(runners, testIterations, false, true, true);
        createTestRunnerSet(runners, testIterations, true, false, true);
        createTestRunnerSet(runners, testIterations, true, true, true);
        createTestRunnerSet(runners, testIterations, false, false, false);
        createTestRunnerSet(runners, testIterations, false, true, false);
        createTestRunnerSet(runners, testIterations, true, false, false);
        createTestRunnerSet(runners, testIterations, true, true, false);
        */
        return runners;
    }

    public void createTestRunnerSet(List<TestRunner> runners, long testIterations,
            boolean highConcurrency, boolean windowed, boolean privateMutator) {
        String prefix = highConcurrency ? "hc_" : "lc_";
        prefix += windowed ? "w_" : "ub_";
        prefix += privateMutator ? "p_" : "g_";

        if(!windowed) {
            runners.add(new AccTestRunner(testIterations, prefix + "counter", createUnboundedCounter(highConcurrency), privateMutator));
            runners.add(new AccTestRunner(testIterations, prefix + "min", createUnboundedMin(highConcurrency), privateMutator));
            runners.add(new AccTestRunner(testIterations, prefix + "max", createUnboundedMax(highConcurrency), privateMutator));
        } else {
            runners.add(new AccTestRunner(testIterations, prefix + "10s_counter", createWindowedCounter(highConcurrency, false, 1, 10), privateMutator));
            runners.add(new AccTestRunner(testIterations, prefix + "2s_counter", createWindowedCounter(highConcurrency, true, 28, 5), privateMutator));
            runners.add(new AccTestRunner(testIterations, prefix + "10s_min", createWindowedMin(highConcurrency, false, 1, 10), privateMutator));
            runners.add(new AccTestRunner(testIterations, prefix + "2s_min", createWindowedMin(highConcurrency, true, 28, 5), privateMutator));
            runners.add(new AccTestRunner(testIterations, prefix + "10s_max", createWindowedMax(highConcurrency, false, 1, 10), privateMutator));
            runners.add(new AccTestRunner(testIterations, prefix + "2s_max", createWindowedMax(highConcurrency, true, 28, 5), privateMutator));
        }
    }

    Accumulator createUnboundedCounter(boolean highConcurrency) {
        if(highConcurrency) {
            return new HighContentionAccumulator(UnboundedAddMutator.MUTATOR_FACTORY);
        } else {
            return new LowContentionAccumulator(org.smf4j.core.accumulator.lc.UnboundedAddMutator.MUTATOR_FACTORY);
        }
    }

    Accumulator createUnboundedMin(boolean highConcurrency) {
        if(highConcurrency) {
            return new HighContentionAccumulator(UnboundedMinMutator.MUTATOR_FACTORY);
        } else {
            return new LowContentionAccumulator(org.smf4j.core.accumulator.lc.UnboundedMinMutator.MUTATOR_FACTORY);
        }
    }

    Accumulator createUnboundedMax(boolean highConcurrency) {
        if(highConcurrency) {
            return new HighContentionAccumulator(UnboundedMaxMutator.MUTATOR_FACTORY);
        } else {
            return new LowContentionAccumulator(org.smf4j.core.accumulator.lc.UnboundedMaxMutator.MUTATOR_FACTORY);
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
            return new HighContentionAccumulator(new WindowedAddMutator.Factory(strategy));
        } else {
            return new LowContentionAccumulator(new org.smf4j.core.accumulator.lc.WindowedAddMutator.Factory(strategy));
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
            return new HighContentionAccumulator(new WindowedMinMutator.Factory(strategy));
        } else {
            return new LowContentionAccumulator(new org.smf4j.core.accumulator.lc.WindowedMinMutator.Factory(strategy));
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
            return new HighContentionAccumulator(new WindowedMaxMutator.Factory(strategy));
        } else {
            return new LowContentionAccumulator(new org.smf4j.core.accumulator.lc.WindowedMaxMutator.Factory(strategy));
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
        final int cores = Runtime.getRuntime().availableProcessors();
        App app = new App();

        Set<Integer> threadCounts = new HashSet<Integer>();
        threadCounts.add(1);
        threadCounts.add(cores/4);
        threadCounts.add(cores/2);
        threadCounts.add(cores/2 + cores/4);
        threadCounts.add(cores);

        Integer[] counts = threadCounts.toArray(new Integer[0]);
        Arrays.sort(counts);
        app.go(iterations, counts);
    }
}
