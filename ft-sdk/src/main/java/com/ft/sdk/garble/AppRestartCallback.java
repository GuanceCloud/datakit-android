package com.ft.sdk.garble;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.FTMonitor;
import com.ft.sdk.garble.manager.FTActivityManager;


/**
 * create: by huangDianHua
 * time: 2020/6/17 17:50:45
 * description:处理当前应用退到后台10秒后重新进入
 */
class AppRestartCallback {
    public static final int MSG_CHECK_SLEEP_STATUS = 1;
    public static final int DELAY_MILLIS = 10000;//10 秒
    private boolean appForeground = true;
    private long lastLeaveTime;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_CHECK_SLEEP_STATUS) {
                checkRealSleep();
            }
        }
    };

    public void onStart() {
        if (!appForeground) {//表示从后台重新进入
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastLeaveTime) / 1000 >= 10) {
                FTAutoTrack.startApp();
                FTMonitor.get().checkForReStart();
            }
        }
    }

    public void onStop() {
        appForeground = FTActivityManager.get().isAppForeground();
        lastLeaveTime = System.currentTimeMillis();

        if (!appForeground) {
            handler.removeMessages(MSG_CHECK_SLEEP_STATUS);
            handler.sendEmptyMessageDelayed(MSG_CHECK_SLEEP_STATUS, DELAY_MILLIS);
        }
    }

    private void checkRealSleep() {
        boolean appForeground = FTActivityManager.get().isAppForeground();
        if (!appForeground) {
            FTAutoTrack.sleepApp();
        }
    }

}
