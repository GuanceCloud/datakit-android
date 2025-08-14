package com.ft.sdk.sessionreplay.internal.recorder.callback;

import android.view.Window;

import java.util.List;

public interface OnWindowRefreshedCallback {

    void onWindowsAdded(List<Window> windows);

    void onWindowsRemoved(List<Window> windows);
}
