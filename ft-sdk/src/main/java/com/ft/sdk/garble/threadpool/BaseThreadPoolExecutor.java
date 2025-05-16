package com.ft.sdk.garble.threadpool;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class BaseThreadPoolExecutor extends BaseThreadPool {
    public BaseThreadPoolExecutor(int corePoolSize, String threadName) {
        super(corePoolSize, threadName);
    }

    public BaseThreadPoolExecutor(int corePoolSize, String threadName, int priority) {
        super(corePoolSize, threadName, priority);
    }

    @Override
    public ThreadPoolExecutor createNew() {
        return new ThreadPoolExecutor(corePoolSize, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
                TimeUnit.SECONDS, workQueue, threadFactory);
    }
}
