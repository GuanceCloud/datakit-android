package com.ft.sdk.sessionreplay.internal.storage;

import static com.ft.sdk.sessionreplay.SessionReplayConstants.DECREASE_PERCENT;
import static com.ft.sdk.sessionreplay.SessionReplayConstants.INCREASE_PERCENT;

import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.SessionReplayConstants;
import com.ft.sdk.sessionreplay.SessionReplayUploader;
import com.ft.sdk.sessionreplay.SystemInfoProxy;
import com.ft.sdk.sessionreplay.internal.persistence.BatchData;
import com.ft.sdk.sessionreplay.internal.persistence.BatchId;
import com.ft.sdk.sessionreplay.internal.persistence.DataUploadConfiguration;
import com.ft.sdk.sessionreplay.internal.persistence.Storage;
import com.ft.sdk.sessionreplay.internal.utils.ExecutorUtils;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private final FeatureSdkCore sdkCore;
    private final File rootPath;

    public SessionReplayDataUploadRunnable(FeatureSdkCore sdkCore, String featureName, ScheduledThreadPoolExecutor threadPoolExecutor,
                                           Storage storage,
                                           SessionReplayUploader uploader,
                                           DataUploadConfiguration dataUploadConfiguration,
                                           SessionReplayContext context,
                                           InternalLogger internalLogger, SystemInfoProxy systemInfoProxy, File rootPath) {
        this.featureName = featureName;
        this.sdkCore = sdkCore;
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
        this.rootPath = rootPath;

    }

    @Override
    public void run() {
        UploadResult result = null;
        if (systemInfoProxy.isNetworkAvailable() && systemInfoProxy.isBatteryHealthToSync()) {
            int batchConsumerAvailableAttempts = maxBatchesPerJob;
            do {
                batchConsumerAvailableAttempts--;
                consumeErrorSampledData();
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


    private void consumeErrorSampledData() {
        long errorTimeLine = sdkCore.getErrorTimeLine() / 1000000;
        long timeNow = System.currentTimeMillis();
        if (errorTimeLine > 0) {
            if (rootPath.exists()) {
                File sampledOnErrorPath = new File(rootPath,
                        SessionReplayConstants.PATH_SESSION_REPLAY_ERROR_SAMPLED);
                File[] files = sampledOnErrorPath.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            long fileTime = file.lastModified(); // The creation time is not available, use the modification time instead
                            if (fileTime < errorTimeLine) {
                                File moveTargetPath = new File(rootPath,
                                        SessionReplayConstants.PATH_SESSION_REPLAY
                                                + "/" + file.getName());
                                //Move to normal upload queue
                                if (moveFile(file, moveTargetPath)) {
                                    internalLogger.d(TAG, "SR consumeErrorSampledData:" + file.getName());
                                }
                            }
                            deleteExpired(file, timeNow);
                        }
                    }
                }

            }
        }
    }

    private final static int ONE_MINUTES_IN_SECOND = 60000;


    private boolean moveFile(File sourceFile, File destFile) {
        // Ensure the parent directory of the target path exists
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }

        // Execute move: first copy the file, then delete the source file
        boolean success = copyFile(sourceFile, destFile);

        // If the copy is successful, delete the source file
        if (success) {
            success = sourceFile.delete();
        }

        return success;
    }

    private boolean copyFile(File sourceFile, File destFile) {
        try (InputStream inputStream = new FileInputStream(sourceFile);
             OutputStream outputStream = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }
    }


    /**
     * Delete expired data that exceeds one minute
     *
     * @param file
     * @param timeNow
     */
    private void deleteExpired(File file, long timeNow) {
        long fileLastModified = file.lastModified();
        if (timeNow - fileLastModified > ONE_MINUTES_IN_SECOND) {
            if (file.delete()) {
                internalLogger.w(TAG, "SR delete expire file:" + file.getName());
            }
        }
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
            //internalLogger.d(TAG,"batchId:"+batchId.getId());
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
