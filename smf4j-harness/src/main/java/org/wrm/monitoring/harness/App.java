package org.wrm.monitoring.harness;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wrm.monitoring.core.*;

/**
 * Hello world!
 *
 */
public class App {

    private static final long BILLION = 1000000000L;
    private static PrintStream printStream = null;

    public static void main(String[] args) throws Exception {
        //lvTest();
        //owcTest();
        //nwcTest();
        //twcTest();
        //times();
        int passes = 1;
        int duration = 2;
        int maxNumThreads = Math.max(Runtime.getRuntime().availableProcessors()-1,1);
        String downloadUrl = "ftp://anonymous:none@localhost/test.dat";

        //runAllCounterTests(passes, duration, maxNumThreads);

        //runAllLongValueTests(passes, duration, maxNumThreads);

        maxNumThreads = Math.max(Runtime.getRuntime().availableProcessors()/2,1);
        runAllDownloadTests(downloadUrl, passes, maxNumThreads);
    }

    static void wcTest() {
        WindowedCounter counter = new WindowedCounter(31, 29, true);
        counter.setOn(true);
        //NewWindowedCounterWorker worker = new NewWindowedCounterWorker(Long.MAX_VALUE, counter);
        //worker.call();
        while(true) {
            counter.incr();
        }
    }

    static void lvTest()
    throws Exception {
        final long iterations = 1000000000;
        List<Callable<Long>> callables = new ArrayList<Callable<Long>>();
        long start = System.nanoTime();
        for(int i=1; i<=4; i++) {
            callables.add(new Callable<Long>() {
                @Override
                public Long call() throws Exception {
                    AtomicLongValue val = new AtomicLongValue();
                    long count = 0;
                    while(count < iterations) {
                        count++;
                        val.incr();
                    }
                    return val.syncGet();
                }
            });
        }
        long count = runCountingTest(callables);
        long duration = System.nanoTime() - start;
        System.out.format("%d iterations in %d seconds: %f/s%n",count,duration/1000000000L,(double)count/(double)duration*1000000000L);
    }

    static void runAllCounterTests(int passes, int duration, int maxNumThreads) throws FileNotFoundException {
        for (int i = 0; i < passes; i++) {
            runAllCounterTests(i, duration, maxNumThreads, false);
            runAllCounterTests(i, duration, maxNumThreads, true);
        }
    }

    static void runAllCounterTests(int pass, int durationSeconds, int maxNumThreads, boolean highContention) throws FileNotFoundException {
        setupFile("TL-WindowCounterTests-" + (highContention ? "hc-" : "lc-") + pass);
        printHeader(maxNumThreads);
        //runTests("Raw", new RawFactory(), maxNumThreads, durationSeconds);
        runCountingTests("Shared AtomicLong", new AtomicLongFactory(highContention), maxNumThreads, durationSeconds);
        runCountingTests("Counter", new CounterFactory(highContention), maxNumThreads, durationSeconds);
        //runTests("MinCounter", new MinOrMaxFactory(highContention, false), maxNumThreads, durationSeconds);
        //runTests("MaxCounter", new MinOrMaxFactory(highContention, true), maxNumThreads, durationSeconds);
        runCountingTests("WindowedCounter", new WindowedCounterFactory(highContention), maxNumThreads, durationSeconds);
        closeFile();
    }

    static void runAllLongValueCountingTests(int passes, int duration, int maxNumThreads) throws FileNotFoundException {
        for (int i = 0; i <= passes; i++) {
            runAllLongValueCountingTests(i, duration, maxNumThreads, false);
            runAllLongValueCountingTests(i, duration, maxNumThreads, true);
        }
    }

    static void runAllLongValueCountingTests(int pass, int durationSeconds, int maxNumThreads, boolean highContention) throws FileNotFoundException {
        if(highContention) {
            setupFile("TLLongValueTests-hc-" + pass);
        } else {
            setupFile("TLLongValueTests-lc-" + pass);
        }
        printHeader(maxNumThreads);
        runCountingTests("Raw", new RawFactory(), maxNumThreads, durationSeconds);
        runCountingTests("LongValue", new LongValueFactory(highContention), maxNumThreads, durationSeconds);
        closeFile();
    }

   static void runCountingTests(String desc, CountingWorkerFactory factory, int maxThreads, int duration) {
        printTestStart(desc);
        for (int i = 1; i <= maxThreads; i++) {
            factory.setup();
            printResult(runCountingTest(factory, duration, i) / duration);
            factory.reset();
        }
        printTestEnd();
    }

    static long runCountingTest(CountingWorkerFactory factory, int duration, int numThreads) {
        long stopAfter = System.nanoTime() + asNanos(duration);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            CountingWorker worker = factory.createWorker(stopAfter);
            executor.submit(worker);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            printFailure();
        }

        return factory.getCount();
    }

    static long runCountingTest(List<Callable<Long>> callables) {
        ExecutorService executor = Executors.newFixedThreadPool(callables.size());
        List<Future<Long>> futures = new ArrayList<Future<Long>>();
        for (int i = 0; i < callables.size(); i++) {
            futures.add(executor.submit(callables.get(i)));
        }
        executor.shutdown();

        long count = 0;
        for(Future<Long> future : futures) {
            try {
                count += future.get();
            } catch (InterruptedException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            printFailure();
        }

        return count;
    }

    static void runAllDownloadTests(String downloadUrl, int pass, int maxNumThreads) throws FileNotFoundException {
        setupFile("DLTests-hc-" + pass);
        printHeader(maxNumThreads);

        Downloader raw = new Downloader(downloadUrl) {
            @Override
            void bytesRead(long count) {
            }
        };
        runDownloadTests(null, raw, maxNumThreads);
        runDownloadTests("Raw", raw, maxNumThreads);

        WindowedCounter wcsec = new WindowedCounter(2,10,false);
        wcsec.setOn(true);
        runDownloadTests("WC-sec", new WCDownloader(downloadUrl,wcsec), maxNumThreads);

        WindowedCounter wc2s = new WindowedCounter(31,28,true);
        wc2s.setOn(true);
        runDownloadTests("WC-2s", new WCDownloader(downloadUrl,wc2s), maxNumThreads);

        closeFile();
    }

    static void runDownloadTests(String name, Downloader downloader, int maxNumThreads) {
        if(name != null) {
            printTestStart(name);
        }
        for(int i=0; i<maxNumThreads; i++) {
            long result = runDownloadTest(downloader, i+1);
            if(name != null) {
                printResult(result);
            }
        }
        if(name != null) {
            printTestEnd();
        }
    }

    static long runDownloadTest(Downloader downloader, int numThreads) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        long start = System.nanoTime();
        for (int i = 0; i < numThreads; i++) {
            executor.submit(downloader);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            printFailure();
        }

        return (System.nanoTime() - start) / 1000000L;
    }

    static long asNanos(int seconds) {
        return seconds * BILLION;
    }

    static void printHeader(int numThreads) {
        printStream.print("Counter Type");
        for (int i = 0; i < numThreads; i++) {
            printStream.format(",%d Threads", i + 1);
        }
        printStream.println();
    }

    static void printTestStart(String desc) {
        printStream.format(desc);
    }

    static void printResult(long count) {
        printStream.format(",%d", count);
    }

    static void printTestEnd() {
        printStream.println();
    }

    static void printFailure() {
        printStream.print(",");

    }

    static void setupFile(String test) throws FileNotFoundException {
        String userDirProp = System.getProperty("user.home");
        File file = new File(userDirProp, test + ".csv");
        printStream = new PrintStream(file);
    }

    static void closeFile() {
        printStream.close();
    }

    static void times() {
        long ams = 1000000L;
        long as = 1000L * ams;
        long am = as * 60L;
        long ah = am * 60L;
        long ad = ah * 24L;
        for(long i=28L; i<50L; i++) {
            long l = 1L<<i;
            long d = l / ad;
            l -= d*ad;
            long h = l / ah;
            l -= h*ah;
            long m = l / am;
            l -= m*am;
            long s = l / as;
            l -= s*as;
            long ms = l / ams;
            l-= ms*ams;
            long ns = l;

            boolean started = false;
            System.out.print(i + ": ");
            if(d != 0) {
                System.out.format("%dd ", d);
                started = true;
            }
            if(h != 0 || started) {
                System.out.format("%dh ", h);
                started = true;
            }
            if(m != 0 || started) {
                System.out.format("%dm ", m);
                started = true;
            }
            if(s != 0 || started) {
                System.out.format("%ds ", s);
                started = true;
            }
            if(ms != 0 || started) {
                System.out.format("%dms ", ms);
            }
            System.out.println();
        }
    }
}
