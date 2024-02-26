package com.ft.sdk.garble.threadpool;

/**
 * 负责 ANR 监测使用线程池
 */
public class ANRDetectThreadPool extends BaseThreadPool {
    private final static int CORE_POOL_SIZE = 1;

    private static ANRDetectThreadPool threadPoolUtils;


    private ANRDetectThreadPool() {
        super(CORE_POOL_SIZE, "FTANRDetect");
    }


    public static ANRDetectThreadPool get() {
        synchronized (ANRDetectThreadPool.class) {
            if (threadPoolUtils == null) {
                threadPoolUtils = new ANRDetectThreadPool();
            }
            return threadPoolUtils;
        }
    }
}
