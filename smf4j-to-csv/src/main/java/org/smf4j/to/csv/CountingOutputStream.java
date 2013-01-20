/*
 * Copyright 2012 Russell Morris (wrussellmorris@gmail.com).
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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class CountingOutputStream extends FilterOutputStream {

    private AtomicLong numWritten = new AtomicLong();

    public CountingOutputStream(OutputStream out) {
        super(out);
        if(out == null) {
            throw new NullPointerException("inner");
        }
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        numWritten.incrementAndGet();
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
        numWritten.addAndGet(b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        numWritten.addAndGet(len);
    }

    public long getCount() {
        return numWritten.get();
    }

    public void setCount(long count) {
        this.numWritten.getAndSet(count);
    }
}
