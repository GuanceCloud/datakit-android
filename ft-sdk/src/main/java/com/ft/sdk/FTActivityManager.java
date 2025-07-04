package com.ft.sdk;


import android.app.Activity;

import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.utils.Constants;

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
     * Store how each {@link Activity} was opened
     *
     * @param className    Name of the derived {@link Activity} class
     * @param fromFragment
     */
    void putActivityOpenFromFragment(String className, boolean fromFragment) {
        activityOpenTypeMap.put(className, fromFragment);
    }

    /**
     * Return how each Activity was opened
     *
     * @param className
     * @return
     */
    boolean getActivityOpenFromFragment(String className) {
        if (activityOpenTypeMap.containsKey(className)) {
            return Boolean.TRUE.equals(activityOpenTypeMap.get(className));
        }
        return false;
    }

    /**
     * Remove the open state of the corresponding Activity
     *
     * @param className
     */
    void removeActivityStatus(String className) {
        activityOpenTypeMap.remove(className);
    }


    /**
     * Set the current {@link AppState}
     *
     * @param state {@link AppState} application running state
     */
    void setAppState(AppState state) {
        this.appState = state;
    }

    /**
     * Get the current {@link AppState}
     *
     * @return {@link AppState}
     */
    AppState getAppState() {
        return appState;
    }

    void setCurrentActivity(Activity activity) {
        mCurrentActivity = new WeakReference<>(activity);
    }

    public Activity curentActivity() {
        return mCurrentActivity != null ? mCurrentActivity.get() : null;
    }

}
