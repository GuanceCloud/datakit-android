package com.ft.sdk;


import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

/**
 * * 监听 {@link android.app.Fragment} 的 view 活动
 */
@RequiresApi(api = Build.VERSION_CODES.O)
final class OreoFragmentLifecycleCallbacks extends FragmentManager.FragmentLifecycleCallbacks {

    @Override
    public void onFragmentResumed(FragmentManager fm, Fragment f) {
        super.onFragmentResumed(fm, f);
        FTRUMGlobalManager.get().startView(fm.getClass().getSimpleName());
    }


    @Override
    public void onFragmentStopped(FragmentManager fm, Fragment f) {
        super.onFragmentStopped(fm, f);
        FTRUMGlobalManager.get().stopView();
    }
}
