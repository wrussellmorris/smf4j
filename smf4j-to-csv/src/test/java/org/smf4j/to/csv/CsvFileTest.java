/*
 * Copyright 2013 Russell Morris (wrussellmorris@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smf4j.to.csv;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.smf4j.Accumulator;
import org.smf4j.Calculator;
import org.smf4j.Registrar;
import org.smf4j.RegistrarFactory;
import org.smf4j.RegistrarFactoryForUnitTests;
import org.smf4j.RegistryNode;
import org.smf4j.core.accumulator.hc.HighContentionAccumulator;
import org.smf4j.core.accumulator.MutatorFactory;
import org.smf4j.core.accumulator.SecondsIntervalStrategy;
import org.smf4j.core.accumulator.hc.WindowedAddMutator;
import org.smf4j.core.calculator.Frequency;
import org.smf4j.core.calculator.Normalizer;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class CsvFileTest {

    private CsvFile csvFile;

    @Before
    public void before() {
        RegistrarFactoryForUnitTests.reset(true);
    }

    @After
    public void after() {
        if(csvFile != null) {
            csvFile.close();
            csvFile = null;
        }
    }

    @Test
    public void createFile()
    throws Exception {
        File tempFile = createTempFile();
        csvFile = createCsvFile(tempFile, false);

        assertFalse(tempFile.isFile());

        csvFile.write();

        assertTrue(tempFile.isFile());
    }

    @Test
    public void rollover()
    throws Exception {
        File tempFile = createTempFile();
        tempFile.deleteOnExit();
        csvFile = createCsvFile(tempFile, true);
        csvFile.setRolloverTimestampPattern("'ROLLOVER'");
        File rolloverFile = csvFile.timestampPath(tempFile);
        rolloverFile.deleteOnExit();

        assertTrue(tempFile.createNewFile());
        assertTrue(tempFile.isFile());
        assertFalse(rolloverFile.isFile());

        csvFile.write();
        csvFile.write();

        assertTrue(tempFile.isFile());
        assertTrue(rolloverFile.isFile());
    }

    @Test
    public void append()
    throws Exception {
        File tempFile = createTempFile();
        tempFile.deleteOnExit();
        csvFile = createCsvFile(tempFile, false);
        csvFile.setAppend(true);
        File rolloverFile = csvFile.timestampPath(tempFile);
        rolloverFile.deleteOnExit();

        assertTrue(tempFile.createNewFile());
        assertTrue(tempFile.isFile());
        assertFalse(rolloverFile.isFile());

        csvFile.write();
        csvFile.write();

        assertTrue(tempFile.isFile());
        assertFalse(rolloverFile.isFile());
    }

    @Test
    public void noAppend()
    throws Exception {
        File tempFile = createTempFile();
        tempFile.deleteOnExit();
        csvFile = createCsvFile(tempFile, false);
        csvFile.setAppend(false);
        File rolloverFile = csvFile.timestampPath(tempFile);
        rolloverFile.deleteOnExit();

        assertTrue(tempFile.createNewFile());
        assertTrue(tempFile.isFile());
        assertFalse(rolloverFile.isFile());

        csvFile.write();
        csvFile.write();

        assertTrue(tempFile.isFile());
        assertTrue(rolloverFile.isFile());
    }

    @Test
    public void timestampColumn()
    throws Exception {
        assertTimestampColumn(true, "Foo");
    }

    @Test
    public void noTimestampColumn()
    throws Exception {
        assertTimestampColumn(false, "Foo");
    }

    void assertTimestampColumn(boolean has, String name)
    throws Exception {
        File tempFile = createTempFile();
        csvFile = createCsvFile(tempFile, false);
        csvFile.setTimestampColumn(has);
        csvFile.setTimestampColumnHeader(name);
        csvFile.write();
        csvFile.close();

        assertTrue(tempFile.isFile());

        FileReader r = null;
        BufferedReader br = null;
        try {
            r = new FileReader(tempFile);
            br = new BufferedReader(r);
            assertTrue(has == br.readLine().startsWith(name));
        } finally {
            try {if(br != null) {br.close();}} catch(IOException e) {}
            try {if(r != null) {r.close();}} catch(IOException e) {}
        }
    }

    @Test
    public void quote()
    throws Exception {
        File tempFile = createTempFile();
        csvFile = createCsvFile(tempFile, false);
        csvFile.close();

        csvFile.setDelimeter(",");
        csvFile.setQuote("\"");
        assertEquals("foo", csvFile.escape("foo"));
        assertEquals("\"foo,bar\"", csvFile.escape("foo,bar"));
        assertEquals("\"foo\"\"bar\"", csvFile.escape("foo\"bar"));
    }

    @Test
    public void delimeter()
    throws Exception {
        File tempFile = createTempFile();
        csvFile = createCsvFile(tempFile, false);
        csvFile.setTimestampColumn(true);
        csvFile.write();
        csvFile.close();

        String[] lines = readLines(tempFile);
        assertTrue(lines.length > 0);
        assertTrue(lines[0].startsWith(csvFile.getTimestampColumnHeader() +
                csvFile.getDelimeter()));
    }

    String[] readLines(File file)
    throws Exception {
        FileReader r = null;
        BufferedReader br = null;
        List<String> strs = new ArrayList<String>();
        try {
            r = new FileReader(file);
            br = new BufferedReader(r);
            String line;
            do {
                line = br.readLine();
                if(line != null) {strs.add(line);}
            } while(line != null);
            return strs.toArray(new String[strs.size()]);
        } finally {
            try { if(r != null) {r.close();} } catch(Exception e) {}
            try { if(br != null) {br.close();} } catch(Exception e) {}
        }
    }

    CsvFile createCsvFile(File file, final boolean forceRollover)
    throws Exception {
        CsvFile temp = new CsvFile() {
            @Override
            protected boolean shouldRollover() {
                return forceRollover;
            }
        };

        Registrar r = createRegistrar();
        CsvFileLayout layout = createLayout(r);
        temp.setLayout(layout);
        temp.setPath(file.getAbsolutePath());

        return temp;
    }

    File createTempFile()
    throws Exception {
        File tempFile = File.createTempFile("test", "tmp");
        tempFile.delete();
        tempFile.deleteOnExit();
        return tempFile;
    }

    Registrar createRegistrar()
    throws Exception {
        Registrar r = RegistrarFactory.getRegistrar();

        RegistryNode totalsOne = r.getNode("totals.one");
        RegistryNode totalsTwo = r.getNode("totals.two");
        RegistryNode ratesOne = r.getNode("rate.one");

        createTotalsNodeMembers(totalsOne, 1);
        createTotalsNodeMembers(totalsTwo, 2);
        createRatesNodeMembers(ratesOne);

        return r;
    }

    void createTotalsNodeMembers(RegistryNode node, long val) {
        node.register("a", new MockAccumulator(val));
        node.register("b", new MockAccumulator(val));
        node.register("c", new MockAccumulator(val));
        node.register("sum", new MockAccumulator(3*val));
    }

    void createRatesNodeMembers(RegistryNode node) {
        MutatorFactory mf = new WindowedAddMutator.Factory(
                new SecondsIntervalStrategy(10, 10));
        node.register("x", new HighContentionAccumulator(mf));
        node.register("y", new HighContentionAccumulator(mf));

        Normalizer x_per_second = new Normalizer();
        x_per_second.setAccumulator("x");
        x_per_second.setFrequency(Frequency.SECONDS);
        node.register("x_per_second", x_per_second);

        Normalizer y_per_second = new Normalizer();
        y_per_second.setAccumulator("y");
        y_per_second.setFrequency(Frequency.SECONDS);
        node.register("y_per_second", y_per_second);
    }

    static class SumAccumulators implements Calculator {
        public Long calculate(Map<String, Long> values,
                Map<String, Accumulator> accumulators) {
            long total = 0;
            for(Accumulator acc : accumulators.values()) {
                total += acc.get();
            }
            return total;
        }

        public String getUnits() {
            return null;
        }
    }

    CsvFileLayout createLayout(Registrar r) {
        CsvFileLayout layout = new CsvFileLayout();
        List<String> filters = new ArrayList<String>();
        filters.add("**");
        layout.setFilters(filters);
        return layout;
    }
}
