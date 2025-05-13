package com.ft.sdk.garble.threadpool;

/**
 * 负责数据入数据库，根据 {@link #CPU_COUNT} 来确定线程池 core size。场景：{@link com.ft.sdk.SyncTaskManager} 同步，
 * 和{@link com.ft.sdk.FTExceptionHandler} native crash 文件加载写入
 *
 * @author Brandon
 */
public class DataProcessThreadPool extends BaseThreadPoolExecutor {
    /**
     * 当前可用 CPU 数
     */
    private final static int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    /**
     * Thread pool core size 与 CPU 核心数相同
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
