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
 * 全局 Fragment 管理类
 */
public class FTFragmentManager {
    public final static String TAG = "FTFragmentManager";
    private static FTFragmentManager mFragmentManager;
    /**
     * 该map在{@link #removeFragmentLifecycle} 方法中回收
     */
    private ConcurrentHashMap<String, FTFragmentLifecycleCallback> fragmentLifecycleCall = new ConcurrentHashMap<>();
    /**
     * 该map在{@link #removeFragmentLifecycle} 方法中回收
     */
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

    /**
     * 添加 Activity 的Fragment 的生命周期监听
     *
     * @param activity
     */
    public void addFragmentLifecycle(Activity activity) {
        if (activity == null) {
            return;
        }

        if (!activityConcurrentHashMap.containsKey(activity.getClass().getName())) {
            LogUtils.d(TAG,"start Activity[" + activity.getClass().getName() + "] monitor Fragment Lifecycle");
            activityConcurrentHashMap.put(activity.getClass().getName(), activity);
            if (activity instanceof FragmentActivity) {
                FTFragmentLifecycleCallback callbacks = new FTFragmentLifecycleCallback(activity);
                fragmentLifecycleCall.put(activity.getClass().getName(), callbacks);
                ((FragmentActivity) activity).getSupportFragmentManager().registerFragmentLifecycleCallbacks(callbacks, true);
            }
        }
    }

    /**
     * 移除 Activity 的Fragment 的生命周期监听
     *
     * @param activity
     */
    public void removeFragmentLifecycle(Activity activity) {
        if (activity == null) {
            return;
        }
        if (activityConcurrentHashMap.containsKey(activity.getClass().getName())) {
            LogUtils.d(TAG,"stop Activity[" + activity.getClass().getName() + "] monitor Fragment Lifecycle");
            if (activity instanceof FragmentActivity) {
                FragmentManager.FragmentLifecycleCallbacks callbacks = fragmentLifecycleCall.get(activity.getClass().getName());
                if (callbacks != null) {
                    ((FragmentActivity) activity).getSupportFragmentManager().unregisterFragmentLifecycleCallbacks(callbacks);
                }
                fragmentLifecycleCall.remove(activity.getClass().getName());
            }
            activityConcurrentHashMap.remove(activity.getClass().getName());
        }
    }

    /**
     * 返回Activity 中的Fragment 的类
     *
     * @param classActivity
     * @return
     */
    public Class getLastFragmentName(String classActivity) {
        try {
            if (fragmentLifecycleCall.containsKey(classActivity)) {
                FTFragmentLifecycleCallback ft = fragmentLifecycleCall.get(classActivity);
                return ft.fragmentLifecycleHandler.getLastFragment();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置 Fragment 的显示状态
     *
     * @param classActivity
     * @return
     */
    public void setFragmentVisible(String classActivity, Class fragment, boolean isVisible) {
        try {
            if (fragmentLifecycleCall.containsKey(classActivity)) {
                FTFragmentLifecycleCallback ft = fragmentLifecycleCall.get(classActivity);
                if (ft != null) {
                    ft.setUserVisibleHint(fragment, isVisible);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除 Fragment 栈
     *
     * @param classActivity
     * @return
     */
    public void clearFragmentList(String classActivity) {
        try {
            if (fragmentLifecycleCall.containsKey(classActivity)) {
                FTFragmentLifecycleCallback ft = fragmentLifecycleCall.get(classActivity);
                if (ft != null) {
                    ft.fragmentLifecycleHandler.fragmentList.clear();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
