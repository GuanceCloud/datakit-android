package com.ft.sdk;

import android.app.Activity;
import android.view.View;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.FirstDrawDoneListener;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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
     * Flag indicating whether the first frame has been drawn
     */
    private final AtomicBoolean firstDrawDone = new AtomicBoolean(false);

    /**
     * Startup start timestamp for duration, unit: nanoseconds
     */
    private long coldStartTimeLineForNanoDuration = 0;

    /**
     * Cold start timeline timestamp used for reporting, unit: nanoseconds
     */
    private long coldStartTimeLine;

    /**
     * Application.onCreate() timestamp, unit: nanoseconds
     */
    private long applicationOnCreateTimeLine = 0;

    /**
     * Duration from Application.onCreate() to first Activity preOnCreate(), unit: nanoseconds
     */
    private long applicationOnCreateDuration = 0;

    /**
     * First Activity preOnCreate() timestamp, unit: nanoseconds
     */
    private long firstActivityPreOnActivityTimeline = 0;

    /**
     * Duration from app start to Application.onCreate(), unit: nanoseconds
     */
    private long applicationPreOnCreateDuration = 0;

    /**
     * Duration from first Activity preOnCreate() to first frame drawn, unit: nanoseconds
     */
    private long firsDrawnDuration = 0;


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
        this.coldStartTimeLineForNanoDuration = Utils.getAppStartTimeNs();
        this.coldStartDuration = coldStartEndTimeLine - coldStartTimeLineForNanoDuration;
        this.coldStartTimeLine = Utils.getCurrentNanoTime() - coldStartDuration;

        this.applicationPreOnCreateDuration = applicationOnCreateTimeLine - coldStartTimeLineForNanoDuration;
        this.applicationOnCreateDuration = firstActivityPreOnActivityTimeline - applicationOnCreateTimeLine;
        this.firsDrawnDuration = coldStartEndTimeLine - firstActivityPreOnActivityTimeline;

        LogUtils.d(TAG, "coldStart:" + coldStartDuration
                + ",coldTimeLine:" + coldStartTimeLineForNanoDuration);
    }

    void checkFirstActivityPreCreate(long nanoTimeLine) {
        if (!firstDrawDone.get()) {
            LogUtils.d(TAG, "checkFirstActivityPreCreate:" + nanoTimeLine);
            firstActivityPreOnActivityTimeline = nanoTimeLine;
        }

    }

    void checkFirstFrameStart(Activity activity, Runnable drawDoneCallback) {
        if (!firstDrawDone.getAndSet(true)) {
            View rootView = activity.findViewById(android.R.id.content);
            if (rootView != null) {
                FirstDrawDoneListener.registerForNextDraw(rootView, drawDoneCallback);
            }
        }
    }

    /**
     * Upload cold start time
     */
    void coldStartUpload() {
        if (coldStartDuration <= 0 || coldStartTimeLineForNanoDuration <= 0) return;

        HashMap<String, Long> preApplicationInit = null;
        HashMap<String, Long> applicationInit = null;
        HashMap<String, Long> firstFrameInit = null;

        // Build preApplicationInit map: {"start": 0, "duration": applicationPreOnCreateDuration}
        if (coldStartTimeLineForNanoDuration > 0 && applicationPreOnCreateDuration > 0) {
            preApplicationInit = new HashMap<>();
            preApplicationInit.put("start", 0L);
            preApplicationInit.put("duration", applicationPreOnCreateDuration);
        }

        // Build applicationInit map: {"start": relativeStart, "duration": applicationOnCreateDuration}
        if (applicationOnCreateTimeLine > 0 && applicationOnCreateDuration > 0) {
            long relativeStart = applicationOnCreateTimeLine - coldStartTimeLineForNanoDuration;
            applicationInit = new HashMap<>();
            applicationInit.put("start", relativeStart);
            applicationInit.put("duration", applicationOnCreateDuration);
        }

        // Build firstFrameInit map: {"start": relativeStart, "duration": firsDrawnDuration}
        if (firstActivityPreOnActivityTimeline > 0 && firsDrawnDuration > 0) {
            long relativeStart = firstActivityPreOnActivityTimeline - coldStartTimeLineForNanoDuration;
            firstFrameInit = new HashMap<>();
            firstFrameInit.put("start", relativeStart);
            firstFrameInit.put("duration", firsDrawnDuration);
        }

        FTAutoTrack.putRUMLaunchPerformance(true, coldStartDuration, coldStartTimeLine,
                preApplicationInit,
                applicationInit,
                firstFrameInit);
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

    /**
     * Record Application.onCreate() timestamp
     *
     * @param nanoTimeLine Application.onCreate() timestamp, unit: nanoseconds
     */
    void appOnCreate(long nanoTimeLine) {
        if (!firstDrawDone.get()) {
            LogUtils.d(TAG, "appOnCreate:" + nanoTimeLine);
            applicationOnCreateTimeLine = nanoTimeLine;
        }
    }


}
