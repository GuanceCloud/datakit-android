package com.ft.sdk;

import android.os.Looper;
import android.util.Printer;

import com.ft.sdk.garble.utils.Utils;

/**
 * 用于检测界面卡顿
 * @author Brandon
 */
public class FTUIBlockManager {
    private static final long TIME_BLOCK_NS = 1000000000L;//超过1秒显示卡顿
    private static final String PREFIX_METHOD_DISPATCH_START = ">>>>> Dispatching to ";
    private static final String PREFIX_METHOD_DISPATCH_END = "<<<<< Finished to ";

    private static class SingletonHolder {
        private static final FTUIBlockManager INSTANCE = new FTUIBlockManager();
    }

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
                if (duration > TIME_BLOCK_NS) {
                    FTRUMGlobalManager.get().addLongTask(method, duration);
                }

            }

        }
    };

    private long startTime;
    private String method;

    /**
     * 启动初始化
     * @param config
     */
    public void start(FTRUMConfig config) {
        if (!config.isRumEnable()
                && !config.isEnableTrackAppUIBlock()) {
            return;
        }

        Looper.getMainLooper().setMessageLogging(printer);
    }

    public void release() {
        Looper.getMainLooper().setMessageLogging(null);
    }
}