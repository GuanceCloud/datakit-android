package com.ft.sdk.internal.anr;


import android.os.Handler;
import android.os.Looper;

import com.ft.sdk.ExtraLogCatSetting;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.StringUtils;
import com.ft.sdk.garble.utils.Utils;

/**
 * ANR error monitoring, continuously monitors whether the execution time difference between two Runners exceeds 
 * {@link #ANR_DETECT_DURATION_MS}, if exceeded, adds an ANR error message
 */
public final class ANRDetectRunnable implements Runnable {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "ANRDetectRunnable";

    /**
     * Monitoring cycle
     */
    public static final int ANR_DETECT_DURATION_MS = 5000;

    private final ExtraLogCatSetting extraLogCatSetting;

    /**
     * Main thread message Handler
     */
    private final Handler handler = new Handler(Looper.getMainLooper());

    public ANRDetectRunnable(ExtraLogCatSetting extraLogCatSetting) {
        this.extraLogCatSetting = extraLogCatSetting;
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
                        String stackTrace =
                                StringUtils.getStringFromStackTraceElement(handler.getLooper().getThread().getStackTrace())
                                        + "\n" + Utils.getAllThreadStack();
                        if (extraLogCatSetting != null) {
                            stackTrace += Utils.getLogcat(extraLogCatSetting.getLogcatMainLines(),
                                    extraLogCatSetting.getLogcatSystemLines(),
                                    extraLogCatSetting.getLogcatEventsLines());
                        }

                        //If not called within timeout, add an Error data
                        FTRUMGlobalManager.get().addError(stackTrace, "android_anr", ErrorType.ANR_ERROR, AppState.RUN);
                        runnable.wait();
                    }
                }
            } catch (InterruptedException e) {
                LogUtils.e(TAG, "ANR Thread interrupt");
                break;
            }


        }
    }

    /**
     * Shutdown Runner
     */
    public void shutdown() {
        isClose = true;
    }

    /**
     * Check if the Runner object is called
     */
    public static class CallbackRunnable implements Runnable {
        /**
         * Whether it is called
         *
         * @return
         */
        public boolean isCalled() {
            return called;
        }

        private boolean called = false;

        @Override
        public void run() {
            called = true;
        }

        /**
         * Reset
         */
        public void reset() {
            called = false;
        }


    }
}
