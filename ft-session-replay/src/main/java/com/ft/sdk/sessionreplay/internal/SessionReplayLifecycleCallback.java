package com.ft.sdk.sessionreplay.internal;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.MainThread;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.ft.sdk.sessionreplay.internal.recorder.callback.OnWindowRefreshedCallback;
import com.ft.sdk.sessionreplay.internal.recorder.callback.RecorderFragmentLifecycleCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class SessionReplayLifecycleCallback implements LifecycleCallback, OnWindowRefreshedCallback {

    private final OnWindowRefreshedCallback onWindowRefreshedCallback;
    private final WeakHashMap<Window, Object> currentActiveWindows = new WeakHashMap<>();

    public SessionReplayLifecycleCallback(OnWindowRefreshedCallback onWindowRefreshedCallback) {
        this.onWindowRefreshedCallback = onWindowRefreshedCallback;
    }

    public void setCurrentWindow(Activity activity) {
        Window window = activity.getWindow();
        if (window != null) {
            currentActiveWindows.put(window, null);
        }
    }

    public void registerFragmentLifecycleCallbacks(Activity activity) {
        if (activity instanceof FragmentActivity) {
            // we need to register before the activity resumes to catch all the fragments
            // added even before the activity resumes
            RecorderFragmentLifecycleCallback lifecycleCallback = new RecorderFragmentLifecycleCallback(this);
            ((FragmentActivity) activity).getSupportFragmentManager().registerFragmentLifecycleCallbacks(
                    lifecycleCallback,
                    true
            );
        }
    }

    @Override
    public void onWindowsAdded(List<Window> windows) {
        for (Window window : windows) {
            currentActiveWindows.put(window, null);
        }
        onWindowRefreshedCallback.onWindowsAdded(windows);
    }

    @Override
    public void onWindowsRemoved(List<Window> windows) {
        for (Window window : windows) {
            currentActiveWindows.remove(window);
        }
        onWindowRefreshedCallback.onWindowsRemoved(windows);
    }

    @Override
    public List<Window> getCurrentWindows() {
        return new ArrayList<>(currentActiveWindows.keySet());
    }

    @MainThread
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (activity instanceof FragmentActivity) {
            // Register fragment lifecycle callbacks to capture fragments added before activity resumes
            RecorderFragmentLifecycleCallback lifecycleCallback = new RecorderFragmentLifecycleCallback(this);
            FragmentManager fragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
            fragmentManager.registerFragmentLifecycleCallbacks(lifecycleCallback, true);
        }
    }

    @MainThread
    @Override
    public void onActivityStarted(Activity activity) {
        // No-op
    }

    @MainThread
    @Override
    public void onActivityResumed(Activity activity) {
        Window window = activity.getWindow();
        if (window != null) {
            currentActiveWindows.put(window, null);
            onWindowRefreshedCallback.onWindowsAdded(List.of(window));
        }
    }

    @MainThread
    @Override
    public void onActivityPaused(Activity activity) {
        Window window = activity.getWindow();
        if (window != null) {
            currentActiveWindows.remove(window);
            onWindowRefreshedCallback.onWindowsRemoved(List.of(window));
        }
    }

    @MainThread
    @Override
    public void onActivityStopped(Activity activity) {
        // No-op
    }

    @MainThread
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // No-op
    }

    @MainThread
    @Override
    public void onActivityDestroyed(Activity activity) {
        // No-op
    }
}
