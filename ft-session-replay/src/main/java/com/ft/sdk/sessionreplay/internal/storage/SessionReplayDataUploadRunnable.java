package com.ft.sdk.sessionreplay.internal.storage;

import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.sessionreplay.SessionReplayUploader;
import com.ft.sdk.sessionreplay.internal.persistence.BatchData;
import com.ft.sdk.sessionreplay.internal.persistence.BatchId;
import com.ft.sdk.sessionreplay.internal.persistence.DataUploadConfiguration;
import com.ft.sdk.sessionreplay.internal.persistence.Storage;
import com.ft.sdk.sessionreplay.internal.utils.ExecutorUtils;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SessionReplayDataUploadRunnable implements Runnable {

    private static final String TAG = "SessionReplayDataUpload";

    private final String featureName;
    private final ScheduledThreadPoolExecutor threadPoolExecutor;
    private final Storage storage;
    private SessionReplayUploader dataUploader;
    private long currentDelayIntervalMs;
    private long minDelayMs;
    private long maxDelayMs;
    private int maxBatchesPerJob;
    private final SessionReplayContext sdkContext;
    private final InternalLogger internalLogger;

    public SessionReplayDataUploadRunnable(String featureName, ScheduledThreadPoolExecutor threadPoolExecutor,
                                           Storage storage,
                                           SessionReplayUploader uploader,
                                           DataUploadConfiguration dataUploadConfiguration,
                                           SessionReplayContext context,
                                           InternalLogger internalLogger) {
        this.featureName = featureName;
        this.threadPoolExecutor = threadPoolExecutor;
        this.storage = storage;
        this.currentDelayIntervalMs = dataUploadConfiguration.getDefaultDelayMs();
        this.dataUploader = uploader;
        this.minDelayMs = dataUploadConfiguration.getMinDelayMs();
        this.maxDelayMs = dataUploadConfiguration.getMaxDelayMs();
        this.maxBatchesPerJob = dataUploadConfiguration.getMaxBatchesPerUploadJob();
        this.sdkContext = context;
        this.internalLogger = internalLogger;
    }

    @Override
    public void run() {
        int batchConsumerAvailableAttempts = maxBatchesPerJob;
        int code = 0;

        do {
            batchConsumerAvailableAttempts--;
            //fixme
            code = handleNextBatch(sdkContext);

        } while (batchConsumerAvailableAttempts > 0 && code == 200);

        if (code > 0) {

            //todo 处理频次
        }

        handleNextUpload();
    }

    private int handleNextBatch(SessionReplayContext context) {
        int uploadStatus = 0;
        BatchData nextBatchData = storage.readNextBatch();
        if (nextBatchData != null) {
            uploadStatus = consumeBatch(
                    context,
                    nextBatchData.getId(),
                    nextBatchData.getData(),
                    nextBatchData.getMetadata()
            );
        }
        return uploadStatus;
    }

    private void handleNextUpload() {
        this.threadPoolExecutor.remove(this);
        ExecutorUtils.scheduleSafe(threadPoolExecutor,
                featureName + ":data upload", currentDelayIntervalMs,
                TimeUnit.MILLISECONDS,
                internalLogger,
                this);
    }

    private int consumeBatch(
            SessionReplayContext context,
            BatchId batchId,
            List<RawBatchEvent> batch,
            byte[] batchMeta
    ) {
        int status = 0;
        try {
            status = dataUploader.upload(context, batch, batchMeta);
        } catch (Exception e) {
            internalLogger.e(TAG, "", e);
        }
        if (status == HttpURLConnection.HTTP_OK || status >= HttpURLConnection.HTTP_BAD_REQUEST
                && status < HttpURLConnection.HTTP_INTERNAL_ERROR) {
            storage.confirmBatchRead(batchId,
                    status == HttpURLConnection.HTTP_OK ? new RemovalReason.Flushed()
                            : new RemovalReason.Invalid(), true);
        } else {
            storage.confirmBatchRead(batchId, new RemovalReason.Flushed(), false);
        }
        return status;
    }
}
