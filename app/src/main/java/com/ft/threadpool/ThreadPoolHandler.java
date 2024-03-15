package com.ft.threadpool;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@link com.ft.HighLoadActivity} 线程池管理
 */
public class ThreadPoolHandler {


    private static class SingletonHolder {
        private static final ThreadPoolHandler INSTANCE = new ThreadPoolHandler();
    }

    public static ThreadPoolHandler get() {
        return ThreadPoolHandler.SingletonHolder.INSTANCE;
    }

    /**
     * 4 core size 同时并发 log 2，http 2
     */
    ExecutorService executor = Executors.newFixedThreadPool(4);

    public ThreadPoolHandler() {

    }

    /**
     * 重置
     */
    public void reset() {
        executor = Executors.newFixedThreadPool(4);
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * 关闭线程池
     */
    public void shutDownAll() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
