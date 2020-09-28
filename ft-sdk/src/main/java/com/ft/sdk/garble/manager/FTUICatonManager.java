package com.ft.sdk.garble.manager;

import android.os.Looper;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.garble.interfaces.LooperLogPrinterListener;
import com.ft.sdk.garble.interfaces.UiPerfMonitorConfig;
import com.ft.sdk.garble.utils.LooperLogPrinter;

/**
 * author: huangDianHua
 * time: 2020/9/28 10:45:00
 * description: UI 卡顿管理类
 */
public class FTUICatonManager implements LooperLogPrinterListener, UiPerfMonitorConfig {
    public static final String TAG = "FTUICatonManager";
    private static FTUICatonManager mInstance;
    private LooperLogPrinter mLooperLogPrinter;
    private int monitorState = UI_PERF_MONITOR_STOP;

    private FTUICatonManager() {
        mLooperLogPrinter = new LooperLogPrinter(this);
    }

    public synchronized static FTUICatonManager getInstance() {
        if (mInstance == null) {
            mInstance = new FTUICatonManager();
        }
        return mInstance;
    }

    public void startMonitor() {
        Looper.getMainLooper().setMessageLogging(mLooperLogPrinter);
        monitorState = UI_PERF_MONITOR_START;
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
                FTAutoTrack.uiBlock();
                break;
            case UI_PERF_LEVEL_2://处理超过 1.5 秒的卡顿
                break;

        }
    }

    public static void release(){
        if(mInstance != null) {
            mInstance.stopMonitor();
            mInstance = null;
        }
    }
}
