package com.ft.sdk;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * 监听 {@link androidx.fragment.app.Fragment} 的 view 活动
 */
final class AndroidXFragmentLifecycleCallbacks extends FragmentManager.FragmentLifecycleCallbacks {

    @Override
    public void onFragmentResumed(FragmentManager fm, Fragment f) {
        super.onFragmentResumed(fm, f);
        FTRUMGlobalManager.get().startView(f.getClass().getSimpleName());
    }


    @Override
    public void onFragmentStopped(FragmentManager fm, Fragment f) {
        super.onFragmentStopped(fm, f);
        FTRUMGlobalManager.get().stopView();

    }
}
