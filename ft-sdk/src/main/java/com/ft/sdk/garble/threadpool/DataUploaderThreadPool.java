package com.ft.sdk.garble.threadpool;

/**
 * 负责数据上行传输，与数据入数据库，根据 {@link #CPU_COUNT} 来确定线程池 core size，
 *
 * @author Brandon
 */
public class DataUploaderThreadPool extends BaseThreadPool {
    /**
     * 当前可用 CPU 数
     */
    private final static int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    /**
     * Thread pool core size 与 CPU 核心数相同
     */
    private final static int CORE_POOL_SIZE = CPU_COUNT;
    private static DataUploaderThreadPool threadPoolUtils;


    private DataUploaderThreadPool() {
        super(CORE_POOL_SIZE, "FTDataUp", Thread.MIN_PRIORITY);
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
