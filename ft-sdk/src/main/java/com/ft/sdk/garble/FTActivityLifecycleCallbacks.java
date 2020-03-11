package com.ft.sdk.garble;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.garble.manager.FTActivityManager;
import com.ft.sdk.garble.manager.FTManager;

/**
 * BY huangDianHua
 * DATE:2019-12-06 11:18
 * Description: Activity 生命周期回调类
 */
public class FTActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        FTManager.getFTActivityManager().putActivity(activity, Lifecycle.Event.ON_CREATE);
        FTFragmentManager.getInstance().addFragmentLifecycle(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        FTManager.getFTActivityManager().putActivity(activity, Lifecycle.Event.ON_START);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        FTManager.getFTActivityManager().putActivity(activity, Lifecycle.Event.ON_RESUME);
        FTManager.getSyncTaskManager().executeSyncPoll();
        FTAutoTrack.startPage(activity.getClass());
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        FTAutoTrack.destroyPage(activity.getClass());
        FTManager.getFTActivityManager().putActivity(activity, Lifecycle.Event.ON_PAUSE);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        FTManager.getFTActivityManager().putActivity(activity, Lifecycle.Event.ON_STOP);
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        //移除对 Fragment 的生命周期的监听
        FTFragmentManager.getInstance().removeFragmentLifecycle(activity);
        //从 Activity 的管理栈中移除 Activity
        FTManager.getFTActivityManager().removeActivity(activity);
        //删除 Activity 打开方式的缓存
        FTActivityManager.get().removeActivityStatus(activity.getClass().getName());
    }
}
