package com.ft.sdk.sessionreplay.internal.persistence;

import androidx.annotation.WorkerThread;

import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.io.File;

public class EncryptedFileReaderWriter implements FileReaderWriter {
    private static final String TAG = "EncryptedFileReaderWrit";
    private final Encryption encryption;
    private final FileReaderWriter delegate;
    private final InternalLogger internalLogger;

    public EncryptedFileReaderWriter(Encryption encryption, FileReaderWriter delegate, InternalLogger internalLogger) {
        this.encryption = encryption;
        this.delegate = delegate;
        this.internalLogger = internalLogger;
    }

    @Override
    @WorkerThread
    public boolean writeData(File file, byte[] data, boolean append) {
        if (append) {
            internalLogger.e(TAG, APPEND_MODE_NOT_SUPPORTED_MESSAGE);
            return false;
        }

        byte[] encryptedData = encryption.encrypt(data);

        if (data.length > 0 && encryptedData.length == 0) {
            internalLogger.e(TAG, BAD_ENCRYPTION_RESULT_MESSAGE);
            return false;
        }

        return delegate.writeData(file, encryptedData, append);
    }

    @Override
    @WorkerThread
    public byte[] readData(File file) {
        return encryption.decrypt(delegate.readData(file));
    }

    private static final String BAD_ENCRYPTION_RESULT_MESSAGE = "Encryption of non-empty data produced empty result, aborting write operation.";
    private static final String APPEND_MODE_NOT_SUPPORTED_MESSAGE = "Append mode is not supported, use EncryptedBatchFileReaderWriter instead.";
}
