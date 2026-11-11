package com.ft.sdk;

import android.os.Build;

import androidx.annotation.RestrictTo;

import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.ErrorType;
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
                runnable = new ANRDetectRunnable(config.getExtraLogCatWithANR(),
                        new ANRDetectRunnable.IssueReporter() {
                            @Override
                            public void report(String stack, long occurredAtNanoseconds, String threadName) {
                                report(stack, occurredAtNanoseconds, threadName, FTActivityManager.get().getAppState());
                            }

                            @Override
                            public void report(String stack, long occurredAtNanoseconds, String threadName, AppState state) {
                                reportAnr(stack, occurredAtNanoseconds, threadName, state);
                            }
                        });
                ANRDetectThreadPool.get().execute(runnable);
            }
        }
    }

    /** Shared reporting path for configured and legacy watchdog detectors. */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void reportAnr(String stack, long occurredAtNanoseconds, String threadName, AppState state) {
        if (!FTRUMConfigManager.get().isRumEnable()) {
            return;
        }
        FTRUMInnerManager.get().addAutomaticIssue(stack, "android_anr", occurredAtNanoseconds,
                FTIssueCategory.ANR, ErrorType.ANR_ERROR.toString(), state, false, threadName, null, null);
    }

    /**
     * Release ANR corresponding resources
     */
    void release() {
        if (runnable != null) {
            runnable.shutdown();
            runnable = null;
        }
        ANRDetectThreadPool.get().shutDown();
        if (historicalCoordinator != null) {
            historicalCoordinator.shutdown();
            historicalCoordinator = null;
        }
    }
}
