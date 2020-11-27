package com.ft.sdk.garble.manager;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import com.ft.sdk.FTApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-06 13:36
 * Description: Activity 管理类
 */
public class FTActivityManager {
    public final static String TAG = "FTActivityManager";
    public ConcurrentHashMap<String, Boolean> isFirstResume;
    private static volatile FTActivityManager instance;
    //栈顶 Activity
    private Activity topActivity;
    //存在的 Activity
    private List<Activity> activityList;

    private ConcurrentHashMap<String, Boolean> activityOpenTypeMap;

    private AppState appState;

    private FTActivityManager() {
        activityList = new ArrayList<>();
        activityOpenTypeMap = new ConcurrentHashMap<>();
        isFirstResume = new ConcurrentHashMap<>();
    }

    public synchronized static FTActivityManager get() {
        if (instance == null) {
            instance = new FTActivityManager();
        }
        return instance;
    }

    /**
     * 获取当前存活的Activity
     *
     * @return
     */
    public int getActiveCount() {
        return activityList == null ? 0 : activityList.size();
    }

    /**
     * 获得栈顶Activity
     *
     * @return
     */
    public Activity getTopActivity() {
        return topActivity;
    }


    public void putActivity(Activity activity) {
        topActivity = activity;
        if (activityList == null) {
            activityList = new ArrayList<>();
        }
        activityList.add(activity);
    }

    public void removeActivity() {
        try {
            if (activityList.size() > 3) {
                Activity activity = activityList.remove(0);
                removeActivityStatus(activity.getClass().getName());
            }
            topActivity = (activityList == null || activityList.size() <= 0) ? null : activityList.get(activityList.size() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 得到上一个 Activity
     *
     * @return
     */
    public Class getLastActivity() {
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
    public boolean lastTwoActivitySame() {
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
    public void putActivityOpenFromFragment(String className, boolean fromFragment) {
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
    public boolean getActivityOpenFromFragment(String className) {
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
