package com.ft.sdk.garble;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import com.ft.sdk.garble.manager.FTManager;

/**
 * BY huangDianHua
 * DATE:2019-12-06 11:18
 * Description:
 */
public class FTActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        FTManager.getFTActivityManager().putActivity(activity, Lifecycle.Event.ON_CREATE);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        FTManager.getFTActivityManager().putActivity(activity, Lifecycle.Event.ON_START);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        FTManager.getFTActivityManager().putActivity(activity, Lifecycle.Event.ON_RESUME);
        FTManager.getSyncTaskManaget().executeSyncPoll();
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
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
        FTManager.getFTActivityManager().removeActivity(activity);

    }
}
