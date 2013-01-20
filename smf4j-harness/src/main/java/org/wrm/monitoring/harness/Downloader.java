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
package org.wrm.monitoring.harness;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public abstract class Downloader implements Callable<Long>{

    private final String downloadUrl;

    public Downloader(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public Long call() throws Exception {
        URL url = new URL(downloadUrl);
        URLConnection con = url.openConnection();
        InputStream is = con.getInputStream();
        final int BUFFER_SIZE = 1<<16;
        byte[] buff = new byte[BUFFER_SIZE];
        int lastRead = 0;
        do {
            lastRead = is.read(buff);
            bytesRead(lastRead);
        } while(lastRead > 0);

        is.close();
        return 0L;
    }

    abstract void bytesRead(long count);
}
