package com.ft.sdk.garble;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.garble.manager.FTActivityManager;

/**
 * create: by huangDianHua
 * time: 2020/6/17 17:50:45
 * description:处理当前应用退到后台10秒后重新进入
 */
class AppRestartCallback {
    private boolean appForeground = true;
    private long lastLeaveTime;

    public void onStart() {
        if (!appForeground) {//表示从后台重新进入
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastLeaveTime) / 1000 >= 10) {
                FTAutoTrack.startApp();
            }
        }
    }

    public void onStop() {
        appForeground = FTActivityManager.get().isAppForeground();
        lastLeaveTime = System.currentTimeMillis();
    }
}
