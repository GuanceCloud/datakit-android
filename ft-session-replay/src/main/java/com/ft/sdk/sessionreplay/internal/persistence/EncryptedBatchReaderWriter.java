package com.ft.sdk.sessionreplay.internal.persistence;

import androidx.annotation.WorkerThread;

import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EncryptedBatchReaderWriter implements BatchFileReaderWriter {
    private static final String TAG = "EncryptedBatchReaderWri";
    private final Encryption encryption;
    private final BatchFileReaderWriter delegate;
    private final InternalLogger internalLogger;

    public EncryptedBatchReaderWriter(Encryption encryption, BatchFileReaderWriter delegate, InternalLogger internalLogger) {
        this.encryption = encryption;
        this.delegate = delegate;
        this.internalLogger = internalLogger;
    }

    @Override
    @WorkerThread
    public boolean writeData(File file, RawBatchEvent data, boolean append) {
        RawBatchEvent encryptedRawBatchEvent = new RawBatchEvent(
                encryption.encrypt(data.getData()),
                encryption.encrypt(data.getMetadata())
        );

        if (data.getData().length > 0 && encryptedRawBatchEvent.getData().length == 0) {
            internalLogger.e(TAG, BAD_ENCRYPTION_RESULT_MESSAGE);
            return false;
        }

        return delegate.writeData(file, encryptedRawBatchEvent, append);
    }

    @Override
    @WorkerThread
    public List<RawBatchEvent> readData(File file) {
        List<RawBatchEvent> decryptedEvents = new ArrayList<>();
        for (RawBatchEvent event : delegate.readData(file)) {
            decryptedEvents.add(new RawBatchEvent(
                    event.getData().length > 0 ? encryption.decrypt(event.getData()) : event.getData(),
                    event.getMetadata().length > 0 ? encryption.decrypt(event.getMetadata()) : event.getMetadata()
            ));
        }
        return decryptedEvents;
    }

    private static final String BAD_ENCRYPTION_RESULT_MESSAGE = "Encryption of non-empty data produced empty result, aborting write operation.";
}
