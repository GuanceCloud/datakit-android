package com.ft.sdk.garble.threadpool;

/**
 * Responsible for ANR monitoring using thread pool, thread name FTANRDetect, core_size is 1
 * Used with {@link com.ft.sdk.internal.anr.ANRDetectRunnable}
 */
public class ANRDetectThreadPool extends BaseThreadPoolExecutor {
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
