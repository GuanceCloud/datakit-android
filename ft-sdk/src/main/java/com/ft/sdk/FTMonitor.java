package com.ft.sdk;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTMonitorConfigManager;
import com.ft.sdk.garble.service.MonitorService;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;


/**
 * create: by huangDianHua
 * time: 2020/4/17 10:16:20
 * description: 监控入口
 */
public class FTMonitor {
    public static final String TAG = "FTMonitor";
    //轮训周期，默认10秒
    private int period = 10;
    //监控类型
    private static FTMonitor instance;
    private FTRUMConfig config;
    private Intent intent;
    private boolean isError;
    private boolean isTrying;
    private static final int MSG_RETRY = 1;
    private static final int MAX_COUNT = 3;
    private int errorCount = 0;

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_RETRY) {
                start(config);
            }
        }
    };


    private FTMonitor() {
        intent = new Intent(FTApplication.getApplication(), MonitorService.class);
    }

    public static FTMonitor get() {
        if (instance == null) {
            instance = new FTMonitor();
        }
        return instance;
    }

    public FTMonitor setPeriod(int period) {
        this.period = period;
        return this;
    }

    public FTMonitor setMonitorType(int monitorType) {
        FTMonitorConfigManager.get().setMonitorType(monitorType);
        return this;
    }

//    public FTMonitor setUseGeoKey(boolean useGeoKey) {
//        FTMonitorConfig.get().setUseGeoKey(useGeoKey);
//        this.useGeoKey = useGeoKey;
//        return this;
//    }
//
//    public FTMonitor setGeoKey(String geoKey) {
//        FTMonitorConfig.get().setGeoKey(geoKey);
//        this.geoKey = geoKey;
//        return this;
//    }

    public void start(FTRUMConfig config) {
        this.config = config;
        boolean onlyMain = true;
        try {
            onlyMain = FTSdk.get().getBaseConfig().isOnlySupportMainProcess();
        } catch (Exception e) {
        }
        if (onlyMain && !Utils.isMainProcess()) {
            throw new InitSDKProcessException("当前 SDK 只能在主进程中运行，如果想要在非主进程中运行可以设置 FTSDKConfig.setOnlySupportMainProcess(false)");
        }
        FTMonitorConfigManager.get().initWithConfig(config);
        if (intent == null) {
            intent = new Intent(FTApplication.getApplication(), MonitorService.class);
        }
        intent.putExtra("command", MonitorService.START_CMD);
        intent.putExtra("period", period);
        try {
            FTApplication.getApplication().startService(intent);
            isTrying = false;
            isError = false;
        } catch (IllegalStateException e) {
            isTrying = true;
            isError = true;
            reStart();

        }
    }

    private void reStart() {
        handler.removeMessages(MSG_RETRY);
        if (errorCount < MAX_COUNT) {
            errorCount++;
            LogUtils.e(TAG, "MonitorService 启动失败，10 秒后重新尝试 ==> " + errorCount);
            handler.sendEmptyMessageDelayed(MSG_RETRY, 10000);

        } else {
            isTrying = false;
            LogUtils.e(TAG, "MonitorService 停止尝试");
        }
    }

    public void checkForReStart() {
        if (!isTrying && isError) {
            errorCount = 0;
            reStart();
        }
    }

    public void release() {
        if (intent != null) {
            intent.putExtra("command", MonitorService.STOP_CMD);
            FTApplication.getApplication().stopService(intent);
        }
        FTMonitorConfigManager.get().release();
        instance = null;
    }
}