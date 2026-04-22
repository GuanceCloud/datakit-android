package com.ft.sdk;


import android.app.Activity;
import android.content.SharedPreferences;

import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-06 13:36
 * Description: {@link Activity} manager class
 */
public final class FTActivityManager {
    public final static String TAG = Constants.LOG_TAG_PREFIX + "ActivityManager";
    private static volatile FTActivityManager instance;

    /**
     * Map of alive activities
     */
    private final ConcurrentHashMap<String, Boolean> activityOpenTypeMap = new ConcurrentHashMap<>();

    private WeakReference<Activity> mCurrentActivity;


    /**
     * Default is {@link AppState#STARTUP}
     */
    private AppState appState = AppState.STARTUP;

    private final Object crashFreeLock = new Object();
    private boolean crashFreeTrackerInitialized;
    private long crashFreeStartTime;
    private long stateStartTime;
    private long foregroundCrashFreeDuration;
    private long backgroundCrashFreeDuration;
    private CrashFreeSnapshot pendingCrashSnapshot;

    private FTActivityManager() {
    }

    public synchronized static FTActivityManager get() {
        if (instance == null) {
            synchronized (FTActivityManager.class) {
                if (instance == null) {
                    instance = new FTActivityManager();
                }
            }
        }
        return instance;
    }

    /**
     * enter foreground
     */
    void appForeground() {
        synchronized (crashFreeLock) {
            ensureCrashFreeTrackerInitialized();
            transitionAppState(AppState.RUN, Utils.getCurrentNanoTime());
        }
    }

    /**
     * enter background
     */
    void appBackGround() {
        synchronized (crashFreeLock) {
            ensureCrashFreeTrackerInitialized();
            transitionAppState(AppState.BACKGROUND, Utils.getCurrentNanoTime());
        }
    }

    /**
     * Get the current {@link AppState}
     *
     * @return {@link AppState}
     */
    AppState getAppState() {
        synchronized (crashFreeLock) {
            ensureCrashFreeTrackerInitialized();
            return appState;
        }
    }

    void setCurrentActivity(Activity activity) {
        mCurrentActivity = new WeakReference<>(activity);
    }

    public Activity curentActivity() {
        return mCurrentActivity != null ? mCurrentActivity.get() : null;
    }

    CrashFreeDuration getCrashFreeDuration(long crashTime, AppState crashState, boolean isPreCrash) {
        synchronized (crashFreeLock) {
            ensureCrashFreeTrackerInitialized();
            CrashFreeSnapshot snapshot = isPreCrash && pendingCrashSnapshot != null
                    ? pendingCrashSnapshot : new CrashFreeSnapshot(
                    foregroundCrashFreeDuration, backgroundCrashFreeDuration, appState, stateStartTime);
            long foregroundDuration = snapshot.foregroundDuration;
            long backgroundDuration = snapshot.backgroundDuration;
            long extraDuration = Math.max(0, crashTime - snapshot.stateStartTime);
            if (isBackgroundState(crashState == AppState.UNKNOWN ? snapshot.state : crashState)) {
                backgroundDuration += extraDuration;
            } else {
                foregroundDuration += extraDuration;
            }
            return new CrashFreeDuration(foregroundDuration, backgroundDuration);
        }
    }

    void resetCrashFreeDuration(long resetTime, AppState resetState) {
        synchronized (crashFreeLock) {
            crashFreeStartTime = resetTime;
            foregroundCrashFreeDuration = 0;
            backgroundCrashFreeDuration = 0;
            appState = normalizeState(resetState);
            stateStartTime = resetTime;
            persistCrashFreeTracker();
        }
    }

    private void ensureCrashFreeTrackerInitialized() {
        if (crashFreeTrackerInitialized) {
            return;
        }
        crashFreeTrackerInitialized = true;
        long now = Utils.getCurrentNanoTime();
        SharedPreferences sp = getSharedPreferences();
        if (sp != null && sp.contains(Constants.FT_CRASH_FREE_START_TIME)) {
            pendingCrashSnapshot = new CrashFreeSnapshot(
                    sp.getLong(Constants.FT_CRASH_FREE_FOREGROUND_DURATION, 0),
                    sp.getLong(Constants.FT_CRASH_FREE_BACKGROUND_DURATION, 0),
                    normalizeState(AppState.getValueFrom(
                            sp.getString(Constants.FT_CRASH_FREE_APP_STATE, AppState.STARTUP.toString()))),
                    sp.getLong(Constants.FT_CRASH_FREE_STATE_START_TIME, now));
        }
        crashFreeStartTime = now;
        foregroundCrashFreeDuration = 0;
        backgroundCrashFreeDuration = 0;
        appState = AppState.STARTUP;
        stateStartTime = now;
        persistCrashFreeTracker();
    }

    private void transitionAppState(AppState nextState, long now) {
        accumulateCurrentStateDuration(now, appState);
        appState = normalizeState(nextState);
        stateStartTime = now;
        persistCrashFreeTracker();
    }

    private void accumulateCurrentStateDuration(long endTime, AppState state) {
        long duration = Math.max(0, endTime - stateStartTime);
        if (isBackgroundState(state)) {
            backgroundCrashFreeDuration += duration;
        } else {
            foregroundCrashFreeDuration += duration;
        }
    }

    private boolean isBackgroundState(AppState state) {
        return normalizeState(state) == AppState.BACKGROUND;
    }

    private AppState normalizeState(AppState state) {
        return state == null || state == AppState.UNKNOWN ? AppState.STARTUP : state;
    }

    private void persistCrashFreeTracker() {
        SharedPreferences sp = getSharedPreferences();
        if (sp == null) {
            return;
        }
        sp.edit()
                .putLong(Constants.FT_CRASH_FREE_START_TIME, crashFreeStartTime)
                .putLong(Constants.FT_CRASH_FREE_FOREGROUND_DURATION, foregroundCrashFreeDuration)
                .putLong(Constants.FT_CRASH_FREE_BACKGROUND_DURATION, backgroundCrashFreeDuration)
                .putString(Constants.FT_CRASH_FREE_APP_STATE, appState.toString())
                .putLong(Constants.FT_CRASH_FREE_STATE_START_TIME, stateStartTime)
                .apply();
    }

    private SharedPreferences getSharedPreferences() {
        if (FTApplication.getApplication() == null) {
            return null;
        }
        return Utils.getSharedPreferences(FTApplication.getApplication());
    }

    static final class CrashFreeDuration {
        final long foregroundDuration;
        final long backgroundDuration;

        CrashFreeDuration(long foregroundDuration, long backgroundDuration) {
            this.foregroundDuration = foregroundDuration;
            this.backgroundDuration = backgroundDuration;
        }
    }

    private static final class CrashFreeSnapshot {
        final long foregroundDuration;
        final long backgroundDuration;
        final AppState state;
        final long stateStartTime;

        CrashFreeSnapshot(long foregroundDuration, long backgroundDuration, AppState state,
                          long stateStartTime) {
            this.foregroundDuration = foregroundDuration;
            this.backgroundDuration = backgroundDuration;
            this.state = state;
            this.stateStartTime = stateStartTime;
        }
    }

}
