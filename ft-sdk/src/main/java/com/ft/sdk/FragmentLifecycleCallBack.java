package com.ft.sdk;

public interface FragmentLifecycleCallBack {
    void onFragmentPreAttached(FragmentWrapper wrapper);

    void onFragmentCreated(FragmentWrapper wrapper);

    void onFragmentResumed(FragmentWrapper wrapper);

    void onFragmentStopped(FragmentWrapper wrapper);
}