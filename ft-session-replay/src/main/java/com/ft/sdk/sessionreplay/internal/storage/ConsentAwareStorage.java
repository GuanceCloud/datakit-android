package com.ft.sdk.sessionreplay.internal.storage;

import static com.ft.sdk.sessionreplay.internal.storage.FileMover.existsSafe;

import com.ft.sdk.api.ConsentProvider;
import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.sessionreplay.internal.persistence.BatchData;
import com.ft.sdk.sessionreplay.internal.persistence.BatchFileReaderWriter;
import com.ft.sdk.sessionreplay.internal.persistence.BatchId;
import com.ft.sdk.sessionreplay.internal.persistence.EventBatchWriterCallback;
import com.ft.sdk.sessionreplay.internal.persistence.FileEventBatchWriter;
import com.ft.sdk.sessionreplay.internal.persistence.FileOrchestrator;
import com.ft.sdk.sessionreplay.internal.persistence.FileReaderWriter;
import com.ft.sdk.sessionreplay.internal.persistence.NoOpEventBatchWriter;
import com.ft.sdk.sessionreplay.internal.persistence.Storage;
import com.ft.sdk.sessionreplay.internal.persistence.TrackingConsent;
import com.ft.sdk.sessionreplay.internal.utils.ExecutorUtils;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.storage.EventBatchWriter;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class ConsentAwareStorage implements Storage {

    private static final String TAG = "ConsentAwareStorage";
    private final ExecutorService executorService;
    final FileOrchestrator grantedOrchestrator;
    final FileOrchestrator errorSessionOrchestrator;
    final FileOrchestrator pendingOrchestrator;
    private final BatchFileReaderWriter batchEventsReaderWriter;
    private final FileReaderWriter batchMetadataReaderWriter;
    private final FileMover fileMover;
    private final InternalLogger internalLogger;
    final FilePersistenceConfig filePersistenceConfig;
    private final MetricsDispatcher metricsDispatcher;
    private final ConsentProvider consentProvider;
    private final Set<Batch> lockedBatches = new HashSet<>();
    private final Object writeLock = new Object();

    public ConsentAwareStorage(
            ExecutorService executorService,
            FileOrchestrator grantedOrchestrator,
            FileOrchestrator errorSessionOrchestrator,
            FileOrchestrator pendingOrchestrator,
            BatchFileReaderWriter batchEventsReaderWriter,
            FileReaderWriter batchMetadataReaderWriter,
            FileMover fileMover,
            InternalLogger internalLogger,
            FilePersistenceConfig filePersistenceConfig,
            MetricsDispatcher metricsDispatcher,
            ConsentProvider contentProvider) {
        this.executorService = executorService;
        this.grantedOrchestrator = grantedOrchestrator;
        this.errorSessionOrchestrator = errorSessionOrchestrator;
        this.pendingOrchestrator = pendingOrchestrator;
        this.batchEventsReaderWriter = batchEventsReaderWriter;
        this.batchMetadataReaderWriter = batchMetadataReaderWriter;
        this.fileMover = fileMover;
        this.internalLogger = internalLogger;
        this.filePersistenceConfig = filePersistenceConfig;
        this.metricsDispatcher = metricsDispatcher;
        this.consentProvider = contentProvider;
    }

    @Override
    public void writeCurrentBatch(
            SessionReplayContext sdkContext,
            boolean forceNewBatch,
            EventBatchWriterCallback callback) {
        FileOrchestrator orchestrator;
        TrackingConsent provider = consentProvider.getConsent();
        switch (provider) {
            case GRANTED:
                orchestrator = grantedOrchestrator;
                break;
            case SAMPLED_ON_ERROR_SESSION:
                orchestrator = errorSessionOrchestrator;
                break;
            case PENDING:
                orchestrator = pendingOrchestrator;
                break;
            case NOT_GRANTED:
            default:
                orchestrator = null;
        }

        ExecutorUtils.submitSafe(executorService, "Data write", internalLogger, new Runnable() {
            @Override
            public void run() {
                synchronized (writeLock) {
                    File batchFile = (orchestrator != null) ? orchestrator.getWritableFile(forceNewBatch) : null;
                    File metadataFile = (batchFile != null) ? orchestrator.getMetadataFile(batchFile) : null;
                    EventBatchWriter writer = (orchestrator == null || batchFile == null)
                            ? new NoOpEventBatchWriter()
                            : new FileEventBatchWriter(
                            batchFile,
                            metadataFile,
                            batchEventsReaderWriter,
                            batchMetadataReaderWriter,
                            filePersistenceConfig,
                            internalLogger);
                    callback.callBack(writer);
                }
            }
        });

    }

    @Override
    public BatchData readNextBatch() {
        File batchFile;
        File metaFile;
        synchronized (lockedBatches) {
            batchFile = grantedOrchestrator.getReadableFile(getLockedBatchFiles());
            if (batchFile == null) return null;
            metaFile = grantedOrchestrator.getMetadataFile(batchFile);
            lockedBatches.add(new Batch(batchFile, metaFile));
        }

        BatchId batchId = BatchId.fromFile(batchFile);
        byte[] batchMetadata = (metaFile != null && metaFile.exists())
                ? batchMetadataReaderWriter.readData(metaFile)
                : null;
        List<RawBatchEvent> batchData = batchEventsReaderWriter.readData(batchFile);

        return new BatchData(batchId, batchData, batchMetadata);
    }

    @Override
    public void confirmBatchRead(BatchId batchId, RemovalReason removalReason, boolean deleteBatch) {
        Batch batch = null;
        synchronized (lockedBatches) {
            for (Batch b : lockedBatches) {
                if (batchId.matchesFile(b.file)) {
                    batch = b;
                    break;
                }
            }
        }

        if (batch == null) return;

        if (deleteBatch) {
            deleteBatch(batch, removalReason);
        }
        synchronized (lockedBatches) {
            lockedBatches.remove(batch);
        }
    }

    @Override
    public void dropAll() {
        ExecutorUtils.submitSafe(executorService, "ConsentAwareStorage.dropAll", internalLogger, new Runnable() {
            @Override
            public void run() {
                synchronized (lockedBatches) {

                    for (Batch batch : lockedBatches) {
                        deleteBatch(batch, new RemovalReason.Flushed());
                    }
                    lockedBatches.clear();
                }
                for (FileOrchestrator orchestrator : new FileOrchestrator[]{pendingOrchestrator, grantedOrchestrator}) {
                    for (File file : orchestrator.getAllFiles()) {
                        File metaFile = orchestrator.getMetadataFile(file);
                        deleteBatch(file, metaFile, new RemovalReason.Flushed());
                    }
                }
            }
        });
    }

    private void deleteBatch(Batch batch, RemovalReason reason) {
        deleteBatch(batch.getFile(), batch.getMetaFile(), reason);
    }

    private void deleteBatch(File batchFile, File metaFile, RemovalReason reason) {
        deleteBatchFile(batchFile, reason);
        if (metaFile != null && existsSafe(metaFile, internalLogger)) {
            deleteBatchMetadataFile(metaFile);
        }
    }

    private void deleteBatchFile(File batchFile, RemovalReason reason) {
        boolean result = fileMover.delete(batchFile);
        if (result) {
            metricsDispatcher.sendBatchDeletedMetric(batchFile, reason);
        } else {
            internalLogger.w(TAG, String.format(Locale.US, WARNING_DELETE_FAILED, batchFile.getPath()));
        }
    }

    private void deleteBatchMetadataFile(File metadataFile) {
        boolean result = fileMover.delete(metadataFile);
        if (!result) {
            internalLogger.w(TAG, String.format(Locale.US, WARNING_DELETE_FAILED, metadataFile.getPath()));
        }
    }

    private Set<File> getLockedBatchFiles() {
        Set<File> files = new HashSet<>();
        for (Batch batch : lockedBatches) {
            files.add(batch.getFile());
        }
        return files;
    }

    private static class Batch {
        private final File file;
        private final File metaFile;

        Batch(File file, File metaFile) {
            this.file = file;
            this.metaFile = metaFile;
        }

        public File getFile() {
            return file;
        }

        public File getMetaFile() {
            return metaFile;
        }
    }

    private static final String WARNING_DELETE_FAILED = "Unable to delete file: %s";
}
