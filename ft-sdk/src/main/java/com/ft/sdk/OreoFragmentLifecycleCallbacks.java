package com.ft.sdk;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

/**
 * Listen to the view activity of {@link android.app.Fragment}
 */
@RequiresApi(api = Build.VERSION_CODES.O)
final class OreoFragmentLifecycleCallbacks extends FragmentManager.FragmentLifecycleCallbacks {

    private FragmentLifecycleCallBack callBack;

    public void setFragmentLifecycleCallBack(FragmentLifecycleCallBack callBack) {
        this.callBack = callBack;
    }


    @Override
    public void onFragmentPreAttached(FragmentManager fm, Fragment f, Context context) {
        super.onFragmentPreAttached(fm, f, context);
        if (callBack != null) {
            callBack.onFragmentPreAttached(new FragmentWrapper(f));
        }
    }

    @Override
    public void onFragmentCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
        super.onFragmentCreated(fm, f, savedInstanceState);
        if (callBack != null) {
            callBack.onFragmentCreated(new FragmentWrapper(f));
        }
    }

    @Override
    public void onFragmentResumed(FragmentManager fm, Fragment f) {
        super.onFragmentResumed(fm, f);
        if (callBack != null) {
            callBack.onFragmentResumed(new FragmentWrapper(f));
        }

    }


    @Override
    public void onFragmentStopped(FragmentManager fm, Fragment f) {
        super.onFragmentStopped(fm, f);
        if (callBack != null) {
            callBack.onFragmentStopped(new FragmentWrapper(f));
        }
    }
}
