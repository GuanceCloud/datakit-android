package com.ft.sdk;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import com.ft.sdk.garble.bean.AppState;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-06 13:36
 * Description: {@link Activity} 管理类
 */
public final class FTActivityManager {
    public final static String TAG = "FTActivityManager";
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
     * @param className
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
     * 判断是否应用是否在前台
     *
     * @return true 前台，反之为后台
     */
    public boolean isAppForeground() {
        ActivityManager am = (ActivityManager) FTApplication.getApplication().getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return false;
        List<ActivityManager.RunningAppProcessInfo> info = am.getRunningAppProcesses();
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningAppProcessInfo aInfo : info) {
            if (aInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (aInfo.processName.equals(FTApplication.getApplication().getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 设置当前 {@link AppState}
     * @param state {@link AppState}
     */
    void setAppState(AppState state) {
        this.appState = state;
    }

    /**
     * @return {@link AppState}
     */
    AppState getAppState() {
        return appState;
    }

}
