/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.ft.sdk.sessionreplay.internal.persistence;

import androidx.annotation.WorkerThread;

import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PlainBatchFileReaderWriter implements BatchFileReaderWriter {

    private static final String TAG = "PlainBatchFileReaderWri";
    private final InternalLogger internalLogger;

    public PlainBatchFileReaderWriter(InternalLogger internalLogger) {
        this.internalLogger = internalLogger;
    }

    // region FileWriter+FileReader

    @WorkerThread
    @Override
    public boolean writeData(File file, RawBatchEvent data, boolean append) {
        try {
            lockFileAndWriteData(file, append, data);
            return true;
        } catch (IOException | SecurityException e) {
            internalLogger.e(TAG, String.format(Locale.US, ERROR_WRITE, file.getPath()), e);
            return false;
        }
    }

    @WorkerThread
    @Override
    public List<RawBatchEvent> readData(File file) {
        try {
            return readFileData(file);
        } catch (IOException | SecurityException e) {
            internalLogger.e(TAG, String.format(Locale.US, ERROR_READ, file.getPath()), e);
            return new ArrayList<>();
        }
    }


    @SuppressWarnings("UnsafeThirdPartyFunctionCall")
    private void lockFileAndWriteData(File file, boolean append, RawBatchEvent data) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file, append)) {
            FileChannel channel = outputStream.getChannel();
            FileLock lock = channel.lock();
            try {
                byte[] meta = data.getMetadata();

                int metaBlockSize = TYPE_SIZE_BYTES + LENGTH_SIZE_BYTES + meta.length;
                int dataBlockSize = TYPE_SIZE_BYTES + LENGTH_SIZE_BYTES + data.getData().length;

                // ByteBuffer by default has BigEndian ordering, which matches to how Java
                // reads data, so no need to define it explicitly
                ByteBuffer buffer = ByteBuffer
                        .allocate(metaBlockSize + dataBlockSize);
                putAsTlv(buffer, BlockType.META, meta);
                putAsTlv(buffer, BlockType.EVENT, data.getData());

                outputStream.write(buffer.array());
            } finally {
                lock.release();
            }
        } catch (IOException e) {
            throw new IOException(e); // Handle the exception as needed
        }
    }

    @SuppressWarnings("UnsafeThirdPartyFunctionCall")
    private List<RawBatchEvent> readFileData(File file) throws IOException {
        int inputLength = (int) file.length();

        List<RawBatchEvent> result = new ArrayList<>();

        int remaining = inputLength;
        try (InputStream inputStream = new FileInputStream(file)) {
            while (remaining > 0) {
                BlockReadResult metaReadResult = readBlock(inputStream, BlockType.META);
                if (metaReadResult.data == null) {
                    remaining -= metaReadResult.bytesRead;
                    break;
                }

                BlockReadResult eventReadResult = readBlock(inputStream, BlockType.EVENT);
                remaining -= metaReadResult.bytesRead + eventReadResult.bytesRead;

                if (eventReadResult.data == null) break;

                result.add(new RawBatchEvent(eventReadResult.data, metaReadResult.data));
            }
        }

        if (remaining != 0 || (inputLength > 0 && result.isEmpty())) {
            internalLogger.e(TAG, String.format(Locale.US, WARNING_NOT_ALL_DATA_READ, file.getPath()));
        }

        return result;
    }

    @SuppressWarnings("ReturnCount")
    private BlockReadResult readBlock(InputStream stream, BlockType expectedBlockType) throws IOException {
        ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_SIZE_BYTES);

        int headerReadBytes = stream.read(headerBuffer.array());

        if (!checkReadExpected(HEADER_SIZE_BYTES, headerReadBytes, "Block(" + expectedBlockType.name() + "): Header read")) {
            return new BlockReadResult(null, Math.max(0, headerReadBytes));
        }

        short blockType = headerBuffer.getShort();
        if (blockType != expectedBlockType.identifier) {
            internalLogger.e(TAG, "Unexpected block type identifier=" + blockType + " met, was expecting "
                    + expectedBlockType + "(" + expectedBlockType.identifier + ")");
            return new BlockReadResult(null, headerReadBytes);
        }

        int dataSize = headerBuffer.getInt();
        byte[] dataBuffer = new byte[dataSize];

        int dataReadBytes = stream.read(dataBuffer);

        if (checkReadExpected(dataSize, dataReadBytes, "Block(" + expectedBlockType.name() + "):Data read")) {
            return new BlockReadResult(dataBuffer, headerReadBytes + dataReadBytes);
        } else {
            return new BlockReadResult(null, headerReadBytes + Math.max(0, dataReadBytes));
        }
    }

    private boolean checkReadExpected(int expected, int actual, String operation) {
        if (expected != actual) {
            if (actual != -1) {
                internalLogger.e(TAG, "Number of bytes read for operation='" + operation
                        + "' doesn't match with expected: expected=" + expected + ", actual=" + actual);
            } else {
                internalLogger.e(TAG, "Unexpected EOF at the operation=" + operation);
            }
            return false;
        } else {
            return true;
        }
    }

    @SuppressWarnings("UnsafeThirdPartyFunctionCall")
    private ByteBuffer putAsTlv(ByteBuffer buffer, BlockType blockType, byte[] data) {
        return buffer
                .putShort(blockType.identifier)
                .putInt(data.length)
                .put(data);
    }

    private static class BlockReadResult {
        final byte[] data;
        final int bytesRead;

        BlockReadResult(byte[] data, int bytesRead) {
            this.data = data;
            this.bytesRead = bytesRead;
        }
    }

    private enum BlockType {
        EVENT((short) 0x00),
        META((short) 0x01);

        final short identifier;

        BlockType(short identifier) {
            this.identifier = identifier;
        }
    }

    // endregion

    // TLV (Type-Length-Value) constants
    private static final int TYPE_SIZE_BYTES = 2;
    private static final int LENGTH_SIZE_BYTES = 4;
    private static final int HEADER_SIZE_BYTES = TYPE_SIZE_BYTES + LENGTH_SIZE_BYTES;

    private static final String ERROR_WRITE = "Unable to write data to file: %s";
    private static final String ERROR_READ = "Unable to read data from file: %s";

    private static final String WARNING_NOT_ALL_DATA_READ = "File %s is probably corrupted, not all content was read.";
}
