package com.ft.sdk.sessionreplay.internal.persistence;


import com.ft.sdk.sessionreplay.utils.InternalLogger;


/**
 * Interface for file reading and writing operations with byte array data.
 */
public interface FileReaderWriter extends FileWriter<byte[]>, FileReader<byte[]> {

    /**
     * Creates either a plain {@link PlainFileReaderWriter} or a {@link PlainFileReaderWriter}
     * wrapped in an {@link EncryptedFileReaderWriter} if encryption is provided.
     * 
     * @param internalLogger The logger to use for internal logging.
     * @param encryption Optional encryption instance to wrap the reader writer.
     * @return An instance of {@link FileReaderWriter}.
     */
    static FileReaderWriter create(InternalLogger internalLogger, Encryption encryption) {
        PlainFileReaderWriter readerWriter = new PlainFileReaderWriter(internalLogger);
        if (encryption == null) {
            return readerWriter;
        } else {
            return new EncryptedFileReaderWriter(encryption, readerWriter, internalLogger);
        }
    }
}
