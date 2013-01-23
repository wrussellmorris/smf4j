package org.smf4j.testharness;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.smf4j.core.accumulator.Counter;
import org.smf4j.core.accumulator.MaxCounter;
import org.smf4j.core.accumulator.MinCounter;
import org.smf4j.core.accumulator.MinOrMaxCounter;
import org.smf4j.core.accumulator.WindowedCounter;

/**
 * Hello world!
 *
 */
public class App
{
    ExecutorService tpe;
    BufferedWriter bw;

    public void go(long testIterations, int numThreads) throws IOException, InterruptedException {
        tpe = Executors.newFixedThreadPool(numThreads);

        List<TestRunner> runners = getTestRunners(testIterations);

        System.out.println("Starting...");
        runLowConcurrencyTests(runners);
        runHighConcurrencyTests(runners, numThreads);

        tpe.shutdown();
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

    public void runHighConcurrencyTest(final TestRunner runner, int threads) throws InterruptedException, IOException {

        List<Callable<Long>> runners = new ArrayList<Callable<Long>>();
        for(int i=0; i<threads; i++) {
            runners.add(new Callable<Long> () {
                public Long call() throws Exception {
                    runner.run();
                    return -1L;
                }
            });
        }

        tpe.invokeAll(runners);
        writeData(runner.getDuration());
    }

    public List<TestRunner> getTestRunners(long testIterations) {
        List<TestRunner> runners = new ArrayList<TestRunner>();

        // Base case
        runners.add(new NullTestRunner(testIterations));

        // Plain AtomicLong
        runners.add(new AtomicLongTestRunner(testIterations));

        // Counter
        runners.add(new AccTestRunner(testIterations, "Counter", new Counter()));

        // WindowedCounter (powers of ten)
        runners.add(new AccTestRunner(testIterations, "WindowedCounter - powers of ten", new WindowedCounter(1, 10, false)));

        // WindowedCounter (powers of two)
        runners.add(new AccTestRunner(testIterations, "WindowedCounter - powers of two", new WindowedCounter(24, 4, true)));

        // MinMax (min)
        MinOrMaxCounter min = new MinOrMaxCounter();
        min.setMax(false);
        runners.add(new AccTestRunner(testIterations, "MinOrMaxCounter - min", min));

        // MinMax (min)
        MinOrMaxCounter max = new MinOrMaxCounter();
        min.setMax(true);
        runners.add(new AccTestRunner(testIterations, "MinOrMaxCounter - max", max));

        // MinCounter
        runners.add(new AccTestRunner(testIterations, "MinCounter", new MinCounter()));

        // MaxCounter
        runners.add(new AccTestRunner(testIterations, "MaxCounter", new MaxCounter()));
        return runners;
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
        final long iterations = 100000000L;
        final int threads = Runtime.getRuntime().availableProcessors();
        App app = new App();
        app.go(iterations, threads);
    }
}
