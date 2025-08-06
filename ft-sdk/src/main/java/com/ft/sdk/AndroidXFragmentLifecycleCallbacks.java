package com.ft.sdk;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.jetbrains.annotations.NotNull;

/**
 * Monitor {@link androidx.fragment.app.Fragment} view activities
 */
final class AndroidXFragmentLifecycleCallbacks extends FragmentManager.FragmentLifecycleCallbacks {

    final static String INVALID_FRAGMENT__REPORT_FRAGMENT = "androidx.lifecycle.ReportFragment";


    private FragmentLifecycleCallBack callBack;

    public void setFragmentLifecycleCallBack(FragmentLifecycleCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onFragmentPreAttached(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f, @NonNull @NotNull Context context) {
        super.onFragmentPreAttached(fm, f, context);
        if (fm.getClass().getName().equals(INVALID_FRAGMENT__REPORT_FRAGMENT)) return;
        if (callBack != null) {
            callBack.onFragmentPreAttached(new FragmentWrapper(f));
        }

    }

    @Override
    public void onFragmentCreated(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f, @Nullable Bundle savedInstanceState) {
        super.onFragmentCreated(fm, f, savedInstanceState);
        if (fm.getClass().getName().equals(INVALID_FRAGMENT__REPORT_FRAGMENT)) return;
        if (callBack != null) {
            callBack.onFragmentCreated(new FragmentWrapper(f));
        }
    }

    @Override
    public void onFragmentResumed(FragmentManager fm, Fragment f) {
        super.onFragmentResumed(fm, f);
        if (fm.getClass().getName().equals(INVALID_FRAGMENT__REPORT_FRAGMENT)) return;
        if (callBack != null) {
            callBack.onFragmentResumed(new FragmentWrapper(f));
        }
    }


    @Override
    public void onFragmentStopped(FragmentManager fm, Fragment f) {
        super.onFragmentStopped(fm, f);
        if (fm.getClass().getName().equals(INVALID_FRAGMENT__REPORT_FRAGMENT)) return;
        if (callBack != null) {
            callBack.onFragmentStopped(new FragmentWrapper(f));
        }
    }
}
