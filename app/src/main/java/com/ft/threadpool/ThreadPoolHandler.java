package com.ft.threadpool;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@link com.ft.HighLoadActivity} thread pool management
 */
public class ThreadPoolHandler {


    private static class SingletonHolder {
        private static final ThreadPoolHandler INSTANCE = new ThreadPoolHandler();
    }

    public static ThreadPoolHandler get() {
        return ThreadPoolHandler.SingletonHolder.INSTANCE;
    }

    /**
     * 4 core size, concurrent log 2, http 2
     */
    ExecutorService executor = Executors.newFixedThreadPool(4);

    public ThreadPoolHandler() {

    }

    /**
     * Reset
     */
    public void reset() {
        executor = Executors.newFixedThreadPool(4);
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Shutdown thread pool
     */
    public void shutDownAll() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
