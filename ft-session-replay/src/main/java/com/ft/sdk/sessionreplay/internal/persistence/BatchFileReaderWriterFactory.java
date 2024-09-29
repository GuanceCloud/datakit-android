package com.ft.sdk.sessionreplay.internal.persistence;

import com.ft.sdk.sessionreplay.utils.InternalLogger;

public class BatchFileReaderWriterFactory {
    /**
     * Creates either plain [PlainBatchFileReaderWriter] or [PlainBatchFileReaderWriter] wrapped in
     * [EncryptedBatchReaderWriter] if encryption is provided.
     */
    public static BatchFileReaderWriter create(InternalLogger internalLogger, Encryption encryption) {
        PlainBatchFileReaderWriter readerWriter = new PlainBatchFileReaderWriter(internalLogger);
        if (encryption == null) {
            return readerWriter;
        } else {
            return new EncryptedBatchReaderWriter(encryption, readerWriter, internalLogger);
        }
    }

}