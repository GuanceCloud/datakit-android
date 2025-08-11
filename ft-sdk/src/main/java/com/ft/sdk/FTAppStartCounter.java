package com.ft.sdk;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

/**
 * Startup timing
 *
 * <a href="https://docs.guance.com/real-user-monitoring/explorer/">Explorer</a>
 * <p>
 * {@link Constants#MEASUREMENT} is {@link Constants#FT_MEASUREMENT_RUM_ACTION}
 * {@link Constants#KEY_RUM_ACTION_TYPE} is {@link  Constants#ACTION_NAME_LAUNCH_COLD,
 * Constants#ACTION_TYPE_LAUNCH_HOT}
 * <p>
 * For the concept of lifecycle, refer to the official documentation
 * <a href="https://developer.android.com/topic/performance/vitals/launch-time?hl=zh-cn">Launch Time</a>
 *
 * <p>
 * Cold start, corresponds to {@link Constants#ACTION_NAME_LAUNCH_COLD}
 * Warm start, hot start, corresponds to {@link Constants#ACTION_TYPE_LAUNCH_HOT}
 *
 * @author Brandon
 */
class FTAppStartCounter {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "AppStartCounter";
    /**
     * Startup duration, unit: nanoseconds
     */
    private long coldStartDuration = 0;

    /**
     * Startup start timestamp, unit: nanoseconds
     */
    private long coldStartTimeLine = 0;


    private FTAppStartCounter() {
    }

    private static class SingletonHolder {
        private static final FTAppStartCounter INSTANCE = new FTAppStartCounter();
    }

    public static FTAppStartCounter get() {
        return FTAppStartCounter.SingletonHolder.INSTANCE;
    }


    /**
     * Record cold start duration
     * * {@link Constants#KEY_RUM_ACTION_TYPE} = {@link  Constants#ACTION_TYPE_LAUNCH_COLD}
     *
     * @param coldStartEndTimeLine
     */
    void coldStart(long coldStartEndTimeLine) {
        this.coldStartTimeLine = Utils.getAppStartTimeNs();
        this.coldStartDuration = coldStartEndTimeLine - coldStartTimeLine;
        LogUtils.d(TAG, "coldStart:" + coldStartDuration);

    }

    /**
     * Upload cold start time
     */
    void coldStartUpload() {
        if (coldStartDuration <= 0 || coldStartTimeLine <= 0) return;
        FTAutoTrack.putRUMLaunchPerformance(true, coldStartDuration, coldStartTimeLine);
        coldStartDuration = 0;
    }

    /**
     * Upload hot start time
     * <p>
     * {@link Constants#KEY_RUM_ACTION_TYPE} = {@link  Constants#ACTION_TYPE_LAUNCH_HOT}
     *
     * @param hotStartDuration Hot start duration, unit: nanoseconds
     */
    void hotStart(long hotStartDuration, long startTime) {
        FTAutoTrack.putRUMLaunchPerformance(false, hotStartDuration, startTime);
        LogUtils.d(TAG, "hotStart:" + hotStartDuration);
    }


    /**
     * Check for retransmission, applied to lifecycle, in cases earlier than SDK {@link FTSdk#install(FTSDKConfig)}, generally used for third-party frameworks,
     * such as flutter SDK, ReactNative SDK
     */
    void checkToReUpload() {
        if (coldStartDuration > 0) {
            coldStartUpload();
        }
    }

}
