package com.ft.sdk;


import android.app.Activity;

import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.utils.Constants;

import java.util.concurrent.ConcurrentHashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-06 13:36
 * Description: {@link Activity} 管理类
 */
public final class FTActivityManager {
    public final static String TAG = Constants.LOG_TAG_PREFIX + "ActivityManager";
    private static volatile FTActivityManager instance;

    /**
     *
     */
    private final ConcurrentHashMap<String, Boolean> activityOpenTypeMap = new ConcurrentHashMap<>();

    /**
     * 默认为 {@link AppState#STARTUP}
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
     * 存储每个 {@link Activity} 是由什么方式打开的
     *
     * @param className    {@link Activity} 衍生类名
     * @param fromFragment
     */
    void putActivityOpenFromFragment(String className, boolean fromFragment) {
        activityOpenTypeMap.put(className, fromFragment);
    }

    /**
     * 返回每个 Activity 是由什么方式打开的
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
     * 删除对应 Activity 的打开状态
     *
     * @param className
     */
    void removeActivityStatus(String className) {
        activityOpenTypeMap.remove(className);
    }


    /**
     * 设置当前 {@link AppState}
     *
     * @param state {@link AppState} 应用运行状态
     */
    void setAppState(AppState state) {
        this.appState = state;
    }

    /**
     * 获取当前 {@link AppState}
     *
     * @return {@link AppState}
     */
    AppState getAppState() {
        return appState;
    }

}
