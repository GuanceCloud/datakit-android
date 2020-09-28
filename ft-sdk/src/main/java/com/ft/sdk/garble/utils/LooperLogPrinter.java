package com.ft.sdk.garble.utils;

import android.util.Printer;

import com.ft.sdk.garble.interfaces.LooperLogPrinterListener;
import com.ft.sdk.garble.interfaces.UiPerfMonitorConfig;

/**
 * author: huangDianHua
 * time: 2020/9/28 10:00:40
 * description: UI 卡顿处理
 */
public class LooperLogPrinter implements Printer, UiPerfMonitorConfig {
    public static final String TAG = "LooperLogPrinter";
    private LooperLogPrinterListener mLooperLogPrinterListener = null;
    private long startTime = 0;

    public LooperLogPrinter(LooperLogPrinterListener listener){
        mLooperLogPrinterListener = listener;
    }
    @Override
    public void println(String message) {
        if(startTime <= 0){
            startTime = System.currentTimeMillis();
            mLooperLogPrinterListener.onStartLoop();
        }else {
            long time = System.currentTimeMillis() - startTime;
            execTime(message,time);
            startTime = 0;
        }
    }

    private void execTime(String message, long time) {
        int level = 0;
        if (time > TIME_WARNING_LEVEL_2){
            level = UI_PERF_LEVEL_2;
        }else if(time > TIME_WARNING_LEVEL_1){
            level = UI_PERF_LEVEL_1;
        }
        mLooperLogPrinterListener.onEndLoop(message,level);
    }
}
