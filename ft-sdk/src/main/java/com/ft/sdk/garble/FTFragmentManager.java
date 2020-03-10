package com.ft.sdk.garble;

import android.app.Activity;
import android.os.Build;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.ft.sdk.garble.utils.LogUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局 Fragment 生命周期管理类
 */
public class FTFragmentManager {
    private static FTFragmentManager mFragmentManager;
    private ConcurrentHashMap<String, FragmentManager.FragmentLifecycleCallbacks> fragmentLifecycleCall = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, android.app.FragmentManager.FragmentLifecycleCallbacks> fragmentLifecycleCallDated = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Activity> activityConcurrentHashMap = new ConcurrentHashMap<>();
    //应该被忽略的Fragment
    public List<String> ignoreFragments = Arrays.asList("com.bumptech.glide.manager.SupportRequestManagerFragment");
    private FTFragmentManager() {

    }

    public static FTFragmentManager getInstance() {
        synchronized (FTFragmentManager.class) {
            if (mFragmentManager == null) {
                mFragmentManager = new FTFragmentManager();
            }
            return mFragmentManager;
        }
    }

    public void addFragmentLifecycle(Activity activity) {
        if (activity == null) {
            return;
        }

        if (!activityConcurrentHashMap.containsKey(activity.getClass().getName())) {
            LogUtils.d("start Activity[" + activity.getClass().getName() + "] monitor Fragment Lifecycle");
            activityConcurrentHashMap.put(activity.getClass().getName(), activity);
            if (activity instanceof FragmentActivity) {
                FragmentManager.FragmentLifecycleCallbacks callbacks = new FTFragmentLifecycleCallback(activity);
                fragmentLifecycleCall.put(activity.getClass().getName(), callbacks);
                ((FragmentActivity) activity).getSupportFragmentManager().registerFragmentLifecycleCallbacks(callbacks, true);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    android.app.FragmentManager.FragmentLifecycleCallbacks callbacks = new FTFragmentLifecycleCallbackDated(activity);
                    fragmentLifecycleCallDated.put(activity.getClass().getName(), callbacks);
                    activity.getFragmentManager().registerFragmentLifecycleCallbacks(callbacks, true);
                }
            }
        }
    }

    public void removeFragmentLifecycle(Activity activity) {
        if (activity == null) {
            return;
        }
        if (activityConcurrentHashMap.containsKey(activity.getClass().getName())) {
            LogUtils.d("stop Activity[" + activity.getClass().getName() + "] monitor Fragment Lifecycle");
            if (activity instanceof FragmentActivity) {
                FragmentManager.FragmentLifecycleCallbacks callbacks = fragmentLifecycleCall.get(activity.getClass().getName());
                if (callbacks != null) {
                    ((FragmentActivity) activity).getSupportFragmentManager().unregisterFragmentLifecycleCallbacks(callbacks);
                }
                fragmentLifecycleCall.remove(activity.getClass().getName());
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    android.app.FragmentManager.FragmentLifecycleCallbacks callbacks = fragmentLifecycleCallDated.get(activity.getClass().getName());
                    if (callbacks != null) {
                        activity.getFragmentManager().unregisterFragmentLifecycleCallbacks(callbacks);
                    }
                    fragmentLifecycleCallDated.remove(activity.getClass().getName());
                }
            }
            activityConcurrentHashMap.remove(activity.getClass().getName());
        }
    }
}
