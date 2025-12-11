package com.ft.sdk;

import android.os.Looper;
import android.util.Printer;

/**
 * Used to detect UI blocking
 *
 * @author Brandon
 */
public class FTUIBlockManager {
    /**
     * Show blocking when exceeding 1 second
     */
    public static final long DEFAULT_TIME_BLOCK_MS = 1000L;
    private static final long MINI_TIME_BLOCK_NS = 100000000L;
    private static final String PREFIX_METHOD_DISPATCH_START = ">>>>> Dispatching to ";
    private static final String PREFIX_METHOD_DISPATCH_END = "<<<<< Finished to ";

    private static class SingletonHolder {
        private static final FTUIBlockManager INSTANCE = new FTUIBlockManager();
    }

    private long blockDurationNS;


    public static FTUIBlockManager get() {
        return FTUIBlockManager.SingletonHolder.INSTANCE;
    }

    final Printer printer = new Printer() {
        @Override
        public void println(String x) {
            if (x == null) return;
            long now = System.nanoTime();
            if (x.startsWith(PREFIX_METHOD_DISPATCH_START)) {
                method = x.substring(PREFIX_METHOD_DISPATCH_START.length());
                startTime = now;
            } else if (x.startsWith(PREFIX_METHOD_DISPATCH_END)) {
                long duration = now - startTime;
                if (duration > blockDurationNS) {
                    FTRUMInnerManager.get().addLongTask(method, duration);
                }

            }

        }
    };

    private long startTime;
    private String method;

    /**
     * Start initialization
     *
     * @param config
     */
    public void start(FTRUMConfig config) {
        if (!config.isRumEnable()
                || !config.isEnableTrackAppUIBlock()) {
            return;
        }
        this.blockDurationNS = Math.max(config.getBlockDurationMS() * 1000000, MINI_TIME_BLOCK_NS);
        Looper.getMainLooper().setMessageLogging(printer);
    }

    /**
     * Release MessageLogging
     */
    public void release() {
        Looper.getMainLooper().setMessageLogging(null);
    }
}