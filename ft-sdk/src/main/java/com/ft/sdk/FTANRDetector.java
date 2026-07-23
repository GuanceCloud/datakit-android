package com.ft.sdk;

import android.os.Build;

import com.ft.sdk.garble.threadpool.ANRDetectThreadPool;
import com.ft.sdk.internal.anr.ANRDetectRunnable;
import com.ft.sdk.internal.anr.historical.HistoricalAnrCoordinator;


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
    private HistoricalAnrCoordinator historicalCoordinator;

    /**
     * Configuration initialization
     *
     * @param config RUM configuration
     */
    void init(FTRUMConfig config) {
        if (config.isEnableTrackAppANR()) {
            if (ANRStrategy.shouldUseHistoricalAnr(Build.VERSION.SDK_INT)) {
                if (historicalCoordinator == null) {
                    historicalCoordinator =
                            HistoricalAnrCoordinator.create(FTApplication.getApplication());
                    historicalCoordinator.consumePending();
                }
            } else if (runnable == null) {
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
        if (historicalCoordinator != null) {
            historicalCoordinator.shutdown();
            historicalCoordinator = null;
        }
    }
}
