package com.ft.sdk.sessionreplay.internal.persistence;

import androidx.annotation.WorkerThread;

import com.ft.sdk.sessionreplay.internal.storage.EventType;
import com.ft.sdk.sessionreplay.internal.storage.FilePersistenceConfig;
import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.storage.EventBatchWriter;

import java.io.File;
import java.util.Locale;

public class FileEventBatchWriter implements EventBatchWriter {
    private static final String TAG = "FileEventBatchWriter";
    private final File batchFile;
    private final File metadataFile;
    private final FileWriter<RawBatchEvent> eventsWriter;
    private final FileReaderWriter metadataReaderWriter;
    private final FilePersistenceConfig filePersistenceConfig;
    private final InternalLogger internalLogger;

    public FileEventBatchWriter(
            File batchFile,
            File metadataFile,
            FileWriter<RawBatchEvent> eventsWriter,
            FileReaderWriter metadataReaderWriter,
            FilePersistenceConfig filePersistenceConfig,
            InternalLogger internalLogger) {
        this.batchFile = batchFile;
        this.metadataFile = metadataFile;
        this.eventsWriter = eventsWriter;
        this.metadataReaderWriter = metadataReaderWriter;
        this.filePersistenceConfig = filePersistenceConfig;
        this.internalLogger = internalLogger;
    }

    @WorkerThread
    @Override
    public byte[] currentMetadata() {
        if (metadataFile == null || !metadataFile.exists()) return null;

        return metadataReaderWriter.readData(metadataFile);
    }


    @WorkerThread
    @Override
    public boolean write(RawBatchEvent event, byte[] batchMetadata, EventType eventType) {
        // prevent useless operation for empty event
        if (event.getData().length == 0) {
            return true;
        } else if (!checkEventSize(event.getData().length)) {
            return false;
        } else if (eventsWriter.writeData(batchFile, event, true)) {
            if (batchMetadata != null && batchMetadata.length > 0 && metadataFile != null) {
                writeBatchMetadata(metadataFile, batchMetadata);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean checkEventSize(int eventSize) {
        if (eventSize > filePersistenceConfig.getMaxItemSize()) {
            internalLogger.e(TAG, String.format(Locale.US, ERROR_LARGE_DATA, eventSize, filePersistenceConfig.getMaxItemSize()));
            return false;
        }
        return true;
    }

    @WorkerThread
    private void writeBatchMetadata(File metadataFile, byte[] metadata) {
        boolean result = metadataReaderWriter.writeData(metadataFile, metadata, false);
        if (!result) {
            internalLogger.e(TAG, String.format(Locale.US, WARNING_METADATA_WRITE_FAILED, metadataFile.getPath()));
        }
    }

    private static final String WARNING_METADATA_WRITE_FAILED = "Unable to write metadata file: %s";
    private static final String ERROR_LARGE_DATA = "Can't write data with size %d (max item size is %d)";
}
