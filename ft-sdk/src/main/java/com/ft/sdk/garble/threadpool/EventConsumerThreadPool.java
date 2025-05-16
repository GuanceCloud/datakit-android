package com.ft.sdk.garble.threadpool;

/**
 * 负责 RUM Action View LongTask Resource Error 数据的事件消费,{@link com.ft.sdk.garble.bean.DataType#RUM_APP}
 */
public class EventConsumerThreadPool extends BaseThreadPoolExecutor {
    private final static int CORE_POOL_SIZE = 1;

    private static EventConsumerThreadPool threadPoolUtils;


    private EventConsumerThreadPool() {
        super(CORE_POOL_SIZE, "FTEventCsr", 8);
    }


    public static EventConsumerThreadPool get() {
        synchronized (EventConsumerThreadPool.class) {
            if (threadPoolUtils == null) {
                threadPoolUtils = new EventConsumerThreadPool();
            }
            return threadPoolUtils;
        }
    }

}
