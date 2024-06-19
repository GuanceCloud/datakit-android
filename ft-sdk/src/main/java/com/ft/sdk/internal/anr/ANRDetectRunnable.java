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
 * ANR 错误监测，循环监测两个 Runner 执行相差时间是否超过 {@link #ANR_DETECT_DURATION_MS},超过则追加一条 ANR 错误信息
 */
public final class ANRDetectRunnable implements Runnable {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "ANRDetectRunnable";

    /**
     * 监测周期
     */
    public static final int ANR_DETECT_DURATION_MS = 5000;

    private final ExtraLogCatSetting extraLogCatSetting;

    /**
     * 主线程消息 Handler
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

                        //如果超时间没有调用则，添加一条 Error 数据
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
     * 关闭 Runner
     */
    public void shutdown() {
        isClose = true;
    }

    /**
     * 检查是否被调用的 Runner 对象
     */
    public static class CallbackRunnable implements Runnable {
        /**
         * 是否被调用
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
         * 重置
         */
        public void reset() {
            called = false;
        }


    }
}
