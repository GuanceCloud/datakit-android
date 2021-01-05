package com.ft.sdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTRUMConfig;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;


/**
 * create: by huangDianHua
 * time: 2020/6/17 17:50:45
 * description:处理当前应用退到后台10秒后重新进入
 */
class AppRestartCallback {
    private static final String TAG = "AppRestartCallback";
    public static final int MSG_CHECK_SLEEP_STATUS = 1;
    public static final int DELAY_MILLIS = 10000;//10 秒
    private boolean alreadySleep = true;
    private boolean mInited = false;//判断第一次第一个页是否创建
    private long startTime = 0;
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_CHECK_SLEEP_STATUS) {
                checkLongTimeSleep();
            }
        }
    };

    public void onStart() {
        if (alreadySleep) {//表示从后台重新进入
            if (mInited) {
                FTAutoTrack.startApp();
                startTime = Utils.getCurrentNanoTime();
            }
            FTMonitor.get().checkForReStart();
        }


    }

    public void onPreOnCreate() {
        if (!mInited) {
            FTAutoTrack.startApp();
            startTime = Utils.getCurrentNanoTime();
        }
    }

    public void onPostOnCreate() {
        if (FTRUMConfig.get().isRumEnable()) {
            if (!mInited) {
                long now = Utils.getCurrentNanoTime();
                FTActivityManager.get().setAppState(AppState.RUN);
                FTAutoTrack.putRUMLaunchPerformance(true, now - startTime);

            }
        }
    }

    public void onPostResume() {
        if (alreadySleep) {
            if (mInited) {
                if (FTRUMConfig.get().isRumEnable()) {
                    if (startTime > 0) {
                        long now = Utils.getCurrentNanoTime();
                        FTAutoTrack.putRUMLaunchPerformance(false, now - startTime);
                    }

                }
            }
            alreadySleep = false;
        }
        //避免重复计算页面启动的统计计算
        if (!mInited) {
            mInited = true;
        }
    }

    public void onStop() {
        boolean appForeground = FTActivityManager.get().isAppForeground();
        if (!appForeground) {
            handler.removeMessages(MSG_CHECK_SLEEP_STATUS);
            handler.sendEmptyMessageDelayed(MSG_CHECK_SLEEP_STATUS, DELAY_MILLIS);
        }
    }

    public void onPostDestroy() {
        boolean empty = FTActivityManager.get().getActiveCount() == 0;
        if (empty) {
            mInited = false;
            LogUtils.d(TAG, "Application all close");
        }
    }

    /**
     * 检测是否长时间休眠
     */
    private void checkLongTimeSleep() {
        boolean appForeground = FTActivityManager.get().isAppForeground();
        if (!appForeground) {
//            FTAutoTrack.sleepApp(DELAY_MILLIS);
            alreadySleep = true;
        }
    }

}
