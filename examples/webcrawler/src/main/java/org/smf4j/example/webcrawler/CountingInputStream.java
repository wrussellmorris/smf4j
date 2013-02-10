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
package org.smf4j.example.webcrawler;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.smf4j.Accumulator;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class CountingInputStream extends FilterInputStream {
    private final Accumulator accumulator;
    CountingInputStream(Accumulator accumulator, InputStream in) {
        super(in);
        this.accumulator = accumulator;
    }

    @Override
    public int read() throws IOException {
        accumulator.getMutator().put(1);
        return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        accumulator.getMutator().put(b.length);
        return super.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        accumulator.getMutator().put(len);
        return super.read(b, off, len);
    }
}
