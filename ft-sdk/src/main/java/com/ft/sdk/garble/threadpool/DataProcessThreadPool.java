package com.ft.sdk.garble.threadpool;

/**
 * Responsible for data entry into database, determines thread pool core size based on {@link #CPU_COUNT}. Scenarios: {@link com.ft.sdk.SyncTaskManager} synchronization,
 * and {@link com.ft.sdk.FTExceptionHandler} native crash file loading and writing
 *
 * @author Brandon
 */
public class DataProcessThreadPool extends BaseThreadPoolExecutor {
    /**
     * Current available CPU count
     */
    private final static int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    /**
     * Thread pool core size is the same as CPU core count
     */
    private final static int CORE_POOL_SIZE = CPU_COUNT;
    private static DataProcessThreadPool threadPoolUtils;


    private DataProcessThreadPool() {
        super(CORE_POOL_SIZE, "FTDataProcess", Thread.MIN_PRIORITY);
    }

    public static DataProcessThreadPool get() {
        synchronized (DataProcessThreadPool.class) {
            if (threadPoolUtils == null) {
                threadPoolUtils = new DataProcessThreadPool();
            }
            return threadPoolUtils;
        }
    }

}
