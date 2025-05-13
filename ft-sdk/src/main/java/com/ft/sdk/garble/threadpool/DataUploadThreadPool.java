package com.ft.sdk.garble.threadpool;

import com.ft.sdk.SyncTaskManager;
import com.ft.sdk.garble.utils.LogUtils;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataUploadThreadPool extends BaseThreadPool {

    private static final String TAG = SyncTaskManager.TAG;

    private final AtomicBoolean isScheduled = new AtomicBoolean(false);
    private final AtomicBoolean needReschedule = new AtomicBoolean(false);

    private static DataUploadThreadPool threadPoolUtils;


    public static DataUploadThreadPool get() {
        synchronized (DataUploadThreadPool.class) {
            if (threadPoolUtils == null) {
                threadPoolUtils = new DataUploadThreadPool();
            }
            return threadPoolUtils;
        }
    }

    public DataUploadThreadPool() {
        super(2, "FTDataUpload", Thread.MIN_PRIORITY);
    }


    private Runnable runnable;

    public void initRunnable(Runnable runnable) {
        LogUtils.d(TAG, "******************* Sync Poll Ready *******************>>>");
        isScheduled.set(false);
        needReschedule.set(false);
        this.runnable = runnable;
    }

    /**
     * for testing
     *
     * @param isScheduled
     */
    public void setRunning(boolean isScheduled) {
        this.isScheduled.set(isScheduled);
    }

    public void schedule(long delay) {
        if (runnable == null) return;
        if (!isScheduled.compareAndSet(false, true)) {
            // 已有任务正在等待或执行，跳过调度
            needReschedule.set(true);
            return;
        }
        scheduleTask(delay);
    }

    private void scheduleTask(long delay) {
        if (delay > 0) {
            LogUtils.d(TAG, "******************* Sync Poll Waiting *******************>>>");
        }
        ((ScheduledThreadPoolExecutor) this.executor).schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    if (delay > 0) {
                        LogUtils.d(TAG, "******************* Sync Poll Running *******************>>>");
                    }
                    runnable.run();
                } finally {
                    if (needReschedule.compareAndSet(true, false)) {
                        scheduleTask(0);
                    } else {
                        isScheduled.set(false);
                        LogUtils.d(TAG, "******************* Sync Poll Finish *******************>>>");
                    }
                }

            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public ThreadPoolExecutor createNew() {
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
    }
}
