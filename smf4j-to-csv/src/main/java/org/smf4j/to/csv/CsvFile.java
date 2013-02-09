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

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class CsvFile implements Closeable, Runnable, Callable<Boolean> {
    private static final String COMMA = ",";
    private static final String DEFAULT_QUOTE_CHARACTER = "\"";
    private static final String DEFAULT_FILE_TIMESTAMP_PATTERN =
            "yyyy-MM-dd-HH-mm-ss";
    private static final String DEFAULT_DATA_TIMESTAMP_PATTERN =
            "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_TIMESTAMP_COLUMN_HEADER =
            "Timestamp";
    private static final long DEFAULT_MAX_SIZE = 10*1024*1024;
    private static final String sysLineEnding = String.format("%n");

    private final Logger log = LoggerFactory.getLogger(CsvFile.class);
    private final ReentrantLock fileIoLock = new ReentrantLock();
    private final AtomicBoolean closed = new AtomicBoolean(true);
    private final AtomicBoolean apiClosed = new AtomicBoolean(false);

    private String path;
    private boolean timestampColumn = true;
    private String timestampColumnHeader = DEFAULT_TIMESTAMP_COLUMN_HEADER;
    private String lineEnding = sysLineEnding;
    private boolean append = true;
    private String delimeter = COMMA;
    private String quote = DEFAULT_QUOTE_CHARACTER;
    private String doubleQuote = quote + quote;
    private Charset charset = Charset.defaultCharset();
    private SimpleDateFormat rolloverTimestampPattern;
    private SimpleDateFormat columnTimestampPattern;
    private long maxSize = DEFAULT_MAX_SIZE;
    private CsvFileLayout layout;

    private boolean initializationFailure = false;
    private NumberFormat numberFormat = NumberFormat.getInstance();

    // w and cos can be accessed via other threads (specifically, shutdown
    // hooks ensuring that files are closed at JVM exit)
    private volatile File file;
    private volatile Writer w;
    private volatile CountingOutputStream cos;

    public CsvFile() {
        rolloverTimestampPattern = new SimpleDateFormat(
                DEFAULT_FILE_TIMESTAMP_PATTERN);
        columnTimestampPattern = new SimpleDateFormat(
                DEFAULT_DATA_TIMESTAMP_PATTERN);
    }

    public void write() {
        if(initializationFailure) {
            return;
        }

        // Grab the file-io lock
        fileIoLock.lock();
        try {
            if(apiClosed.get()) {
                // We're closed down
                return;
            }

            // Make sure file is ready for writing
            prepareFile();

            // Write the row
            writeRow();
        } catch(Throwable t) {
            log.error("Error caught while attempting to write row", t);
        } finally {
            fileIoLock.unlock();
        }
    }

    protected void prepareFile() {
        if(path == null || file == null) {
            setupFailed();
            throw new NullPointerException("path");
        }

        if(cos == null || w == null) {
            openFile();
        } else if(shouldRollover()) {
            rollover();
        }
    }

    protected boolean shouldRollover() {
        return cos.getCount() > maxSize;
    }

    protected void openFile() {
        if(file.isDirectory()) {
            setupFailed();
            String err = String.format(
                    "Cannot open file: '%s' is a directory.",
                    file.toString());
            log.error(err);
            return;
        }

        long startCount = 0L;
        if(file.exists() && append) {
            startCount = file.length();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            cos = new CountingOutputStream(fos);
            cos.setCount(startCount);
            w = new OutputStreamWriter(cos, charset);
            if(startCount == 0L) {
                writeHeader();
            }
        } catch(IOException e) {
            setupFailed();
            log.error(String.format(
                    "Failed to create file output stream for path '%s'.",
                    file.toString()),
                    e);
            if(w != null) { try { w.close(); } catch(IOException e1) {} }
            if(cos != null) { try { cos.close(); } catch(IOException e1) {} }
            if(fos != null) { try { fos.close(); } catch(IOException e1) {} }
            w = null;
            cos = null;
        }
    }

    protected void rollover() {
        // Close the original file
        closeFile();

        // Determine the rollover file name
        File renamed = timestampPath(file);
        if(!file.renameTo(renamed)) {
            log.error("Failed to move '{}' to '{}'.", file.getAbsolutePath(),
                    renamed.getAbsolutePath());
            setupFailed();
        }

        openFile();
    }

    protected void closeFile() {
        if(w != null) {
            try { w.close(); } catch(IOException e) {}
            w = null;
        }
        if(cos != null) {
            try { cos.close(); } catch(IOException e) {}
            cos = null;
        }
        closed.set(true);
    }

    protected File timestampPath(File file) {
        String timestamp = rolloverTimestampPattern.format(new Date());
        String oldPath = file.getAbsolutePath();
        StringBuilder newPath = new StringBuilder();
        int i = oldPath.lastIndexOf('.');
        if(i == -1) {
            newPath.append(oldPath);
            newPath.append("-");
            newPath.append(timestamp);
        } else {
            newPath.append(oldPath.substring(0, i));
            newPath.append("-");
            newPath.append(timestamp);
            newPath.append(oldPath.substring(i));
        }

        return new File(newPath.toString());
    }

    protected void setupFailed() {
        initializationFailure = true;
        cos = null;
        w = null;
    }

    protected void writeHeader()
    throws IOException {
        boolean prependDelimeter = false;
        StringBuilder sb = new StringBuilder();

        if(timestampColumn) {
            sb.append(escape(timestampColumnHeader));
            prependDelimeter = true;
        }

        // Call prepare() to force the layout to re-investigate the set
        // of nodes available in the registrar.
        layout.prepare();
        for(CsvDataColumn col : layout.getColumns()) {
            if(prependDelimeter) {
                sb.append(delimeter);
            }
            prependDelimeter = true;
            sb.append(escape(col.getColumnName()));
        }

        sb.append(lineEnding);
        w.write(sb.toString());
    }

    protected void writeRow()
    throws IOException {
        boolean prependDelimeter = false;
        StringBuilder sb = new StringBuilder();

        if(timestampColumn) {
            String timestamp = columnTimestampPattern.format(new Date());
            sb.append(escape(timestamp));
            prependDelimeter = true;
        }

        for(CsvDataColumn col : layout.getColumns()) {
            if(prependDelimeter) {
                sb.append(delimeter);
            }
            prependDelimeter = true;
            writeColumn(col, sb);
        }

        sb.append(lineEnding);
        w.write(sb.toString());
    }

    protected void writeColumn(CsvDataColumn col, StringBuilder sb) {
        Map<String, Object> snapshot = col.getNode().snapshot();
        Object val = col.getDatum(snapshot);
        String formatted = "";
        if(val != null) {
            if(val instanceof Number) {
                formatted = numberFormat.format(val);
            } else {
                formatted = val.toString();
            }
        }
        sb.append(escape(formatted));
    }

    protected String escape(String str) {
        if(!str.contains(delimeter) && !str.contains(quote)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(2 + str.length());
        sb.append(quote);
        // Gotta double-up quotes inside quotes.  That way, there's more quotes.
        sb.append(str.replace(quote, doubleQuote));
        sb.append(quote);
        return sb.toString();
    }

    @Override
    public void close() {
        fileIoLock.lock();
        try {
            if(apiClosed.get()) {
                // No need to take action if we're already closed.
                return;
            }

            apiClosed.getAndSet(true);
            closeFile();
        } finally {
            fileIoLock.unlock();
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
        this.file = new File(path);
    }

    public String getRolloverTimestampPattern() {
        return rolloverTimestampPattern.toPattern();
    }

    public void setRolloverTimestampPattern(String rolloverTimestampPattern) {
        this.rolloverTimestampPattern = new SimpleDateFormat(
                rolloverTimestampPattern);
    }

    public String getColumnTimestampPattern() {
        return columnTimestampPattern.toPattern();
    }

    public void setColumnTimestampPattern(String columnTimestampPattern) {
        this.columnTimestampPattern = new SimpleDateFormat(
                columnTimestampPattern);
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public CsvFileLayout getLayout() {
        return layout;
    }

    public void setLayout(CsvFileLayout layout) {
        this.layout = layout;
    }

    public boolean isAppend() {
        return append;
    }

    public boolean isTimestampColumn() {
        return timestampColumn;
    }

    public void setTimestampColumn(boolean timestampColumn) {
        this.timestampColumn = timestampColumn;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public String getCharset() {
        return charset.name();
    }

    public void setCharset(String charset) {
        Charset cs;
        if(charset == null || !Charset.isSupported(charset)) {
            log.warn("Unsupported charset '{}'.",
                    charset == null ? "(null)" : charset);
            cs = Charset.defaultCharset();
        } else {
            cs = Charset.forName(charset);
        }
        this.charset = cs;
    }

    public String getDelimeter() {
        return delimeter;
    }

    public void setDelimeter(String delimeter) {
        if(delimeter == null || delimeter.length() == 0) {
            delimeter = COMMA;
        }
        this.delimeter = delimeter;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        if(quote == null || quote.length() == 0) {
            quote = DEFAULT_QUOTE_CHARACTER;
        }
        this.quote = quote;
        this.doubleQuote = quote + quote;
    }

    public String getTimestampColumnHeader() {
        return timestampColumnHeader;
    }

    public void setTimestampColumnHeader(String timestampColumnHeader) {
        if(timestampColumnHeader == null) {
            timestampColumnHeader = DEFAULT_TIMESTAMP_COLUMN_HEADER;
        }
        this.timestampColumnHeader = timestampColumnHeader;
    }

    public String getLineEnding() {
        return lineEnding;
    }

    public void setLineEnding(String lineEnding) {
        this.lineEnding = lineEnding;
    }

    @Override
    public void run() {
        write();
    }

    @Override
    public Boolean call() throws Exception {
        write();
        return initializationFailure;
    }
}
