package com.ft.sdk.garble.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
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
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2;
    private static final int KEEP_ALIVE = 5;
    private static BlockingQueue workQueue = new ArrayBlockingQueue(10);
    private static ThreadPoolExecutor executor;

    private ThreadPoolUtils() {
    }

    private static ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicInteger integer = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "ThreadPoolUtils:" + integer.getAndIncrement());
        }
    };

    static {
        executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, workQueue, threadFactory);
    }

    public static void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    public static void execute(FutureTask futureTask) {
        executor.execute(futureTask);
    }

    public static void cancel(FutureTask futureTask) {
        futureTask.cancel(true);
    }

}
