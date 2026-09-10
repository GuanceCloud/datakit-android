package com.ft.sdk;

import java.io.IOException;
import java.io.OutputStream;

/** Calls each original overload once; closing an upload never completes the response. */
final class FTURLConnectionOutputStream extends OutputStream {
    private final OutputStream source;
    private final FTURLConnectionDelegate delegate;

    FTURLConnectionOutputStream(OutputStream source, FTURLConnectionDelegate delegate) {
        this.source = source;
        this.delegate = delegate;
    }

    @Override public void write(int value) throws IOException {
        try { source.write(value); delegate.state.progress(0, 1); }
        catch (IOException e) { delegate.failure(e); throw e; }
    }

    @Override public void write(byte[] bytes) throws IOException {
        try { source.write(bytes); delegate.state.progress(0, bytes.length); }
        catch (IOException e) { delegate.failure(e); throw e; }
    }

    @Override public void write(byte[] bytes, int offset, int length) throws IOException {
        try { source.write(bytes, offset, length); delegate.state.progress(0, length); }
        catch (IOException e) { delegate.failure(e); throw e; }
    }

    @Override public void flush() throws IOException {
        try { source.flush(); delegate.state.progress(0, 0); }
        catch (IOException e) { delegate.failure(e); throw e; }
    }

    @Override public void close() throws IOException {
        try { source.close(); delegate.state.progress(0, 0); }
        catch (IOException e) { delegate.failure(e); throw e; }
    }
}
