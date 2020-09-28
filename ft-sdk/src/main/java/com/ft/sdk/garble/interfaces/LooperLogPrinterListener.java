package com.ft.sdk.garble.interfaces;

/**
 * author: huangDianHua
 * time: 2020/9/28 10:33:36
 * description: UI Looper 数据打印回调
 */
public interface LooperLogPrinterListener {
    void onStartLoop();

    void onEndLoop(String logInfo, int level);
}
