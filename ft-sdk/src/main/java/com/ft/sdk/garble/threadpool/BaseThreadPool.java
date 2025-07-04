package com.ft.sdk.garble.threadpool;

import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * BY huangDianHua
 * DATE:2019-11-29 18:57
 * Description: Base class for thread pool, used to build blocking thread queues to consume data
 */
public abstract class BaseThreadPool {
    public static final String TAG = Constants.LOG_TAG_PREFIX + "BaseThreadPool";
    /**
     * Maximum capacity
     */
    protected final static int MAXIMUM_POOL_SIZE = 128;
    protected final static int KEEP_ALIVE = 5;
    /**
     * Blocking thread queue
     */
    protected BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    protected ThreadPoolExecutor executor;
    protected final int corePoolSize;

    protected final ThreadFactory threadFactory;

    public BaseThreadPool(int corePoolSize, String threadName) {
        this(corePoolSize, threadName, Thread.NORM_PRIORITY);

    }

    /**
     * @param corePoolSize
     * @param threadName   Thread name
     * @param priority     Priority
     */
    public BaseThreadPool(int corePoolSize, String threadName, int priority) {
        this.corePoolSize = corePoolSize;
        this.threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, threadName);
                thread.setPriority(priority);
                return thread;
            }
        };
        executor = createNew();
        executor.setRejectedExecutionHandler(sRunOnSerialPolicy);
    }

    private final RejectedExecutionHandler sRunOnSerialPolicy =
            new RejectedExecutionHandler() {
                public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                    LogUtils.w(TAG, "Exceeded ThreadPoolExecutor pool size");
                    synchronized (this) {
                        if (executor == null) {
                            workQueue = new LinkedBlockingQueue<>();
                            executor = createNew();
                            executor.allowCoreThreadTimeOut(true);
                        }
                    }
                    executor.execute(r);
                }
            };


    public void execute(Runnable runnable) {
        if (!poolRunning()) {
            reStartPool();
        }
        if (executor != null) {
            executor.execute(runnable);
        }
    }

    public void cancel(FutureTask futureTask) {
        futureTask.cancel(true);
    }

    /**
     * Used to determine whether the thread pool is running
     *
     * @return
     */
    public boolean poolRunning() {
        return executor != null && !executor.isShutdown();
    }

    /**
     * Release queue resources, will be recycled when {@link FTSdk#shutDown()} is called
     */
    public void shutDown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
            executor = null;
        }
    }

    /**
     * Reset, recreate the queue
     */
    public void reStartPool() {
        executor = createNew();
    }

    /**
     * Create a new thread pool
     *
     * @return
     */
    public abstract ThreadPoolExecutor createNew();
}
