package com.ft.sdk;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

/**
 * Listen to the view activity of {@link android.app.Fragment}
 */
@RequiresApi(api = Build.VERSION_CODES.O)
final class OreoFragmentLifecycleCallbacks extends FragmentManager.FragmentLifecycleCallbacks {

    // Map to record Fragment pre-attached start time
    private final Map<Fragment, Long> fragmentPreAttachedTimeMap = new HashMap<>();

    @Override
    public void onFragmentPreAttached(FragmentManager fm, Fragment f, Context context) {
        super.onFragmentPreAttached(fm, f, context);
        fragmentPreAttachedTimeMap.put(f, SystemClock.elapsedRealtimeNanos());
    }

    @Override
    public void onFragmentCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
        super.onFragmentCreated(fm, f, savedInstanceState);
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
        FTRUMGlobalManager.get().startView(f.getClass().getSimpleName());
    }


    @Override
    public void onFragmentStopped(FragmentManager fm, Fragment f) {
        super.onFragmentStopped(fm, f);
        FTRUMGlobalManager.get().stopView();
    }
}
