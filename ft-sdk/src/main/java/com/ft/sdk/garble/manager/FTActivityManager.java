package com.ft.sdk.garble.manager;


import android.app.Activity;

import androidx.lifecycle.Lifecycle;

import com.ft.sdk.garble.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-06 13:36
 * Description: Activity 管理类
 */
public class FTActivityManager {
    private static volatile FTActivityManager instance;
    //栈顶 Activity
    private Activity topActivity;
    //存在的 Activity
    private List<Activity> activityList;

    private ConcurrentHashMap<String, Boolean> activityOpenTypeMap;

    private FTActivityManager() {
        activityList = new ArrayList<>();
        activityOpenTypeMap = new ConcurrentHashMap<>();
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


    public void putActivity(Activity activity, Lifecycle.Event event) {
        topActivity = activity;
        if (activityList == null) {
            activityList = new ArrayList<>();
        }
        //if (!activityList.contains(activity)) {
            activityList.add(activity);
        //}
    }

    public void removeActivity(Activity activity) {
        activityList.remove(activity);
        topActivity = (activityList == null || activityList.size() <= 0) ? null : activityList.get(activityList.size() - 1);
    }

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
     * 存储每个 Activity 是由什么方式打开的
     *
     * @param className
     * @param fromFragment
     */
    public void putActivityStatus(String className, boolean fromFragment) {
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
    public boolean getActivityStatus(String className) {
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
    public void removeActivityStatus(String className) {
        if (activityOpenTypeMap != null) {
            activityOpenTypeMap.remove(className);
        }
    }

    public void printTest(Activity activity) {
        LogUtils.d(FTActivityManager.class.getSimpleName() + "\n" +
                "activeCount=" + activityList.size() + "\n" +
                "topActivity=" + topActivity + "\n");
    }

}
