package com.ft.sdk.garble.manager;


import android.app.Activity;

import androidx.lifecycle.Lifecycle;

import com.ft.sdk.garble.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
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
    //存储 Activity 的最后的状态
    private ConcurrentHashMap<String, Lifecycle.Event> activityEventMap;
    private FTActivityManager() {
        activityList = new ArrayList<>();
        activityEventMap = new ConcurrentHashMap<>();
    }

    public synchronized static FTActivityManager get() {
        if (instance == null) {
            instance = new FTActivityManager();
        }
        return instance;
    }

    /**
     * 获取当前存活的Activity
     * @return
     */
    public int getActiveCount() {
        return activityList==null?0:activityList.size();
    }

    /**
     * 获得栈顶Activity
     * @return
     */
    public Activity getTopActivity() {
        return topActivity;
    }

    /**
     * 判断程序是否在前台运行
     * @return
     */
    public boolean isForeground(){
        if(activityEventMap == null || activityEventMap.isEmpty()){
            return false;
        }
        if (activityEventMap.contains(Lifecycle.Event.ON_RESUME)) {
            return true;
        }
        return false;
    }

    public void putActivity(Activity activity, Lifecycle.Event event){
        topActivity = activity;
        if(activityList == null){
            activityList = new ArrayList<>();
        }
        if(activityEventMap == null){
            activityEventMap = new ConcurrentHashMap<>();
        }

        if(!activityList.contains(activity)) {
            activityList.add(activity);
        }
        activityEventMap.put(activity.toString(),event);

    }
    public void removeActivity(Activity activity) {
        activityList.remove(activity);
        activityEventMap.remove(activity.toString());
        topActivity = (activityList == null || activityList.size()<=0)?null:activityList.get(activityList.size()-1);
    }

    public String getLastActivity(){
        if(activityList != null && activityList.size()>1){
            Activity activity = activityList.get(activityList.size()-2);
            return activity.getLocalClassName();
        }else{
            return "root";
        }
    }

    public void printTest(Activity activity){
        LogUtils.d(FTActivityManager.class.getSimpleName()+"\n"+
                "activeCount="+activityList.size()+"\n"+
                "topActivity="+topActivity+"\n"+
                "activityState="+activityEventMap.get(activity.toString()));
    }

}
