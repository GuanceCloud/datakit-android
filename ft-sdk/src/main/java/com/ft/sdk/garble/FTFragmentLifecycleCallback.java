package com.ft.sdk.garble;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 监听系统 Fragment 的生命周期回调
 */
public class FTFragmentLifecycleCallback extends FragmentManager.FragmentLifecycleCallbacks {

    Activity activity;
    ConcurrentLinkedQueue<Fragment> fragmentQueue;

    FTFragmentLifecycleCallback(Activity activity) {
        this.activity = activity;
        fragmentQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void onFragmentActivityCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        if(FTFragmentManager.getInstance().ignoreFragments.contains(f.getClass().getName())){
            return;
        }
        if(!f.getUserVisibleHint()){
            return;
        }
        /**LogUtils.d("Fragment["+f.getClass().getSimpleName()+"] state Resume");
        Fragment fragment = fragmentQueue.poll();
        String parent = Constants.FLOW_ROOT;
        if(fragment != null){
            parent = fragment.getClass().getSimpleName();
        }
        FTAutoTrack.startPage(f.getClass(), activity,parent);
        fragmentQueue.add(f);*/
        FTFragmentLifecycleHandler.getInstance().fragmentShow(f.getClass(),activity.getClass());
    }

    @Override
    public void onFragmentPaused(@NonNull FragmentManager fm, @NonNull Fragment f) {
        if(FTFragmentManager.getInstance().ignoreFragments.contains(f.getClass().getName())){
            return;
        }
        if(!f.getUserVisibleHint()){
            return;
        }
        //LogUtils.d("Fragment["+f.getClass().getSimpleName()+"] state Pause");
        //FTAutoTrack.destroyPage(f.getClass(), activity,Constants.FLOW_ROOT);
        FTFragmentLifecycleHandler.getInstance().fragmentHidden(f.getClass(),activity.getClass());
    }

    @Override
    public void onFragmentStopped(@NonNull FragmentManager fm, @NonNull Fragment f) {
    }

    @Override
    public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {

    }

    /**
     * 设置 Fragment 是否可见
     * @param fragment
     * @param isVisible
     */
    public void setUserVisibleHint(Class fragment,boolean isVisible){
        if(isVisible){
            FTFragmentLifecycleHandler.getInstance().fragmentShow(fragment,activity.getClass());
        }else{
            FTFragmentLifecycleHandler.getInstance().fragmentHidden(fragment,activity.getClass());
        }
    }
}
