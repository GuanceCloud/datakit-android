package com.ft.sdk.garble.manager;

import android.os.Looper;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.garble.FTTrackInner;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.interfaces.LooperLogPrinterListener;
import com.ft.sdk.garble.interfaces.UiPerfMonitorConfig;
import com.ft.sdk.garble.utils.LooperLogPrinter;

/**
 * author: huangDianHua
 * time: 2020/9/28 10:45:00
 * description: UI 卡顿管理类
 */
public class FTUIBlockManager implements LooperLogPrinterListener, UiPerfMonitorConfig {
    public static final String TAG = "FTUIBlockManager";
    private static FTUIBlockManager mInstance;
    private LooperLogPrinter mLooperLogPrinter;
    private int monitorState = UI_PERF_MONITOR_STOP;
    private FTSDKConfig mConfig;

    private FTUIBlockManager() {
        mLooperLogPrinter = new LooperLogPrinter(this);
    }

    public synchronized static FTUIBlockManager getInstance() {
        if (mInstance == null) {
            mInstance = new FTUIBlockManager();
        }
        return mInstance;
    }

    public void startMonitor(FTSDKConfig config) {
        if (!config.isEnableTrackAppUIBlock()) {
            return;
        }
        Looper.getMainLooper().setMessageLogging(mLooperLogPrinter);
        monitorState = UI_PERF_MONITOR_START;
        mConfig = config;
    }

    public void stopMonitor() {
        Looper.getMainLooper().setMessageLogging(null);
        monitorState = UI_PERF_MONITOR_STOP;
    }

    public boolean isMonitoring() {
        return monitorState == UI_PERF_MONITOR_START;
    }

    @Override
    public void onStartLoop() {

    }

    @Override
    public void onEndLoop(String logInfo, int level) {
        //卡顿等级可以自己定义
        switch (level) {
            case UI_PERF_LEVEL_1://处理超过 1 秒的卡顿
            case UI_PERF_LEVEL_2://处理超过 1.5 秒的卡顿
                FTAutoTrack.uiBlock();

                LogBean logBean = new LogBean("------ UIBlock  ------, " + logInfo, System.currentTimeMillis());
                logBean.setStatus(Status.CRITICAL);
                logBean.setEnv(mConfig.getEnv());
                logBean.setServiceName(mConfig.getTraceServiceName());
                FTTrackInner.getInstance().logBackground(logBean);
                break;

        }
    }

    public static void release() {
        if (mInstance != null) {
            mInstance.stopMonitor();
            mInstance = null;
        }
    }
}
