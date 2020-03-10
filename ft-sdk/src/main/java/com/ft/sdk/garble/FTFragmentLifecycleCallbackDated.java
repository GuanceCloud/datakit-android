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

@SuppressWarnings("deprecation")
@RequiresApi(api = Build.VERSION_CODES.O)
public class FTFragmentLifecycleCallbackDated extends FragmentManager.FragmentLifecycleCallbacks {
    Activity activity;
    ConcurrentLinkedQueue<Fragment> fragmentQueue;
    FTFragmentLifecycleCallbackDated(Activity activity){
        this.activity = activity;
        fragmentQueue = new ConcurrentLinkedQueue<>();
    }
    @Override
    public void onFragmentActivityCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
    }

    @Override
    public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        if(FTFragmentManager.getInstance().ignoreFragments.contains(f.getClass().getName())){
            return;
        }
        LogUtils.d("Fragment["+f.getClass().getSimpleName()+"] Dated state Resume");
        Fragment fragment = fragmentQueue.poll();
        String parent = Constants.FLOW_ROOT;
        if(fragment != null){
            parent = fragment.getClass().getSimpleName();
        }
        FTAutoTrack.startPage(f.getClass(), activity,parent);
        fragmentQueue.add(f);
    }

    @Override
    public void onFragmentPaused(@NonNull FragmentManager fm, @NonNull Fragment f) {
        if(FTFragmentManager.getInstance().ignoreFragments.contains(f.getClass().getName())){
            return;
        }
        LogUtils.d("Fragment["+f.getClass().getSimpleName()+"] Dated state Pause");
        FTAutoTrack.destroyPage(f.getClass(), activity,Constants.FLOW_ROOT);
    }

    @Override
    public void onFragmentStopped(@NonNull FragmentManager fm, @NonNull Fragment f) {
    }

    @Override
    public void onFragmentDestroyed(FragmentManager fm, Fragment f) {

    }
}
