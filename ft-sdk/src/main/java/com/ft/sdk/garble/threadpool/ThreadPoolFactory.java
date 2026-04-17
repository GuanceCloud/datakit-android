package com.ft.sdk.garble.threadpool;

public class ThreadPoolFactory extends BaseThreadPoolExecutor {
    private final static int CORE_POOL_SIZE = 1;

    public ThreadPoolFactory(int corePoolSize, String threadName) {
        super(corePoolSize, threadName);
    }

    public ThreadPoolFactory(int corePoolSize, String threadName, int priority) {
        super(corePoolSize, threadName, priority);
    }

    public ThreadPoolFactory(String threadName) {
        super(CORE_POOL_SIZE, "FT-" + threadName, 4);
    }
}
