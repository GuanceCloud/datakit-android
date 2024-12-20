package com.ft.sdk.garble.gesture;

import android.view.Window;

/**
 * Track all windows callback
 */
public class WindowCallbackTracker {

    /**
     *  stat to track window event
     * @param window
     */
    public void startTrack(Window window) {
        if (window == null) return;
        Window.Callback originCallBack = window.getCallback();
        window.setCallback(new WindowCallbackWrapper(window, originCallBack));
    }

    /**
     * stop to track window event
     * @param window
     */
    public void stopTrack(Window window) {
        if (window == null) return;
        Window.Callback current = window.getCallback();
        if (current instanceof WindowCallbackWrapper) {
            Window.Callback origin = ((WindowCallbackWrapper) current).getWrappedCallback();
            if (!(origin instanceof NoOpWindowCallback)) {
                window.setCallback(origin);
            } else {
                window.setCallback(null);
            }
        }


    }
}
