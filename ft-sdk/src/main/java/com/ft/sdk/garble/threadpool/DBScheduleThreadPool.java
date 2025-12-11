package com.ft.sdk.garble.threadpool;

import com.ft.sdk.garble.utils.LogUtils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DBScheduleThreadPool extends BaseThreadPool {


    private static DBScheduleThreadPool threadPoolUtils;

    public static DBScheduleThreadPool get() {
        synchronized (DBScheduleThreadPool.class) {
            if (threadPoolUtils == null) {
                threadPoolUtils = new DBScheduleThreadPool();
            }
            return threadPoolUtils;
        }
    }

    public DBScheduleThreadPool() {
        super(1, "FTDBScheduleLock");
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long delayMS) {
        if (!poolRunning()) {
            reStartPool();
        }
        return ((ScheduledThreadPoolExecutor) executor).schedule(runnable, delayMS, TimeUnit.MILLISECONDS);
    }

    @Override
    public ThreadPoolExecutor createNew() {
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
    }
}
