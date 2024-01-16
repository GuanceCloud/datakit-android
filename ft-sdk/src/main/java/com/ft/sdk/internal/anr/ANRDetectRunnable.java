package com.ft.sdk.internal.anr;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.StringUtils;

/**
 * ANR 错误监测
 */
public final class ANRDetectRunnable implements Runnable {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "ANRDetectRunnable";

    public static final int ANR_DETECT_DURATION_MS = 5000;

    private final Handler handler = new Handler(Looper.getMainLooper());

    public ANRDetectRunnable() {
    }

    private final CallbackRunnable runnable = new CallbackRunnable();

    private boolean isClose = false;

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            if (isClose) break;
            try {
                synchronized (runnable) {
                    runnable.reset();
                    if (!handler.post(runnable)) {
                        return;
                    }
                    runnable.wait(ANR_DETECT_DURATION_MS);

                    if (!runnable.isCalled()) {
                        FTRUMGlobalManager.get().addError(
                                StringUtils.getStringFromStackTraceElement(handler.getLooper().getThread().getStackTrace()),
                                "android_anr", ErrorType.ANR_ERROR, AppState.RUN);
                        runnable.wait();
                    }
                }


            } catch (InterruptedException e) {
                LogUtils.e(TAG, "ANR Thread interrupt");
                break;
            }


        }
    }

    public void shutdown() {
        isClose = true;
    }

    public static class CallbackRunnable implements Runnable {
        public boolean isCalled() {
            return called;
        }

        private boolean called = false;

        @Override
        public void run() {
            called = true;
        }

        public void reset() {
            called = false;
        }


    }
}
