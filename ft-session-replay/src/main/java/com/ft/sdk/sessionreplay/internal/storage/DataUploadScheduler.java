package com.ft.sdk.sessionreplay.internal.storage;

import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.sessionreplay.SessionReplayUploader;
import com.ft.sdk.sessionreplay.SystemInfoProxy;
import com.ft.sdk.sessionreplay.internal.persistence.DataUploadConfiguration;
import com.ft.sdk.sessionreplay.internal.persistence.Storage;
import com.ft.sdk.sessionreplay.internal.utils.ExecutorUtils;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class DataUploadScheduler implements UploadScheduler {

    private final Runnable runnable;

    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final InternalLogger internalLogger;
    private final String feature;

    public DataUploadScheduler(String feature, InternalLogger internalLogger,
                               DataUploadConfiguration dataUploadConfiguration,
                               Storage storage,
                               SessionReplayUploader uploader, SessionReplayContext context, SystemInfoProxy systemInfoProxy) {
        this.internalLogger = internalLogger;
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);

        this.runnable = new SessionReplayDataUploadRunnable(feature, scheduledThreadPoolExecutor,
                storage, uploader, dataUploadConfiguration, context, internalLogger, systemInfoProxy);

        this.feature = feature;
    }

    public void startScheduling() {
        ExecutorUtils.executeSafe(scheduledThreadPoolExecutor, feature + ": data upload",
                internalLogger, this.runnable);

    }

    public void stopScheduling() {
        scheduledThreadPoolExecutor.remove(runnable);
    }
}