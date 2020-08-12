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
    public final static String TAG = "FTFragmentLifecycleHandler";
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
        LogUtils.d(TAG,"Fragment[" + fragment.getSimpleName() + "] state Resume");
        show(fragment);
    }

    public void onFragmentPaused(Class fragment) {
        if (FTFragmentManager.getInstance().ignoreFragments.contains(fragment.getName())) {
            return;
        }
        if (fragmentUseVisibleHint.containsKey(fragment.getName())) {
            return;
        }
        LogUtils.d(TAG,"Fragment[" + fragment.getSimpleName() + "] state Pause");
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
        LogUtils.d(TAG,"Fragment[" + fragment.getSimpleName() + "] isVisible=" + isVisible);
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
     * Fragment 页面显示
     * Fragment（假如为 AF） 打开有四种方式，
     * 1、首次在 Activity（假如 AA） 打开，这时候它的 Parent 为当前 AA ,                                 Fragment栈清空
     * 2、当前 Activity 中有多个 Fragment，从其他 Fragment（假如为 BF）,这时候它的 Parent 为 AA.BF
     * 3、当前 Activity 未关闭后重启，这时候它的 Parent 为 AA                                           Fragment栈清空
     * 4、在当前Fragment（AF）中打开了 BA页面，然后BA页面关闭，回到 AF页面，这时候它的 Parent 为 BA           Fragment栈清空
     *
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
     * Fragment 页面隐藏或者关闭
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
