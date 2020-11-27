package com.ft.sdk.garble.manager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.FTMonitor;


/**
 * create: by huangDianHua
 * time: 2020/6/17 17:50:45
 * description:处理当前应用退到后台10秒后重新进入
 */
class AppRestartCallback {
    public static final int MSG_CHECK_SLEEP_STATUS = 1;
    public static final int DELAY_MILLIS = 10000;//10 秒
    private boolean alreadySleep = false;
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
            FTAutoTrack.startApp();
            FTMonitor.get().checkForReStart();
        }
    }

    public void onPostResume() {
        if (alreadySleep) {
            FTAutoTrack.putLaunchPerformance(false);
            alreadySleep = false;
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
