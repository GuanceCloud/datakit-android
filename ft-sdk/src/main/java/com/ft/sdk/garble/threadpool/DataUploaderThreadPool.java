package com.ft.sdk.garble.threadpool;

/**
 * BY huangDianHua
 * DATE:2019-11-29 18:57
 * Description:
 */
public class DataUploaderThreadPool extends BaseThreadPool {
    private final static int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private final static int CORE_POOL_SIZE = CPU_COUNT;
    private static DataUploaderThreadPool threadPoolUtils;

    private DataUploaderThreadPool() {
        super(CORE_POOL_SIZE);
    }


    public static DataUploaderThreadPool get() {
        synchronized (DataUploaderThreadPool.class) {
            if (threadPoolUtils == null) {
                threadPoolUtils = new DataUploaderThreadPool();
            }
            return threadPoolUtils;
        }
    }

}
