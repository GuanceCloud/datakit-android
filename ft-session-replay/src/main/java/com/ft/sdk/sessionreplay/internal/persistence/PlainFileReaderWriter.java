package com.ft.sdk.sessionreplay.internal.persistence;

import androidx.annotation.WorkerThread;

import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * A plain implementation of FileReaderWriter that handles reading and writing of ByteArray data.
 */
public class PlainFileReaderWriter implements FileReaderWriter {

    private static final String TAG = "PlainFileReaderWriter";
    private final InternalLogger internalLogger;

    public PlainFileReaderWriter(InternalLogger internalLogger) {
        this.internalLogger = internalLogger;
    }

    @WorkerThread
    @Override
    public boolean writeData(File file, byte[] data, boolean append) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file, append);
            outputStream.write(data);
            return true;
        } catch (IOException | SecurityException e) {
            internalLogger.e(TAG, String.format(Locale.US, ERROR_WRITE, file.getPath()), e);
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    internalLogger.e(TAG, String.format(Locale.US, ERROR_WRITE, file.getPath()), e);
                }
            }
        }
    }

    @WorkerThread
    @Override
    public byte[] readData(File file) {
        FileInputStream inputStream = null;
        try {
            if (!file.exists() || file.isDirectory()) {
                internalLogger.e(TAG, String.format(ERROR_READ, file.getPath()));
                return EMPTY_BYTE_ARRAY;
            }

            inputStream = new FileInputStream(file);
            return readBytesFromStream(inputStream);
        } catch (IOException | SecurityException e) {
            internalLogger.e(TAG, String.format(ERROR_READ, file.getPath()), e);
            return EMPTY_BYTE_ARRAY;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    internalLogger.e(TAG, String.format(ERROR_READ, file.getPath()), e);
                }
            }
        }
    }

    private byte[] readBytesFromStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private static final String ERROR_WRITE = "Unable to write data to file: %s";
    private static final String ERROR_READ = "Unable to read data from file: %s";
}
