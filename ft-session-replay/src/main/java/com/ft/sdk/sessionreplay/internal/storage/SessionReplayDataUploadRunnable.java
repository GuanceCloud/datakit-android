package com.ft.sdk.sessionreplay.internal.storage;

import static com.ft.sdk.sessionreplay.SessionReplayConstants.DECREASE_PERCENT;
import static com.ft.sdk.sessionreplay.SessionReplayConstants.INCREASE_PERCENT;

import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.sessionreplay.SessionReplayUploader;
import com.ft.sdk.sessionreplay.SystemInfoProxy;
import com.ft.sdk.sessionreplay.internal.persistence.BatchData;
import com.ft.sdk.sessionreplay.internal.persistence.BatchId;
import com.ft.sdk.sessionreplay.internal.persistence.DataUploadConfiguration;
import com.ft.sdk.sessionreplay.internal.persistence.Storage;
import com.ft.sdk.sessionreplay.internal.utils.ExecutorUtils;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SessionReplayDataUploadRunnable implements Runnable {

    private static final String TAG = "SessionReplayDataUpload";

    private final String featureName;
    private final ScheduledThreadPoolExecutor threadPoolExecutor;
    private final Storage storage;
    private final SessionReplayUploader dataUploader;
    private long currentDelayIntervalMs;
    private final long minDelayMs;
    private final long maxDelayMs;
    private final int maxBatchesPerJob;
    private final SessionReplayContext sdkContext;
    private final InternalLogger internalLogger;
    private final SystemInfoProxy systemInfoProxy;

    public SessionReplayDataUploadRunnable(String featureName, ScheduledThreadPoolExecutor threadPoolExecutor,
                                           Storage storage,
                                           SessionReplayUploader uploader,
                                           DataUploadConfiguration dataUploadConfiguration,
                                           SessionReplayContext context,
                                           InternalLogger internalLogger, SystemInfoProxy systemInfoProxy) {
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
        this.systemInfoProxy = systemInfoProxy;
    }

    @Override
    public void run() {
        UploadResult result = null;
        if (systemInfoProxy.isNetworkAvailable() && systemInfoProxy.isBatteryHealthToSync()) {
            int batchConsumerAvailableAttempts = maxBatchesPerJob;
            do {
                batchConsumerAvailableAttempts--;
                result = handleNextBatch(sdkContext);

            } while (batchConsumerAvailableAttempts > 0 && result != null && result.isSuccess());
        }
        if (result != null && result.isSuccess()) {
            currentDelayIntervalMs = Math.max((long) (currentDelayIntervalMs * DECREASE_PERCENT), minDelayMs);
        } else {
            currentDelayIntervalMs = Math.min((long) (currentDelayIntervalMs * INCREASE_PERCENT), maxDelayMs);
        }

        handleNextUpload(currentDelayIntervalMs);
    }

    private UploadResult handleNextBatch(SessionReplayContext context) {
        UploadResult result = null;
        BatchData nextBatchData = storage.readNextBatch();
        if (nextBatchData != null) {
            result = consumeBatch(
                    context,
                    nextBatchData.getId(),
                    nextBatchData.getData(),
                    nextBatchData.getMetadata()
            );
        }
        return result;
    }

    private void handleNextUpload(long currentDelayIntervalMs) {
        this.threadPoolExecutor.remove(this);
        ExecutorUtils.scheduleSafe(threadPoolExecutor,
                featureName + ":data upload", currentDelayIntervalMs,
                TimeUnit.MILLISECONDS,
                internalLogger,
                this);
    }

    private UploadResult consumeBatch(
            SessionReplayContext context,
            BatchId batchId,
            List<RawBatchEvent> batch,
            byte[] batchMeta
    ) {
        UploadResult result = null;
        try {
            result = dataUploader.upload(context, batch, batchMeta);
            if (result != null) {
                storage.confirmBatchRead(batchId,
                        !result.isNeedReTry() ? new RemovalReason.Flushed()
                                : new RemovalReason.Invalid(), true);
            } else {
                storage.confirmBatchRead(batchId, new RemovalReason.Flushed(), false);
            }
        } catch (Exception e) {
            internalLogger.e(TAG, e.getMessage(), e);
        }

        return result;
    }
}
