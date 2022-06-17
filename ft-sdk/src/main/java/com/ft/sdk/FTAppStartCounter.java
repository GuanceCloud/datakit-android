package com.ft.sdk;

import com.ft.sdk.garble.utils.Utils;

/**
 * 启动计时
 */
class FTAppStartCounter {
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
    }

    long getMarkCodeTimeLine() {
        return codeStartTimeLine;
    }

    void resetCodeStartTimeline() {
        codeStartTimeLine = 0;
    }

    void codeStart(long codeStartTime) {
        this.codeStartTime = codeStartTime;

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
