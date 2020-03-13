package com.ft.sdk.garble;


import android.app.Activity;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * create: by huangDianHua
 * time: 2020/3/13 09:33:49
 * description: Fragment 生命周期处理类
 */
public class FTFragmentLifecycleHandler {
    Activity activity;
    List<Class> fragmentList;
    ConcurrentHashMap<String, Boolean> fragmentUseVisibleHint;

    FTFragmentLifecycleHandler(Activity activity) {
        this.activity = activity;
        fragmentList = new ArrayList<>();
        fragmentUseVisibleHint = new ConcurrentHashMap<>();
    }

    public void onFragmentActivityCreated(Class fragment) {
    }

    public void onFragmentResumed(Class fragment) {
        if (FTFragmentManager.getInstance().ignoreFragments.contains(fragment.getName())) {
            return;
        }
        if (fragmentUseVisibleHint.containsKey(fragment.getName())) {
            return;
        }
        LogUtils.d("Fragment[" + fragment.getSimpleName() + "] state Resume");
        show(fragment);
    }

    public void onFragmentPaused(Class fragment) {
        if (FTFragmentManager.getInstance().ignoreFragments.contains(fragment.getName())) {
            return;
        }
        if (fragmentUseVisibleHint.containsKey(fragment.getName())) {
            return;
        }
        LogUtils.d("Fragment[" + fragment.getSimpleName() + "] state Pause");
        hidden(fragment);
    }

    public void onFragmentStopped(Class fragment) {
    }

    public void onFragmentDestroyed(Class fragment) {
        fragmentUseVisibleHint.remove(fragment.getName());
    }

    /**
     * 设置 Fragment 是否可见
     *
     * @param fragment
     * @param isVisible
     */
    public void setUserVisibleHint(Class fragment, boolean isVisible) {
        LogUtils.d("Fragment[" + fragment.getSimpleName() + "] isVisible=" + isVisible);
        if (!fragmentUseVisibleHint.containsKey(fragment.getName())) {
            fragmentUseVisibleHint.put(fragment.getName(), true);
        }
        if (isVisible) {
            show(fragment);
        } else {
            hidden(fragment);
        }
    }

    /**
     * @param fragment
     */
    private void show(Class fragment) {
        Class clazz = getLastFragment();
        String parent = Constants.FLOW_ROOT;
        if (clazz != null) {
            parent = clazz.getSimpleName();
        }
        FTAutoTrack.startPage(fragment, activity, parent);
        fragmentList.add(fragment);
    }

    /**
     * @param fragment
     */
    private void hidden(Class fragment) {
        FTAutoTrack.destroyPage(fragment, activity, Constants.FLOW_ROOT);
    }

    /**
     * 得到父级 Fragment
     *
     * @return
     */
    Class getLastFragment() {
        if (fragmentList.isEmpty()) {
            return null;
        } else {
            return fragmentList.get(fragmentList.size() - 1);
        }
    }
}
