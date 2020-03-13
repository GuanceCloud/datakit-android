package com.ft.sdk.garble;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import com.ft.sdk.FTApplication;
import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.garble.manager.FTActivityManager;
import com.ft.sdk.garble.manager.FTManager;
import com.ft.sdk.garble.utils.LocationUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-06 11:18
 * Description: Activity 生命周期回调类
 */
public class FTActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    ConcurrentHashMap<String,Boolean> isFirstResume = new ConcurrentHashMap<>();
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        FTFragmentManager.getInstance().addFragmentLifecycle(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        FTFragmentManager.getInstance().clearFragmentList(activity.getClass().getName());
        if(isFirstResume.containsKey(activity.getClass().getName()) && isFirstResume.get(activity.getClass().getName())){
            FTActivityManager.get().putActivityStatus(activity.getClass().getName(),false);
        }
        FTManager.getFTActivityManager().putActivity(activity, Lifecycle.Event.ON_RESUME);
        FTManager.getSyncTaskManager().executeSyncPoll();
        FTAutoTrack.startPage(activity.getClass());
        isFirstResume.put(activity.getClass().getName(),true);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        FTAutoTrack.destroyPage(activity.getClass());
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        isFirstResume.remove(activity.getClass().getName());
        //移除对 Fragment 的生命周期的监听
        FTFragmentManager.getInstance().removeFragmentLifecycle(activity);
        //从 Activity 的管理栈中移除 Activity
        //FTManager.getFTActivityManager().removeActivity(activity);
        //删除 Activity 打开方式的缓存
        //FTActivityManager.get().removeActivityStatus(activity.getClass().getName());
    }
}
