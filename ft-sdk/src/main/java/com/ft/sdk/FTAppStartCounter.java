package com.ft.sdk;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.view.View;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.FirstDrawDoneListener;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
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

    /** Pending candidates, confined to the main thread and released on pause/destroy or completion. */
    private final Map<Activity, FirstDrawDoneListener> firstDrawListeners = new IdentityHashMap<>();

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
     * First main-loop timestamp after Application.onCreate() has returned, unit: nanoseconds
     */
    private long applicationOnCreateCompletedTimeLine = 0;

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

    /**
     * Whether the process was started while the app was not in foreground.
     */
    private boolean launchFromBackground = false;

    private boolean foregroundDuringLaunch = false;

    private boolean backgroundDuringLaunch = false;


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
        firstDrawDone.set(true);
        this.coldStartTimeLineForNanoDuration = Utils.getAppStartTimeNs();
        this.coldStartDuration = coldStartEndTimeLine - coldStartTimeLineForNanoDuration;
        this.coldStartTimeLine = Utils.getCurrentNanoTime() - coldStartDuration;

        this.applicationPreOnCreateDuration = applicationOnCreateTimeLine - coldStartTimeLineForNanoDuration;
        this.applicationOnCreateDuration = firstActivityPreOnActivityTimeline - applicationOnCreateTimeLine;
        this.firsDrawnDuration = coldStartEndTimeLine - firstActivityPreOnActivityTimeline;

        LogUtils.d(TAG, "coldStart:" + coldStartDuration
                + ",coldTimeLine:" + coldStartTimeLineForNanoDuration
                + ",launchFromBackground:" + launchFromBackground
                + ",appLaunchType:" + getColdStartLaunchType());
    }

    void checkFirstActivityPreCreate(long nanoTimeLine) {
        if (!firstDrawDone.get() && firstActivityPreOnActivityTimeline == 0) {
            LogUtils.d(TAG, "checkFirstActivityPreCreate:" + nanoTimeLine);
            firstActivityPreOnActivityTimeline = nanoTimeLine;
        }

    }

    void checkFirstFrameStart(Activity activity, Runnable drawDoneCallback) {
        onAppForeground();
        if (firstDrawDone.get() || firstDrawListeners.containsKey(activity)) {
            return;
        }
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView != null) {
            firstDrawListeners.put(activity,
                    FirstDrawDoneListener.registerForNextDrawCancellable(rootView, () -> {
                        // Registration is only an attempt. Any later Activity may supply the first draw.
                        if (firstDrawDone.compareAndSet(false, true)) {
                            for (FirstDrawDoneListener listener : firstDrawListeners.values()) {
                                listener.cancel();
                            }
                            firstDrawListeners.clear();
                            drawDoneCallback.run();
                        }
                    }));
        }
    }

    /** Release an Activity that leaves the foreground without drawing; later candidates may retry. */
    void cancelFirstFrame(Activity activity) {
        FirstDrawDoneListener listener = firstDrawListeners.remove(activity);
        if (listener != null) {
            listener.cancel();
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
                firstFrameInit,
                getColdStartLaunchType());
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
    void appOnCreate(long nanoTimeLine, Application application) {
        // Repeated Application hooks must preserve the first launch snapshot.
        if (!firstDrawDone.get() && applicationOnCreateTimeLine == 0) {
            applicationOnCreateTimeLine = nanoTimeLine;
            launchFromBackground = isLaunchFromBackground(application);
            LogUtils.d(TAG, "appOnCreate:" + nanoTimeLine
                    + ",launchFromBackground:" + launchFromBackground);
        }
    }

    /**
     * Records the first main-loop opportunity after Application.onCreate() returns.
     *
     * @param nanoTimeLine completion timestamp, unit: nanoseconds
     */
    void appOnCreateCompleted(long nanoTimeLine) {
        if (!firstDrawDone.get() && applicationOnCreateCompletedTimeLine == 0) {
            applicationOnCreateCompletedTimeLine = nanoTimeLine;
            LogUtils.d(TAG, "appOnCreateCompleted:" + nanoTimeLine
                    + ",firstActivityPreCreate:" + firstActivityPreOnActivityTimeline);
            if (firstActivityPreOnActivityTimeline == 0 && !foregroundDuringLaunch) {
                launchFromBackground = true;
                LogUtils.d(TAG, "launchFromBackground:true,noActivityAfterApplication");
            }
        }
    }

    /**
     * Records app-wide lifecycle changes until the cold-start measurement ends.
     */
    void onAppForeground() {
        if (!firstDrawDone.get()) {
            foregroundDuringLaunch = true;
        }
    }

    void onAppBackground() {
        if (!firstDrawDone.get()) {
            backgroundDuringLaunch = true;
        }
    }

    String getColdStartLaunchType() {
        // Entering the foreground to draw does not change a background-origin launch.
        if (launchFromBackground) {
            return Constants.APP_LAUNCH_TYPE_BACKGROUND;
        }
        boolean hasForeground = foregroundDuringLaunch || applicationOnCreateTimeLine != 0;
        if (hasForeground && backgroundDuringLaunch) {
            return Constants.APP_LAUNCH_TYPE_FOREGROUND_AND_BACKGROUND;
        }
        return backgroundDuringLaunch ? Constants.APP_LAUNCH_TYPE_BACKGROUND
                : Constants.APP_LAUNCH_TYPE_FOREGROUND;
    }

    private boolean isLaunchFromBackground(Application application) {
        if (application == null) {
            return false;
        }
        ActivityManager.RunningAppProcessInfo processInfo =
                new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(processInfo);
        return processInfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
    }


}
