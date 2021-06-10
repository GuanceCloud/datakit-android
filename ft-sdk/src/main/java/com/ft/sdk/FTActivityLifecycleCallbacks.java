package com.ft.sdk;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ft.sdk.garble.FTFragmentManager;
import com.ft.sdk.garble.FTRUMConfig;
import com.ft.sdk.garble.utils.AopUtils;
import com.ft.sdk.garble.utils.Utils;

import java.util.HashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-06 11:18
 * Description: Activity 生命周期回调类
 */
public class FTActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private final AppRestartCallback mAppRestartCallback = new AppRestartCallback();
    private final HashMap<Activity, Long> mCreateMap = new HashMap<>();


    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        //防止监听网速的线程挂掉，在页面打开时判断线程是够挂了，挂了重启
//        if (FTMonitorConfig.get().isMonitorType(MonitorType.NETWORK)) {
//            NetUtils.get().startMonitorNetRate();
//        }
        FTFragmentManager.getInstance().addFragmentLifecycle(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        mAppRestartCallback.onStart();
    }

    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        mCreateMap.put(activity, Utils.getCurrentNanoTime());
        mAppRestartCallback.onPreOnCreate();
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        mAppRestartCallback.onPostOnCreate();
        if (FTRUMConfig.get().isRumEnable()) {

            Long startTime = mCreateMap.get(activity);
            if (startTime != null) {
                long duration = Utils.getCurrentNanoTime() - startTime;
                String viewName = AopUtils.getClassName(activity);
                FTAutoTrack.putRUMViewLoadPerformance(viewName, duration);
            }
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        boolean isFirstLoad = true;
        if (FTActivityManager.get().isFirstResume.containsKey(activity.getClass().getName())
                && FTActivityManager.get().isFirstResume.get(activity.getClass().getName())) {
            isFirstLoad = false;
        }
        FTManager.getFTActivityManager().putActivity(activity);

        //页面打开埋点数据插入
        FTAutoTrack.startPage(activity.getClass(), isFirstLoad);
        //开启同步
        FTManager.getSyncTaskManager().executeSyncPoll();
        //标记当前页面是否是第一次调用OnResume方法
        FTActivityManager.get().isFirstResume.put(activity.getClass().getName(), true);
    }

    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {
        mAppRestartCallback.onPostResume();
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        //页面关闭埋点数据插入
        FTAutoTrack.destroyPage(activity.getClass());
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        mAppRestartCallback.onStop();
    }


    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        //移除当前页面是否第一次调用 onResume 的标记
        FTActivityManager.get().isFirstResume.remove(activity.getClass().getName());
        //移除对 Fragment 的生命周期的监听
        FTFragmentManager.getInstance().removeFragmentLifecycle(activity);
//        //从 Activity 的管理栈中移除 Activity
//        FTManager.getFTActivityManager().removeActivity(activity);
        mCreateMap.remove(activity);
    }

    @Override
    public void onActivityPostDestroyed(@NonNull Activity activity) {
        mAppRestartCallback.onPostDestroy();

    }
}
