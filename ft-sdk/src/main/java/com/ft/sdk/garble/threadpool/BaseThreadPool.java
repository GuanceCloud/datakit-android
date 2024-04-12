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
import java.util.concurrent.TimeUnit;

/**
 * BY huangDianHua
 * DATE:2019-11-29 18:57
 * Description:线程池基类，用于构建阻塞线程队列来消费数据
 */
public abstract class BaseThreadPool {
    public static final String TAG = Constants.LOG_TAG_PREFIX + "BaseThreadPool";
    /**
     * 最大容量
     */
    private final static int MAXIMUM_POOL_SIZE = 128;
    private final static int KEEP_ALIVE = 5;
    /**
     * 阻塞线程队列
     */
    private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    private ThreadPoolExecutor executor;
    private final int corePoolSize;

    private final ThreadFactory threadFactory;

    public BaseThreadPool(int corePoolSize, String threadName) {
        this(corePoolSize, threadName, Thread.NORM_PRIORITY);

    }

    /**
     * @param corePoolSize
     * @param threadName   线程名
     * @param priority     权重
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
     * 用于判断线程池是否在运行
     * @return
     */
    public boolean poolRunning() {
        return executor != null && !executor.isShutdown();
    }

    /**
     * 释放队列资源，在 {@link FTSdk#shutDown()} 时，会被回收
     */
    public void shutDown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
            executor = null;
        }
    }

    /**
     * 重置，重新创建队列
     */
    public void reStartPool() {
        executor = createNew();
    }

    /**
     * 新建线程池
     *
     * @return
     */
    private ThreadPoolExecutor createNew() {
        return new ThreadPoolExecutor(corePoolSize, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
                TimeUnit.SECONDS, workQueue, threadFactory);

    }
}
