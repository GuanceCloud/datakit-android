package com.ft.sdk;

import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.Map;
import java.util.Set;

/**
 * 启动计时
 */
class FTAppStartCounter {
    private static final String TAG = "FTAppStartCounter";
    private long codeStartTime = 0;
    private long codeStartTimeLine = 0;

    private FTAppStartCounter() {

    }

    private static class SingletonHolder {
        private static final FTAppStartCounter INSTANCE = new FTAppStartCounter();
    }

    public static FTAppStartCounter get() {
        return FTAppStartCounter.SingletonHolder.INSTANCE;
    }

    void markCodeStartTimeLine() {
        codeStartTimeLine = Utils.getCurrentNanoTime();
        LogUtils.d(TAG, "markCodeStartTimeLine");
    }

    long getMarkCodeTimeLine() {
        return codeStartTimeLine;
    }

    void resetCodeStartTimeline() {
        codeStartTimeLine = 0;
    }

    void codeStart(long codeStartTime) {
        this.codeStartTime = codeStartTime;
        LogUtils.d(TAG, "codeStart:" + codeStartTime);

    }

    void codeStartUpload() {
        FTAutoTrack.putRUMLaunchPerformance(true, codeStartTime);
        codeStartTime = 0;
    }


    void hotStart(long hotStartTime) {
        FTAutoTrack.putRUMLaunchPerformance(false, hotStartTime);
    }


    void checkToReUpload() {
        if (codeStartTime > 0) {
            codeStartUpload();
        }
    }

}
