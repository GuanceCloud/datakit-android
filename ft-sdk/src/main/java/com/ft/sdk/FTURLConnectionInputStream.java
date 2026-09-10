package com.ft.sdk;

import java.io.IOException;
import java.io.InputStream;

/** Counts the highest observed stream position, so mark/reset does not double-count bytes. */
final class FTURLConnectionInputStream extends InputStream {
    private final InputStream source;
    private final FTURLConnectionDelegate delegate;
    private long position;
    private long maximum;
    private long markedPosition;
    private boolean marked;

    FTURLConnectionInputStream(InputStream source, FTURLConnectionDelegate delegate) {
        this.source = source;
        this.delegate = delegate;
    }

    @Override public int read() throws IOException {
        try {
            int value = source.read();
            readResult(value < 0 ? -1 : 1);
            return value;
        } catch (IOException e) { delegate.failure(e); throw e; }
    }

    @Override public int read(byte[] bytes) throws IOException {
        try {
            int count = source.read(bytes);
            readResult(count);
            return count;
        } catch (IOException e) { delegate.failure(e); throw e; }
    }

    @Override public int read(byte[] bytes, int offset, int length) throws IOException {
        try {
            int count = source.read(bytes, offset, length);
            readResult(count);
            return count;
        } catch (IOException e) { delegate.failure(e); throw e; }
    }

    private void readResult(int count) {
        if (count == -1) delegate.state.finish("eof", true, null);
        else advance(count);
    }

    private void advance(long count) {
        synchronized (FTURLConnectionResourceReporter.LOCK) {
            if (count <= 0) return;
            position = Long.MAX_VALUE - position < count ? Long.MAX_VALUE : position + count;
            long additional = Math.max(0, position - maximum);
            maximum = Math.max(maximum, position);
            delegate.state.progress(additional, 0);
        }
    }

    @Override public long skip(long count) throws IOException {
        try {
            long skipped = source.skip(count);
            advance(skipped); // Zero is never evidence of EOF.
            return skipped;
        } catch (IOException e) { delegate.failure(e); throw e; }
    }

    @Override public int available() throws IOException {
        return source.available();
    }

    @Override public void close() throws IOException {
        try {
            source.close();
            delegate.state.finish("closed", false, null);
        } catch (IOException e) { delegate.failure(e); throw e; }
    }

    @Override public void mark(int readLimit) {
        source.mark(readLimit);
        synchronized (FTURLConnectionResourceReporter.LOCK) {
            markedPosition = position;
            marked = source.markSupported();
        }
    }

    @Override public void reset() throws IOException {
        source.reset(); // Unsupported/invalid reset is a usage error, not a network failure.
        synchronized (FTURLConnectionResourceReporter.LOCK) {
            if (marked) position = markedPosition;
            else {
                position = 0;
                delegate.state.sizeKnown = false;
            }
        }
        delegate.state.progress(0, 0);
    }

    @Override public boolean markSupported() {
        return source.markSupported();
    }
}
