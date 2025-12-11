package com.ft.sdk;


import android.app.Activity;

import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.utils.Constants;

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
     * enter foreground
     */
    void appForeground() {
        this.appState = AppState.RUN;
    }

    /**
     * enter background
     */
    void appBackGround() {
        this.appState = AppState.BACKGROUND;
    }

    /**
     * Get the current {@link AppState}
     *
     * @return {@link AppState}
     */
    AppState getAppState() {
        return appState;
    }

}
