package com.ft.sdk.garble.threadpool;

/**
 * BY huangDianHua
 * DATE:2019-11-29 18:57
 * Description:
 *
 * 负责 RUM Action View LongTask 等数据的事件消费
 *
 */
public class EventConsumerThreadPool extends BaseThreadPool {
    private final static int CORE_POOL_SIZE = 1;

    private static EventConsumerThreadPool threadPoolUtils;


    private EventConsumerThreadPool() {
        super(CORE_POOL_SIZE);
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
