package org.smf4j.testharness;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.smf4j.Registrar;
import org.smf4j.RegistrarFactory;
import org.smf4j.core.accumulator.Counter;
import org.smf4j.core.accumulator.MaxCounter;
import org.smf4j.core.accumulator.MinCounter;
import org.smf4j.core.accumulator.WindowedCounter;
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
        runHighConcurrencyTests(runners, numThreads);
        runLowConcurrencyTests(runners);

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
    }

    public List<TestRunner> getTestRunners(long testIterations) {
        List<TestRunner> runners = new ArrayList<TestRunner>();

        // Base case
        runners.add(new NullTestRunner(testIterations));

        // Plain synchronized block
        runners.add(new SyncTestRunner(testIterations));

        // Plain AtomicLong
        runners.add(new AtomicLongTestRunner(testIterations));

        // Counter
        runners.add(new AccTestRunner(testIterations, "Counter", new Counter()));

        // WindowedCounter (powers of ten)
        runners.add(new AccTestRunner(testIterations, "Windowed_10s", new WindowedCounter(1, 10, false)));

        // WindowedCounter (powers of two)
        runners.add(new AccTestRunner(testIterations, "Windowed_2s", new WindowedCounter(28, 5, true)));

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
        final long iterations = 10000000L;
        final int threads = Runtime.getRuntime().availableProcessors();
        App app = new App();
        app.go(iterations, threads);
    }
}
