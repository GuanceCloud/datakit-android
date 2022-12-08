package com.ft.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ft.sdk.garble.FTFragmentManager;

/**
 * BY huangDianHua
 * DATE:2019-12-06 11:18
 * Description: Activity 生命周期回调类
 */
public class FTActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private final LifeCircleTraceCallback mAppRestartCallback = new LifeCircleTraceCallback();


    /**
     * 应用创建
     *
     * @param activity           {@link Activity}.
     * @param savedInstanceState {@link Activity#onSaveInstanceState(Bundle)}
     */
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        //防止监听网速的线程挂掉，在页面打开时判断线程是够挂了，挂了重启
//        if (FTMonitorConfig.get().isMonitorType(MonitorType.NETWORK)) {
//            NetUtils.get().startMonitorNetRate();
//        }
        FTFragmentManager.getInstance().addFragmentLifecycle(activity);
    }

    /**
     * {@link Activity#onStart()}}  之前调用，使用 {@link LifeCircleTraceCallback#onPreStart()} 处理，
     * 集成应用中 {@link Activity#onStart()}} 逻辑
     * @param activity {@link Activity}.
     */
    @Override
    public void onActivityPreStarted(@NonNull Activity activity) {
        mAppRestartCallback.onPreStart();

    }

    /**
     * no use
     * @param activity  {@link Activity}.
     */
    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    /**
     * {@link LifeCircleTraceCallback#onPreOnCreate(Context)}  }
     *
     * @param activity           {@link Activity}.
     * @param savedInstanceState {@link Activity#onSaveInstanceState(Bundle)}.
     */
    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        mAppRestartCallback.onPreOnCreate(activity);
    }

    /**
     * {@link LifeCircleTraceCallback#onPostOnCreate(Context)}  }
     *
     * @param activity           {@link Activity }.
     * @param savedInstanceState {@link Activity#onSaveInstanceState(Bundle)}.
     */
    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        mAppRestartCallback.onPostOnCreate(activity);

    }

    /**
     *
     *
     *
     * @param activity {@link Activity }.
     */
    @Override
    public void onActivityResumed(@NonNull Activity activity) {

        //页面打开埋点数据插入
        FTRUMConfigManager manager = FTRUMConfigManager.get();
        if (manager.isRumEnable() && manager.getConfig().isEnableTraceUserView()) {
            FTRUMGlobalManager.get().startView(activity.getClass().getSimpleName());
        }


        //开启同步
        if (FTSdk.checkInstallState()) {
            SyncTaskManager.get().executeSyncPoll();
        }
    }

    /**
     * @param activity {@link Activity }.
     */
    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {
        mAppRestartCallback.onPostResume(activity);
    }

    /**
     * @param activity {@link Activity }.
     */
    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        //页面关闭埋点数据插入
        FTRUMConfigManager manager = FTRUMConfigManager.get();
        if (manager.isRumEnable() && manager.getConfig().isEnableTraceUserView()) {
            FTRUMGlobalManager.get().stopView();
        }

    }

    /**
     * @param activity {@link Activity }.
     */
    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        mAppRestartCallback.onStop();
    }


    /**
     *
     * @param activity           {@link Activity }.
     * @param outState {@link Activity#onSaveInstanceState(Bundle)}.
     */
    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    /**
     * @param activity {@link Activity }.
     */
    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        //移除当前页面是否第一次调用 onResume 的标记
        FTFragmentManager.getInstance().removeFragmentLifecycle(activity);
    }

    /**
     * @param activity {@link Activity }.
     */
    @Override
    public void onActivityPostDestroyed(@NonNull Activity activity) {
        mAppRestartCallback.onPostDestroy(activity);

    }
}
