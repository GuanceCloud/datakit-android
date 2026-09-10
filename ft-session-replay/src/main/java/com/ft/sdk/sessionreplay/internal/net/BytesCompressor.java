package com.ft.sdk.sessionreplay.internal.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

public class BytesCompressor {

    public byte[] compressBytes(byte[] uncompressedData) throws IOException {
        Deflater deflater = new Deflater(COMPRESSION_LEVEL);
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            deflater.setInput(uncompressedData);

            // Replay segments must end on a byte boundary so their deflate blocks can
            // be joined after removing each segment's zlib header and final trailer.
            int count;
            do {
                count = deflater.deflate(buffer, 0, buffer.length, Deflater.SYNC_FLUSH);
                outputStream.write(buffer, 0, count);
            } while (count == buffer.length);

            // Finish the same stream: an empty final block and this payload's checksum.
            // Resetting here would append a second zlib stream and break segment joins.
            deflater.finish();
            while (!deflater.finished()) {
                count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        } finally {
            deflater.end();
        }
    }

    public static final int HEADER_SIZE_IN_BYTES = 2;
    public static final int SYNC_FLAG_SIZE_IN_BYTES = 4;
    public static final int CHECKSUM_FLAG_SIZE_IN_BYTES = 6;

    // Match the compression level used in the Browser SDK.
    private static final int COMPRESSION_LEVEL = 6;
}
