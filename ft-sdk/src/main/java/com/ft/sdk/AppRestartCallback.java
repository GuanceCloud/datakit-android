package com.ft.sdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTRUMConfig;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.utils.Constants;
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
    private boolean mInited = false;
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
            }
            FTMonitor.get().checkForReStart();
        }


    }

    public void onPostOnCreate() {
        if (FTRUMConfig.get().isRumEnable()) {
            if (!mInited) {
                long startTime = Utils.querySharePreference(Constants.SHARE_PRE_START_TIME, Long.class, 0L);
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
                    long startTime = Utils.querySharePreference(Constants.SHARE_PRE_START_TIME, Long.class, 0L);

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
