package com.ft.sdk.sessionreplay.internal.net;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.zip.Adler32;
import java.util.zip.Inflater;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BytesCompressorTest {
    private final BytesCompressor compressor = new BytesCompressor();

    @Test
    public void compressBytes_roundTripsOneCompleteStreamAcrossBufferBoundaries() throws Exception {
        Random random = new Random(42);
        for (int size : new int[]{0, 1, 1023, 1024, 1025, 2048, 65536, 1000000}) {
            byte[] payload = new byte[size];
            random.nextBytes(payload);
            assertArrayEquals(payload, inflate(compressor.compressBytes(payload)));
        }
    }

    @Test
    public void compressBytes_supportsHighlyCompressibleLargePayloads() throws Exception {
        byte[] payload = new byte[1000000];
        assertArrayEquals(payload, inflate(compressor.compressBytes(payload)));
    }

    @Test
    public void compressBytes_segmentsCanBeJoinedWithoutRecompressingTheirPayloads() throws Exception {
        byte[][] payloads = {
                "{\"records\":[{\"type\":4}]}\n".getBytes(StandardCharsets.UTF_8),
                new byte[0],
                "{\"records\":[{\"type\":10}]}\n".getBytes(StandardCharsets.UTF_8)
        };
        ByteArrayOutputStream joined = new ByteArrayOutputStream();
        ByteArrayOutputStream expected = new ByteArrayOutputStream();
        Adler32 checksum = new Adler32();
        for (int index = 0; index < payloads.length; index++) {
            byte[] segment = compressor.compressBytes(payloads[index]);
            // Keep one zlib header and each byte-aligned deflate body, replacing the
            // per-segment trailers with one final block and the combined checksum.
            int offset = index == 0 ? 0 : BytesCompressor.HEADER_SIZE_IN_BYTES;
            joined.write(segment, offset,
                    segment.length - offset - BytesCompressor.CHECKSUM_FLAG_SIZE_IN_BYTES);
            expected.write(payloads[index]);
            checksum.update(payloads[index]);
        }
        joined.write(new byte[]{3, 0});
        for (int shift = 24; shift >= 0; shift -= 8) {
            joined.write((int) (checksum.getValue() >>> shift) & 0xff);
        }
        assertArrayEquals(expected.toByteArray(), inflate(joined.toByteArray()));
    }

    private byte[] inflate(byte[] compressed) throws Exception {
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(compressed);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[777];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                assertTrue("Compressed stream must finish without more input",
                        count > 0 || inflater.finished());
                output.write(buffer, 0, count);
            }
            assertEquals("No extra stream or trailing bytes", 0, inflater.getRemaining());
            return output.toByteArray();
        } finally {
            inflater.end();
        }
    }
}
