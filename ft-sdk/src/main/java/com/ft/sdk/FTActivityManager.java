package com.ft.sdk;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-06 13:36
 * Description: Activity 管理类
 */
public final class FTActivityManager {
    public final static String TAG = "FTActivityManager";
    public ConcurrentHashMap<String, Boolean> isFirstResume;
    private static volatile FTActivityManager instance;
    //栈顶 Activity
    private Activity topActivity;
    //存在的 Activity
    private List<Activity> activityList;

    private ConcurrentHashMap<String, Boolean> activityOpenTypeMap;

    private AppState appState = AppState.STARTUP;

    private FTActivityManager() {
        activityList = new ArrayList<>();
        activityOpenTypeMap = new ConcurrentHashMap<>();
        isFirstResume = new ConcurrentHashMap<>();
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
     * 获取当前存活的Activity
     *
     * @return
     */
    int getActiveCount() {
        return activityList == null ? 0 : activityList.size();
    }

    /**
     * 获得栈顶Activity
     *
     * @return
     */
    Activity getTopActivity() {
        return topActivity;
    }


    void putActivity(Activity activity) {
        topActivity = activity;
        if (activityList == null) {
            activityList = new ArrayList<>();
        }

        activityList.add(activity);
    }

    void removeActivity(Activity activity) {
        if (activityList != null) {
            try {
                for (int i = activityList.size() - 1; i >= 0; i--) {
                    if (activityList.get(i).equals(activity)) {
                        activityList.remove(activity);
                        removeActivityStatus(activity.getClass().getName());
                        break;
                    }
                }
            } catch (Exception e) {
                LogUtils.e(TAG, e.getMessage());
            }

        }
    }

    /**
     * 得到上一个 Activity
     *
     * @return
     */
    Class getLastActivity() {
        if (activityList != null && activityList.size() > 1) {
            Activity activity = activityList.get(activityList.size() - 2);
            if (activity != null) {
                return activity.getClass();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 判断最后两个Activity 是否为同一个 Activity
     *
     * @return
     */
    boolean lastTwoActivitySame() {
        if (activityList != null && activityList.size() > 1) {
            Activity activity1 = activityList.get(activityList.size() - 2);
            Activity activity2 = activityList.get(activityList.size() - 1);
            if (activity1 != null && activity2 != null) {
                return activity1.getClass().getName().equals(activity2.getClass().getName());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 存储每个 Activity 是由什么方式打开的
     *
     * @param className
     * @param fromFragment
     */
    void putActivityOpenFromFragment(String className, boolean fromFragment) {
        if (activityOpenTypeMap == null) {
            activityOpenTypeMap = new ConcurrentHashMap<>();
        }
        activityOpenTypeMap.put(className, fromFragment);
    }

    /**
     * 返回每个 Activity 是由什么方式打开的
     *
     * @param className
     * @return
     */
    boolean getActivityOpenFromFragment(String className) {
        if (activityOpenTypeMap != null && activityOpenTypeMap.containsKey(className)) {
            return activityOpenTypeMap.get(className);
        }
        return false;
    }

    /**
     * 删除对应 Activity 的打开状态
     *
     * @param className
     */
    void removeActivityStatus(String className) {
        if (activityOpenTypeMap != null) {
            activityOpenTypeMap.remove(className);
        }
    }

    boolean isAppForeground() {
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

    void setAppState(AppState state) {
        this.appState = state;
    }

    AppState getAppState() {
        return appState;
    }

}
