package com.ft.sdk;

import com.ft.sdk.garble.threadpool.ANRDetectThreadPool;
import com.ft.sdk.internal.anr.ANRDetectRunnable;


/**
 * ANR event monitoring
 */
public class FTANRDetector {


    private static class SingletonHolder {
        private static final FTANRDetector INSTANCE = new FTANRDetector();
    }

    public static FTANRDetector get() {
        return FTANRDetector.SingletonHolder.INSTANCE;
    }

    private ANRDetectRunnable runnable;

    /**
     * Configuration initialization
     *
     * @param config RUM configuration
     */
    void init(FTRUMConfig config) {
        if (config.isEnableTrackAppANR()) {
            if (runnable == null) {
                runnable = new ANRDetectRunnable(config.getExtraLogCatWithANR());
                ANRDetectThreadPool.get().execute(runnable);
            }
        }
    }

    /**
     * Release ANR corresponding resources
     */
    void release() {
        ANRDetectThreadPool.get().shutDown();
        if (runnable != null) {
            runnable.shutdown();
            runnable = null;
        }
    }
}
