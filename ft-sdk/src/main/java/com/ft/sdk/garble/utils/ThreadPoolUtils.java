package com.ft.sdk.garble.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BY huangDianHua
 * DATE:2019-11-29 18:57
 * Description:
 */
public class ThreadPoolUtils {
    public static final String TAG = "ThreadPoolUtils";
    private final static int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private final static int CORE_POOL_SIZE = CPU_COUNT;
    private final static int MAXIMUM_POOL_SIZE = 128;
    private final static int KEEP_ALIVE = 5;
    private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
    private ThreadPoolExecutor executor;
    private static ThreadPoolUtils threadPoolUtils;

    private ThreadPoolUtils() {
        executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, workQueue, threadFactory);
        executor.setRejectedExecutionHandler(sRunOnSerialPolicy);
    }

    private final RejectedExecutionHandler sRunOnSerialPolicy =
            new RejectedExecutionHandler() {
                public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                    LogUtils.w(TAG,"Exceeded ThreadPoolExecutor pool size");
                    // As a last ditch fallback, run it on an executor with an unbounded queue.
                    // Create this executor lazily, hopefully almost never.
                    synchronized (this) {
                        if (executor == null) {
                            workQueue = new LinkedBlockingQueue<Runnable>();
                            executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, workQueue, threadFactory);
                            executor.allowCoreThreadTimeOut(true);
                        }
                    }
                    executor.execute(r);
                }
            };

    public static ThreadPoolUtils get() {
        synchronized (ThreadPoolUtils.class) {
            if (threadPoolUtils == null) {
                threadPoolUtils = new ThreadPoolUtils();
            }
            return threadPoolUtils;
        }
    }

    private static ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicInteger integer = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "ThreadPoolUtils:" + integer.getAndIncrement());
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

    public void execute(FutureTask futureTask) {
        if (!poolRunning()) {
            reStartPool();
        }
        if (executor != null) {
            executor.execute(futureTask);
        }
    }

    public void cancel(FutureTask futureTask) {
        futureTask.cancel(true);
    }

    public boolean poolRunning() {
        return executor != null && !executor.isShutdown();
    }

    public void shutDown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public void reStartPool() {
        executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, workQueue, threadFactory);
    }
}
