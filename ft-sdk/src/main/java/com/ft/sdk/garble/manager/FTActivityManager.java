package com.ft.sdk.garble.manager;


import android.app.Activity;
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
    public ConcurrentHashMap<String,Boolean> isFirstResume;
    private static volatile FTActivityManager instance;
    //栈顶 Activity
    private Activity topActivity;
    //存在的 Activity
    private List<Activity> activityList;

    private ConcurrentHashMap<String, Boolean> activityOpenTypeMap;

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
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *  得到上一个 Activity
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
