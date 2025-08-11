package com.ft.sdk;

import com.ft.sdk.garble.bean.BaseContentBean;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.db.FTDBCachePolicy;
import com.ft.sdk.garble.threadpool.LogConsumerThreadPool;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * author: huangDianHua
 * time: 2020/7/22 11:16:58
 * description: Local print log synchronization management class
 */
public class TrackLogManager {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "TrackLogManager";

    private static final int DEFAULT_BATCH_SIZE = 20;
    private static final long POLL_TIMEOUT_MS = 200L;

    private static volatile TrackLogManager instance;

    private final BlockingQueue<LogBean> logQueue;
    private final List<BaseContentBean> logBatchBuffer;
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private final ReentrantLock bufferLock = new ReentrantLock();

    private TrackLogManager() {
        this.logQueue = new LinkedBlockingQueue<>();
        this.logBatchBuffer = new ArrayList<>(DEFAULT_BATCH_SIZE * 2);
    }

    public static TrackLogManager get() {
        if (instance == null) {
            synchronized (TrackLogManager.class) {
                if (instance == null) {
                    instance = new TrackLogManager();
                }
            }
        }
        return instance;
    }

    /**
     * Record log
     *
     * @param logBean   log data
     * @param isSilence whether silent mode
     */
    public void trackLog(LogBean logBean, boolean isSilence) {
        if (!shouldProcessLog(logBean)) {
            return;
        }

        if (logQueue.size() >= FTDBCachePolicy.get().getLogLimitCount()) {
            switch (FTDBCachePolicy.get().getLogCacheDiscardStrategy()) {
                case DISCARD:
                    break;
                case DISCARD_OLDEST:
                    logQueue.poll();
                    logQueue.add(logBean);
                    break;
                default:
                    logQueue.add(logBean);
            }
        } else {
            logQueue.add(logBean);
        }

        triggerProcessing(isSilence);
    }

    /**
     * Shutdown manager, clear queue
     */
    public void shutdown() {
        logQueue.clear();
        bufferLock.lock();
        try {
            logBatchBuffer.clear();
        } finally {
            bufferLock.unlock();
        }
    }

    private boolean shouldProcessLog(LogBean logBean) {
        FTLoggerConfig config = FTLoggerConfigManager.get().getConfig();
        if (config == null) {
            LogUtils.w(TAG, "Logger config is null, discard log");
            return false;
        }

        if (!Utils.enableTraceSamplingRate(config.getSamplingRate())) {
            LogUtils.w(TAG, "Log discarded by sampling rate: " + logBean.getContent());
            return false;
        }

        if (config.isEnableLinkRumData()) {
            HashMap<String, Object> rumTags = FTRUMConfigManager.get().getRUMPublicDynamicTags();
            HashMap<String, Object> fields = new HashMap<>();
            FTRUMInnerManager.get().attachRUMRelative(rumTags, fields, false);
            logBean.appendTags(rumTags);
            logBean.appendFields(fields);
        }

        return true;
    }

    private void triggerProcessing(boolean isSilence) {
        if (isProcessing.compareAndSet(false, true)) {
            LogConsumerThreadPool.get().execute(new Runnable() {
                @Override
                public void run() {
                    processQueue(isSilence);
                }
            });
        }
    }

    private void processQueue(boolean isSilence) {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                LogBean logBean = logQueue.poll(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);

                if (logBean != null) {
                    addToBatchBuffer(logBean);
                }

                if (shouldFlushBatch(logBean)) {
                    flushBatch(isSilence);
                }

                if (isQueueEmptyAndNoMoreData(logBean)) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            LogUtils.e(TAG, "Log processing thread interrupted" + LogUtils.getStackTraceString(e));
        } catch (Exception e) {
            LogUtils.e(TAG, "Unexpected error processing logs:" + LogUtils.getStackTraceString(e));
        } finally {
            isProcessing.set(false);
            // If new logs arrive during processing, trigger processing again
            if (!logQueue.isEmpty()) {
                triggerProcessing(isSilence);
            }
        }
    }

    private void addToBatchBuffer(LogBean logBean) {
        bufferLock.lock();
        try {
            logBatchBuffer.add(logBean);
        } finally {
            bufferLock.unlock();
        }
    }

    private boolean shouldFlushBatch(LogBean currentLog) {
        bufferLock.lock();
        try {
            return logBatchBuffer.size() >= DEFAULT_BATCH_SIZE ||
                    (currentLog == null && !logBatchBuffer.isEmpty());
        } finally {
            bufferLock.unlock();
        }
    }

    private void flushBatch(boolean isSilence) {
        List<BaseContentBean> batchToSend;
        bufferLock.lock();
        try {
            batchToSend = new ArrayList<>(logBatchBuffer);
            logBatchBuffer.clear();
        } finally {
            bufferLock.unlock();
        }

        LogUtils.d(TAG, "Sending log batch, size: " + batchToSend.size());
        FTTrackInner.getInstance().batchLogBeanSync(batchToSend, isSilence);
    }

    private boolean isQueueEmptyAndNoMoreData(LogBean currentLog) {
        return currentLog == null && logQueue.isEmpty();
    }
}