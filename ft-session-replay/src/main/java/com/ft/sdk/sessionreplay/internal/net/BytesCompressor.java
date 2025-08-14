package com.ft.sdk.sessionreplay.internal.net;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.io.IOException;

public class BytesCompressor {

    public byte[] compressBytes(byte[] uncompressedData) throws IOException {
        // Create the compressor with highest level of compression.
        Deflater deflater = new Deflater(COMPRESSION_LEVEL);
        // We will start with an OutputStream double the size of the data
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(uncompressedData.length * 2);
        // Compress the data
        // in order to align with dogweb way of decompressing the segments we need to compress
        // using the SYNC_FLUSH flag which adds the 0000FFFF flag at the end of the
        // compressed data
        compress(deflater, uncompressedData, outputStream, Deflater.SYNC_FLUSH);
        // in order to align with dogweb way of decompressing the segments we need to add
        // a fake checksum at the end
        compress(deflater, new byte[0], outputStream, Deflater.FULL_FLUSH);
        deflater.end();
        // Get the compressed data
        return outputStream.toByteArray();
    }

    private void compress(
        Deflater deflater,
        byte[] data,
        ByteArrayOutputStream output,
        int flag
    ) throws IOException {
        deflater.reset();
        deflater.setInput(data);
        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            output.write(buffer, 0, count);
        }
    }

    public static final int HEADER_SIZE_IN_BYTES = 2;
    public static final int SYNC_FLAG_SIZE_IN_BYTES = 4;
    public static final int CHECKSUM_FLAG_SIZE_IN_BYTES = 6;

    // We are using compression level 6 in order to align with the same compression type used
    // in the browser sdk.
    private static final int COMPRESSION_LEVEL = 6;
}
