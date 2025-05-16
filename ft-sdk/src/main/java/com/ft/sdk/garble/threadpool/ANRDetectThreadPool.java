package com.ft.sdk.garble.threadpool;

/**
 * 负责 ANR 监测使用线程池，线程名称 FTANRDetect，core_size 为 1
 * 与 {@link com.ft.sdk.internal.anr.ANRDetectRunnable} 一起使用
 *
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
