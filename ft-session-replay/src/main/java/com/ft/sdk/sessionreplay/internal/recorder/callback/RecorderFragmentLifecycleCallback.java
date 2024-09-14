package com.ft.sdk.sessionreplay.internal.recorder.callback;

import android.app.Activity;
import android.view.Window;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.List;

public class RecorderFragmentLifecycleCallback extends FragmentManager.FragmentLifecycleCallbacks {

    private final OnWindowRefreshedCallback onWindowRefreshedCallback;

    public RecorderFragmentLifecycleCallback(OnWindowRefreshedCallback onWindowRefreshedCallback) {
        this.onWindowRefreshedCallback = onWindowRefreshedCallback;
    }

    @Override
    public void onFragmentResumed(FragmentManager fm, Fragment f) {
        super.onFragmentResumed(fm, f);
        List<Window> windowsToRecord = getWindowsToRecord(f);
        if (windowsToRecord != null) {
            onWindowRefreshedCallback.onWindowsAdded(windowsToRecord);
        }
    }

    @Override
    public void onFragmentPaused(FragmentManager fm, Fragment f) {
        super.onFragmentPaused(fm, f);
        List<Window> windowsToRecord = getWindowsToRecord(f);
        if (windowsToRecord != null) {
            onWindowRefreshedCallback.onWindowsRemoved(windowsToRecord);
        }
    }

    private List<Window> getWindowsToRecord(Fragment f) {
        if (f instanceof DialogFragment && f.getContext() != null) {
            return getWindowsToRecord((DialogFragment)f);
        }
        return null;
    }

    private List<Window> getWindowsToRecord(DialogFragment f) {
        Window dialogWindow = f.getDialog().getWindow();
        Activity dialogOwnerActivity = f.getDialog().getOwnerActivity();
        Window ownerActivityWindow = dialogOwnerActivity.getWindow();
        if (dialogWindow == null || dialogOwnerActivity == null || ownerActivityWindow == null) {
            return null;
        }
        if (dialogWindow != ownerActivityWindow) {
            return List.of(dialogWindow); // Java 9+ way to create a List
        } else {
            return null;
        }
    }
}
