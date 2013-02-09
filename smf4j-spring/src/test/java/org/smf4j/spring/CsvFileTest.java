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
package org.smf4j.spring;

import static org.junit.Assert.*;
import static org.smf4j.spring.TestUtils.*;

import org.junit.Test;
import org.smf4j.to.csv.CsvFile;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class CsvFileTest {
    private final Class<CsvFileTest> c = CsvFileTest.class;

    @Test
    public void defaults()
    throws Exception {
        ApplicationContext context = loadContext("csv-export.xml", c);

        CsvFile f = context.getBean("defaults", CsvFile.class);
        assertNotNull(f);
        assertEquals("foo", f.getPath());
    }

    @Test
    public void all()
    throws Exception {
        ApplicationContext context = loadContext("csv-export.xml", c);

        CsvFile f = context.getBean("all", CsvFile.class);
        assertNotNull(f);
        assertEquals("foo", f.getPath());
        assertTrue(f.isTimestampColumn());
        assertEquals("timestampColumnHeader", f.getTimestampColumnHeader());
        assertEquals("HH-mm-ss", f.getRolloverTimestampPattern());
        assertEquals("HH:mm:ss", f.getColumnTimestampPattern());
        assertEquals("\n", f.getLineEnding());
        assertFalse(f.isAppend());
        assertEquals("\t", f.getDelimeter());
        assertEquals("'", f.getQuote());
        assertEquals("UTF-8", f.getCharset());
        assertEquals(123456L, f.getMaxSize());
    }
}
