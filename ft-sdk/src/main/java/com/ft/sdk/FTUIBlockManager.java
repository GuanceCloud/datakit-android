package com.ft.sdk;

import android.os.Looper;
import android.util.Printer;

import com.ft.sdk.garble.utils.Utils;

/**
 * 用于检测界面卡顿
 *
 * @author Brandon
 */
public class FTUIBlockManager {
    /**
     * 超过1秒显示卡顿
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

            if (x.startsWith(PREFIX_METHOD_DISPATCH_START)) {
                method = x.substring(PREFIX_METHOD_DISPATCH_START.length());
                startTime = Utils.getCurrentNanoTime();
            } else if (x.startsWith(PREFIX_METHOD_DISPATCH_END)) {
                long duration = Utils.getCurrentNanoTime() - startTime;
                if (duration > blockDurationNS) {
                    FTRUMInnerManager.get().addLongTask(method, duration);
                }

            }

        }
    };

    private long startTime;
    private String method;

    /**
     * 启动初始化
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
     * 释放 MessageLogging
     */
    public void release() {
        Looper.getMainLooper().setMessageLogging(null);
    }
}