package com.ft.sdk;


import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

/**
 * Listen to the view activity of {@link android.app.Fragment}
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
