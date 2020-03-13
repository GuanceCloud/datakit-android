package com.ft.sdk.garble;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * 监听系统 Fragment 的生命周期回调
 */
public class FTFragmentLifecycleCallback extends FragmentManager.FragmentLifecycleCallbacks {

    Activity activity;
    FTFragmentLifecycleHandler fragmentLifecycleHandler;

    FTFragmentLifecycleCallback(Activity activity) {
        this.activity = activity;
        fragmentLifecycleHandler = new FTFragmentLifecycleHandler(activity);
    }

    @Override
    public void onFragmentActivityCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
        fragmentLifecycleHandler.onFragmentActivityCreated(f.getClass());
    }

    @Override
    public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        fragmentLifecycleHandler.onFragmentResumed(f.getClass());
    }

    @Override
    public void onFragmentPaused(@NonNull FragmentManager fm, @NonNull Fragment f) {
        fragmentLifecycleHandler.onFragmentPaused(f.getClass());
    }

    @Override
    public void onFragmentStopped(@NonNull FragmentManager fm, @NonNull Fragment f) {
        fragmentLifecycleHandler.onFragmentStopped(f.getClass());
    }

    @Override
    public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        fragmentLifecycleHandler.onFragmentDestroyed(f.getClass());
    }

    /**
     * 设置 Fragment 是否可见
     *
     * @param fragment
     * @param isVisible
     */
    public void setUserVisibleHint(Class fragment, boolean isVisible) {
        fragmentLifecycleHandler.setUserVisibleHint(fragment, isVisible);
    }
}
