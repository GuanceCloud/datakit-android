package com.ft.sdk.garble.compress;

import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import okio.Buffer;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * 
 * In order to be compatible with the Sink interface of okhttp 3.12.0, 
 * it is necessary to implement a DeflaterSink class here
 * 
 */
class DeflaterSink extends ForwardingSink {

    private final DeflaterOutputStream deflaterOutputStream;

    public DeflaterSink(Sink sink, Deflater deflater) {
        super(sink);
        this.deflaterOutputStream = new DeflaterOutputStream(Okio.buffer(sink).outputStream(), deflater);
    }

    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        byte[] bytes = source.readByteArray(byteCount);
        deflaterOutputStream.write(bytes);
    }

    @Override
    public void close() throws IOException {
        deflaterOutputStream.close();
        super.close();
    }
}