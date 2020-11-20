package com.ft.sdk.garble.manager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class FTMainLoopLogMonitor {

    private static class SingletonHolder {
        private static final FTMainLoopLogMonitor INSTANCE = new FTMainLoopLogMonitor();
    }

    public static FTMainLoopLogMonitor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private final HandlerThread mLogThread = new HandlerThread("UI BLock Log");
    private final Handler mIoHandler;
    private static final long TIME_BLOCK = 1000L;//超过1秒显示卡顿
    private static LogCallBack mLogCallBack;

    public interface LogCallBack {
        void getStackLog(String log);

    }

    public void setLogCallBack(LogCallBack callBack) {
        mLogCallBack = callBack;
    }

    public static void release(){
        getInstance().removeMonitor();
        mLogCallBack =null;
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
            mLogCallBack.getStackLog(sb.toString());
        }


    };


    public void startMonitor() {
        mIoHandler.postDelayed(mLogRunnable, TIME_BLOCK);
    }

    public void removeMonitor() {
        mIoHandler.removeCallbacks(mLogRunnable);
    }
}