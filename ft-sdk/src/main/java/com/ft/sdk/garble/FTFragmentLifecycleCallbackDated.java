package com.ft.sdk.garble;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.util.concurrent.ConcurrentLinkedQueue;
/**
 * 监听系统 Fragment 的生命周期回调
 */
@SuppressWarnings("deprecation")
@RequiresApi(api = Build.VERSION_CODES.O)
public class FTFragmentLifecycleCallbackDated extends FragmentManager.FragmentLifecycleCallbacks {
    Activity activity;
    FTFragmentLifecycleHandler fragmentLifecycleHandler;
    FTFragmentLifecycleCallbackDated(Activity activity){
        this.activity = activity;
        fragmentLifecycleHandler = new FTFragmentLifecycleHandler(activity);
    }
    @Override
    public void onFragmentActivityCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
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
    public void onFragmentDestroyed(FragmentManager fm, Fragment f) {
        fragmentLifecycleHandler.onFragmentDestroyed(f.getClass());
    }

    /**
     * 设置 Fragment 是否可见
     * @param fragment
     * @param isVisible
     */
    public void setUserVisibleHint(Class fragment,boolean isVisible){
        fragmentLifecycleHandler.setUserVisibleHint(fragment,isVisible);
    }
}
