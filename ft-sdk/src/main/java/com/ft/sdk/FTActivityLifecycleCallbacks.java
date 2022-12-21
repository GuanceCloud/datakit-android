package com.ft.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ft.sdk.garble.utils.Constants;

import java.util.HashMap;


/**
 * BY huangDianHua
 * DATE:2019-12-06 11:18
 * Description: {@link Activity} 生命周期回调类
 * <p>
 * 用于监听 {@link Activity} 生命周期，结合 {@link LifeCircleTraceCallback},在
 * {@link #onActivityPreStarted(Activity),#onActivityPreCreated(Activity, Bundle), #onActivityPostCreated(Activity)}
 * 从而输出以 {@link Activity} 为 {@link  Constants#FT_MEASUREMENT_RUM_VIEW} 指标
 * 页面加载时间：{@link Constants#KEY_RUM_VIEW_LOAD}
 * 启动时间：{@link Constants#ACTION_TYPE_LAUNCH_HOT},{@link Constants#ACTION_NAME_LAUNCH_COLD}
 *
 * 这些可以通过观测云 Studio <a href="https://docs.guance.com/real-user-monitoring/explorer/view/">查看器 View</a> 进行查看
 *
 */
public class FTActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private final LifeCircleTraceCallback mAppRestartCallback = new LifeCircleTraceCallback();


    /**
     * {@link Activity} 创建
     * <p>
     * no use
     *
     * @param activity           {@link Activity}.
     * @param savedInstanceState {@link Activity#onSaveInstanceState(Bundle)}
     */
    @Override
    public void onActivityCreated(@NonNull Activity activity, @NonNull Bundle savedInstanceState) {
    }

    /**
     * {@link Activity#onStart()}}  之前调用，使用 {@link LifeCircleTraceCallback#onPreStart()} 处理，
     * 集成应用中 {@link Activity#onStart()}} 逻辑
     *
     * @param activity {@link Activity}.
     */
    @Override
    public void onActivityPreStarted(@NonNull Activity activity) {
        mAppRestartCallback.onPreStart();

    }

    /**
     * {@link Activity#onStart()}} 之后调用，no use
     *
     * @param activity {@link Activity}.
     */
    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    /**
     * {@link LifeCircleTraceCallback#onPreOnCreate(Context)}  } 用于记录应用开始时间
     *
     * @param activity           {@link Activity}.
     * @param savedInstanceState {@link Activity#onSaveInstanceState(Bundle)}.
     */
    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        mAppRestartCallback.onPreOnCreate(activity);
    }

    /**
     * {@link LifeCircleTraceCallback#onPostOnCreate(Context)}  } 用于记录应用创建完毕时间
     *
     * @param activity           {@link Activity }.
     * @param savedInstanceState {@link Activity#onSaveInstanceState(Bundle)}.
     */
    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        mAppRestartCallback.onPostOnCreate(activity);

    }

    /**
     * {@link Activity#onResume()}  } 恢复事件
     * <p>
     * {@link FTRUMConfigManager#isRumEnable()} 开启状态下，使用 {@link FTRUMGlobalManager#startView(String, HashMap)}
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
     * {@link Activity#onPostResume()} 恢复之后 ，{@link  LifeCircleTraceCallback#onPostResume(Context)}
     *
     * @param activity {@link Activity }.
     */
    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {
        mAppRestartCallback.onPostResume(activity);
    }

    /**
     * {@link Activity#onPause()}时调用，{@link FTRUMConfigManager#isRumEnable()} 开启状态下，
     * 使用 {@link FTRUMGlobalManager#stopView()}
     *
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
     * {@link Activity#onStop()} 时调用，{@link  LifeCircleTraceCallback#onStop()} 处理页面休眠事务处理
     *
     * @param activity {@link Activity }.
     */
    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        mAppRestartCallback.onStop();
    }


    /**
     * {@link Activity} 保存状态
     * <p>
     * no use
     *
     * @param activity {@link Activity }.
     * @param outState {@link Activity#onSaveInstanceState(Bundle)}.
     */
    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    /**
     * {@link Activity} 销毁
     * <p>
     * no use
     *
     * @param activity {@link Activity }.
     */
    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    /**
     * {@link Activity#onDestroy()}之后调用 ，配合 {@link LifeCircleTraceCallback#onPostDestroy(Context)}
     * 处理一些 {@link Activity} 生命周期结束后的逻辑
     *
     * @param activity {@link Activity }.
     */
    @Override
    public void onActivityPostDestroyed(Activity activity) {
        mAppRestartCallback.onPostDestroy(activity);

    }
}
