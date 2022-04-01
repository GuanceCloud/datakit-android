package com.ft.sdk;

/**
 * 启动计时
 */
class FTAppStartCounter {
    private long codeStartTime = 0;

    private FTAppStartCounter() {

    }

    private static class SingletonHolder {
        private static final FTAppStartCounter INSTANCE = new FTAppStartCounter();
    }

    public static FTAppStartCounter get() {
        return FTAppStartCounter.SingletonHolder.INSTANCE;
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
