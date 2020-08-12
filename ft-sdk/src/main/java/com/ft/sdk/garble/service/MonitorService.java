package com.ft.sdk.garble.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ft.sdk.garble.utils.LogUtils;

/**
 * create: by huangDianHua
 * time: 2020/4/28 14:31:34
 * description:监控服务
 */
public class MonitorService extends Service {
    public final static String TAG = MonitorService.class.getSimpleName();
    public static final int START_CMD = 1;
    public static final int STOP_CMD = 2;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.w(TAG,"MonitorService 启动");
        if (intent == null) {
            return START_STICKY;
        }
        Bundle bundle = intent.getExtras();
        int command = bundle != null ? bundle.getInt("command") : 0;
        switch (command) {
            case START_CMD:
                int period = bundle.getInt("period");
                FTMonitorManager.install(period);
                break;
            case STOP_CMD:
                FTMonitorManager ftMonitorManager = FTMonitorManager.get();
                if (ftMonitorManager != null) {
                    ftMonitorManager.release();
                }
                break;
            default:
        }
        return START_STICKY;
    }
}
