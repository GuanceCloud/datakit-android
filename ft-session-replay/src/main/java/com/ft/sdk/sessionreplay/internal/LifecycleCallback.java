package com.ft.sdk.sessionreplay.internal;

import android.app.Application;
import android.view.Window;

import java.util.List;

public interface LifecycleCallback extends Application.ActivityLifecycleCallbacks {

    List<Window> getCurrentWindows();

}
