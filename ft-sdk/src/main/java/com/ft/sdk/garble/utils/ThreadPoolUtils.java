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
    private final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private final int CORE_POOL_SIZE = CPU_COUNT;
    private final int MAXIMUM_POOL_SIZE = 128;
    private final int KEEP_ALIVE = 5;
    private BlockingQueue workQueue = new ArrayBlockingQueue(10);
    private ThreadPoolExecutor executor;
    private static ThreadPoolUtils threadPoolUtils;

    private ThreadPoolUtils() {
        executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, workQueue, threadFactory);
    }

    public static ThreadPoolUtils get(){
        synchronized (ThreadPoolUtils.class){
            if(threadPoolUtils == null){
                threadPoolUtils = new ThreadPoolUtils();
            }
            return threadPoolUtils;
        }
    }

    private ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicInteger integer = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "ThreadPoolUtils:" + integer.getAndIncrement());
        }
    };



    public void execute(Runnable runnable) {
        if(!poolRunning()){
            reStartPool();
        }
        if(executor != null) {
            executor.execute(runnable);
        }
    }

    public void execute(FutureTask futureTask) {
        if(executor != null) {
            executor.execute(futureTask);
        }
    }

    public void cancel(FutureTask futureTask) {
        futureTask.cancel(true);
    }

    public boolean poolRunning(){
        return executor != null && !executor.isShutdown();
    }

    public void shutDown(){
        if(executor!=null && !executor.isShutdown()) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public void reStartPool(){
        executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, workQueue, threadFactory);
    }
}
