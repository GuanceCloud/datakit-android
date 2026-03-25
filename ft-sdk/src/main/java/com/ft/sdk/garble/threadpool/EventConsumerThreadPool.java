package com.ft.sdk.garble.threadpool;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

/**
 * Responsible for event consumption of RUM Action View LongTask Resource Error data,
 * {@link com.ft.sdk.garble.bean.DataType#RUM_APP}
 * 
 * This class is used to consume the RUM Action View LongTask Resource Error data,
 * and the data is consumed by the thread pool.
 */
public class EventConsumerThreadPool extends BaseThreadPoolExecutor {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "EventConsumerThreadPool";
    private final static int CORE_POOL_SIZE = 1;

    private static EventConsumerThreadPool threadPoolUtils;
    private final ThreadLocal<Boolean> inExecutor = new ThreadLocal<>();

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

    @Override
    public void execute(Runnable task) {
        if (!poolRunning()) {
            reStartPool();
        }

        if (Boolean.TRUE.equals(inExecutor.get())) {
//            LogUtils.w(TAG, "inExecutor，" +
//                    "called from: " + LogUtils.getCallerMethodInfo(2));
            safeRun(task);
            return;
        }

        if (executor != null) {
            executor.execute(() -> {
                inExecutor.set(true);
                try {
                    safeRun(task);
                } finally {
                    inExecutor.remove();
                }
            });
        }
    }

    private void safeRun(Runnable task) {
        try {
            task.run();
        } catch (Throwable t) {
            handleError(t);
        }
    }

    private void handleError(Throwable t) {
        LogUtils.d(TAG, "EventConsumerThreadPool handleError:" + LogUtils.getStackTraceString(t));
    }

}
