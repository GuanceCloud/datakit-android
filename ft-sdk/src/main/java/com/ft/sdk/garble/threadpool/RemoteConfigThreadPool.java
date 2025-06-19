package com.ft.sdk.garble.threadpool;

public class RemoteConfigThreadPool extends BaseThreadPoolExecutor {
    private final static int CORE_POOL_SIZE = 1;

    private static RemoteConfigThreadPool threadPoolUtils;


    private RemoteConfigThreadPool() {
        super(CORE_POOL_SIZE, "FTRemoteConfig");
    }


    public static RemoteConfigThreadPool get() {
        synchronized (RemoteConfigThreadPool.class) {
            if (threadPoolUtils == null) {
                threadPoolUtils = new RemoteConfigThreadPool();
            }
            return threadPoolUtils;
        }
    }
}
