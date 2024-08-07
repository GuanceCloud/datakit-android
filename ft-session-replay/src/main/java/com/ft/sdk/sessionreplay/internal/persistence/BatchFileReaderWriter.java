package com.ft.sdk.sessionreplay.internal.persistence;

import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

public interface BatchFileReaderWriter extends FileWriter<RawBatchEvent>, BatchFileReader {

    /**
     * Creates either plain [PlainBatchFileReaderWriter] or [PlainBatchFileReaderWriter] wrapped in
     * [EncryptedBatchReaderWriter] if encryption is provided.
     */
    static BatchFileReaderWriter create(InternalLogger internalLogger, Encryption encryption) {
        PlainBatchFileReaderWriter readerWriter = new PlainBatchFileReaderWriter(internalLogger);
        if (encryption == null) {
            return readerWriter;
        } else {
            return new EncryptedBatchReaderWriter(encryption, readerWriter, internalLogger);
        }
    }
}