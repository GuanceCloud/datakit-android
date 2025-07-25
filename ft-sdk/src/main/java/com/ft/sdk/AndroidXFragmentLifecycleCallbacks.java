package com.ft.sdk;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * Monitor {@link androidx.fragment.app.Fragment} view activities
 */
final class AndroidXFragmentLifecycleCallbacks extends FragmentManager.FragmentLifecycleCallbacks {

    final static String INVALID_FRAGMENT__REPORT_FRAGMENT = "androidx.lifecycle.ReportFragment";

    @Override
    public void onFragmentResumed(FragmentManager fm, Fragment f) {
        super.onFragmentResumed(fm, f);
        if (fm.getClass().getName().equals(INVALID_FRAGMENT__REPORT_FRAGMENT)) return;
        FTRUMGlobalManager.get().startView(f.getClass().getSimpleName());
    }


    @Override
    public void onFragmentStopped(FragmentManager fm, Fragment f) {
        super.onFragmentStopped(fm, f);
        if (fm.getClass().getName().equals(INVALID_FRAGMENT__REPORT_FRAGMENT)) return;
        FTRUMGlobalManager.get().stopView();

    }
}
