package com.ft.sdk.garble.manager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.ft.sdk.garble.utils.Utils;

public class FTMainLoopLogMonitor {

    private static final String TAG = "FTMainLoopLogMonitor";
    private long startUptimeNs = 0L;
    private String target = "";

//    @Override
//    public void println(String message) {
//        long now = System.nanoTime();
//        if (message.startsWith(PREFIX_START)) {
//            target = message.substring(PREFIX_START_LENGTH);
//            startUptimeNs = now;
//        } else if (message.startsWith(PREFIX_END)) {
//            long durationNs = now - startUptimeNs;
//            if (durationNs > 1000000000) {
//                LogUtils.d(TAG, target);
//
//            }
//        }
//    }

    private static class SingletonHolder {
        private static final FTMainLoopLogMonitor INSTANCE = new FTMainLoopLogMonitor();
    }

    public static FTMainLoopLogMonitor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private final HandlerThread mLogThread = new HandlerThread("UI BLock Log");
    private final Handler mIoHandler;
    private static final long TIME_BLOCK_NS = 1000000000L;//超过1秒显示卡顿
    private static LogCallBack mLogCallBack;
    private long lastTime;
    private long longTaskDuration;

    public interface LogCallBack {
        void getStackLog(String log, long duration);

    }

    public void setLogCallBack(LogCallBack callBack) {
        mLogCallBack = callBack;
    }

    public static void release() {
        getInstance().removeMonitor();
        mLogCallBack = null;
    }


    private FTMainLoopLogMonitor() {
        mLogThread.start();
        mIoHandler = new Handler(mLogThread.getLooper());
    }

    private static final Runnable mLogRunnable = () -> {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTrace = Looper.getMainLooper().getThread().getStackTrace();
        for (StackTraceElement s : stackTrace) {
            sb.append(s.toString()).append("\n");
        }

        if (mLogCallBack != null) {
            mLogCallBack.getStackLog(sb.toString(), getInstance().longTaskDuration);
        }


    };


    public void startMonitor() {
        if (lastTime > 0) {
            long duration = Utils.getCurrentNanoTime() - lastTime;
            if (duration > TIME_BLOCK_NS) {
                longTaskDuration = duration;
                mIoHandler.post(mLogRunnable);
            }
        }
        lastTime = Utils.getCurrentNanoTime();
    }

    public void stopMonitor() {
        lastTime = 0;
    }

    public void removeMonitor() {
        mIoHandler.removeCallbacks(mLogRunnable);
    }
}