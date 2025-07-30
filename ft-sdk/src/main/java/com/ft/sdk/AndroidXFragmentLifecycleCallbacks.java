package com.ft.sdk;


import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Monitor {@link androidx.fragment.app.Fragment} view activities
 */
final class AndroidXFragmentLifecycleCallbacks extends FragmentManager.FragmentLifecycleCallbacks {

    final static String INVALID_FRAGMENT__REPORT_FRAGMENT = "androidx.lifecycle.ReportFragment";

    // Map to record Fragment pre-attached start time
    private final Map<Fragment, Long> fragmentPreAttachedTimeMap = new HashMap<>();

    @Override
    public void onFragmentPreAttached(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f, @NonNull @NotNull Context context) {
        super.onFragmentPreAttached(fm, f, context);
        if (fm.getClass().getName().equals(INVALID_FRAGMENT__REPORT_FRAGMENT)) return;
        // Record Fragment pre-attached start time with nanosecond precision
        fragmentPreAttachedTimeMap.put(f, SystemClock.elapsedRealtimeNanos());
    }

    @Override
    public void onFragmentCreated(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f, @Nullable Bundle savedInstanceState) {
        super.onFragmentCreated(fm, f, savedInstanceState);
        if (fm.getClass().getName().equals(INVALID_FRAGMENT__REPORT_FRAGMENT)) return;

        // Calculate Fragment pre-attached to pre-created duration
        Long preAttachedStartTime = fragmentPreAttachedTimeMap.remove(f);
        if (preAttachedStartTime != null) {
            long createDuration = SystemClock.elapsedRealtimeNanos() - preAttachedStartTime;
            FTRUMGlobalManager.get().onCreateView(f.getClass().getSimpleName(), createDuration);
        }
    }

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
