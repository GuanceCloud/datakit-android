package com.ft.sdk;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.utils.AopUtils;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.LinkedHashMap;


/**
 * create: by huangDianHua
 * time: 2020/6/17 17:50:45
 * description: Handle current application going to background
 * for 10 seconds and then returning to foreground
 */
class LifeCircleTraceCallback {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "LifeCircleTraceCallback";
    /**
     * Message channel
     */
    public static final int MSG_CHECK_SLEEP_STATUS = 1;
    /**
     * Sleep delay time
     */
    public static final int DELAY_SLEEP_MILLIS = 10000;//10 seconds
    /**
     * Whether already in sleep state, here going to background for 10 seconds is considered as sleep
     */
    private boolean alreadySleep = true;
    /**
     * Determine if the first page is created for the first time
     */
    private boolean mInited = false;//
    /**
     * Application startup time point
     */
    private long startTimeNanoTime = 0;

    private long startClockTimeNano = 0;

    /**
     * Cache creation time point
     */
    private final LinkedHashMap<Context, Long> mCreateMap = new LinkedHashMap<>();

    /**
     * Used to send delayed messages, execute sleep {@link #alreadySleep} = true operation after 10 seconds
     */
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_CHECK_SLEEP_STATUS) {
                checkLongTimeSleep();
            }
        }
    };

    /**
     * Before {@link Activity#onStart()}
     */
    public void onPreStart() {
        if (alreadySleep) {// Indicates returning from background
            if (mInited) {
                //hot start
                startTimeNanoTime = Utils.getCurrentNanoTime();
                startClockTimeNano= SystemClock.elapsedRealtimeNanos();
            }
        }


    }

    /**
     * Before {@link Activity#onCreate(Bundle)}
     *
     * @param context
     */
    public void onPreOnCreate(Context context) {
        mCreateMap.put(context, SystemClock.elapsedRealtimeNanos());
        if (!mInited) {
//            FTAutoTrack.startApp();
            // warn start
            startTimeNanoTime = Utils.getCurrentNanoTime();
            startClockTimeNano= SystemClock.elapsedRealtimeNanos();
        }
    }

    /**
     * After {@link Activity#onCreate(Bundle)}
     *
     * @param context
     */
    public void onPostOnCreate(Context context) {
        FTRUMConfigManager manager = FTRUMConfigManager.get();
        FTRUMConfig config = manager.getConfig();

        if (manager.isRumEnable()) {
            //config nonnull here ignore warning
            if (config.isEnableTraceUserView()) {
                Long startTime = mCreateMap.get(context);
                if (startTime != null) {
                    long durationNS = SystemClock.elapsedRealtimeNanos() - startTime;
                    String viewName = AopUtils.getClassName(context);
                    FTRUMInnerManager.get().onCreateView(viewName, durationNS);
                }
            }
        }
    }

    /**
     * @param context
     */
    public void onPostOnStart(Context context) {

    }

    /**
     * see <a href="https://developer.android.com/topic/performance/vitals/launch-time?hl=zh-cn#warm">Launch time calculation rules</a>
     * After {@link Activity#onResume() }
     *
     * @param context
     */
    public void onPostResume(Context context) {
        FTRUMConfigManager manager = FTRUMConfigManager.get();
        FTRUMConfig config = manager.getConfig();

        if (!mInited) {
            FTActivityManager.get().setAppState(AppState.RUN);
            FTAppStartCounter.get().coldStart(Utils.getCurrentNanoTime());
            //config nonnull here ignore warning
            if (manager.isRumEnable() && config.isEnableTraceUserAction()) {
                // If SDK is not initialized, this data will be supplemented 
                // after SDK delayed initialization
                FTAppStartCounter.get().coldStartUpload();
            }
        }

        // Already sleeping
        if (alreadySleep) {
            if (mInited) {
                if (config != null && config.isRumEnable() && config.isEnableTraceUserAction()) {
                    if (startTimeNanoTime > 0) {
                        long now = SystemClock.elapsedRealtimeNanos();
                        FTAppStartCounter.get().hotStart(now - startClockTimeNano,
                                startTimeNanoTime);

                    }

                }
            }
            FTSdk.updateRemoteConfig();
            alreadySleep = false;
        }
        // Avoid duplicate calculation of page startup statistics
        if (!mInited) {
            mInited = true;
        }
    }

    public void onPreResume(Activity activity) {
        FTActivityManager.get().setCurrentActivity(activity);
    }

    /**
     * Triggered when all Activities are onStop
     */
    public void onEnterBackground() {
        if (FTSdk.checkInstallState()) {
            handler.removeMessages(MSG_CHECK_SLEEP_STATUS);
            // Execute after sleeping for a while to distinguish short-term wake-up behaviors
            handler.sendEmptyMessageDelayed(MSG_CHECK_SLEEP_STATUS, DELAY_SLEEP_MILLIS);
        }
    }

    /**
     * {@link Activity#onDestroy() }
     *
     * @param context
     */
    public void onPostDestroy(Context context) {
        mCreateMap.remove(context);
        if (mCreateMap.isEmpty()) {
            mInited = false;
            LogUtils.d(TAG, "Application all close");
        }
    }

    /**
     * Check if long-term sleep
     */
    private void checkLongTimeSleep() {
        boolean appForeground = FTActivityLifecycleCallbacks.isAppInForeground();
        if (!appForeground) {
            alreadySleep = true;
        }
    }

}
