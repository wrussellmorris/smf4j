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

import org.smf4j.to.csv.CountingOutputStream;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.junit.Test;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class CountingOutputStreamTest {

    @Test
    public void counting()
    throws Exception {
        Charset charset = Charset.forName("UTF-8");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CountingOutputStream cos = new CountingOutputStream(baos);
        OutputStreamWriter w = new OutputStreamWriter(cos, charset);

        String test = "The test string...";
        ByteBuffer bb = charset.encode(test);
        int len = bb.limit() - bb.arrayOffset();

        w.write(test);
        w.flush();
        assertEquals(len, baos.size());
        assertEquals(len, cos.getCount());

        w.write(test);
        w.flush();
        assertEquals(2*len, baos.size());
        assertEquals(2*len, cos.getCount());

        cos.setCount(0L);
        w.write(test);
        w.flush();
        assertEquals(3*len, baos.size());
        assertEquals(len, cos.getCount());
    }
}
