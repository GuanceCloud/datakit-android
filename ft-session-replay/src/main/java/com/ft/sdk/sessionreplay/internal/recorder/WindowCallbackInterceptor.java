package com.ft.sdk.sessionreplay.internal.recorder;

import static com.ft.sdk.sessionreplay.internal.recorder.callback.RecorderWindowCallback.FLUSH_BUFFER_THRESHOLD_NS;
import static com.ft.sdk.sessionreplay.internal.recorder.callback.RecorderWindowCallback.MOTION_UPDATE_DELAY_THRESHOLD_NS;

import android.content.Context;
import android.view.Window;

import androidx.annotation.NonNull;

import com.ft.sdk.sessionreplay.SessionReplayPrivacy;
import com.ft.sdk.sessionreplay.internal.async.RecordedDataQueueHandler;
import com.ft.sdk.sessionreplay.internal.recorder.callback.NoOpWindowCallback;
import com.ft.sdk.sessionreplay.internal.recorder.callback.RecorderWindowCallback;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.TimeProvider;

import java.util.List;
import java.util.WeakHashMap;

public class WindowCallbackInterceptor {

    private final RecordedDataQueueHandler recordedDataQueueHandler;
    private final ViewOnDrawInterceptor viewOnDrawInterceptor;
    private final TimeProvider timeProvider;
    private final InternalLogger internalLogger;
    private final SessionReplayPrivacy privacy;

    private final WeakHashMap<Window, Object> wrappedWindows = new WeakHashMap<>();

    public WindowCallbackInterceptor(@NonNull RecordedDataQueueHandler recordedDataQueueHandler,
                                     @NonNull ViewOnDrawInterceptor viewOnDrawInterceptor,
                                     @NonNull TimeProvider timeProvider,
                                     @NonNull InternalLogger internalLogger,
                                     @NonNull SessionReplayPrivacy privacy) {
        this.recordedDataQueueHandler = recordedDataQueueHandler;
        this.viewOnDrawInterceptor = viewOnDrawInterceptor;
        this.timeProvider = timeProvider;
        this.internalLogger = internalLogger;
        this.privacy = privacy;
    }

    public void intercept(@NonNull List<Window> windows, @NonNull Context appContext) {
        for (Window window : windows) {
            wrapWindowCallback(window, appContext);
            wrappedWindows.put(window, null);
        }
    }

    public void stopIntercepting(@NonNull List<Window> windows) {
        for (Window window : windows) {
            unwrapWindowCallback(window);
            wrappedWindows.remove(window);
        }
    }

    public void stopIntercepting() {
        for (Window window : wrappedWindows.keySet()) {
            unwrapWindowCallback(window);
        }
        wrappedWindows.clear();
    }

    private void wrapWindowCallback(@NonNull Window window, @NonNull Context appContext) {
        Window.Callback toWrap = window.getCallback();
        if (toWrap == null) {
            toWrap = new NoOpWindowCallback();
        }
        window.setCallback(new RecorderWindowCallback(
                appContext,
                recordedDataQueueHandler,
                toWrap,
                timeProvider,
                viewOnDrawInterceptor,
                internalLogger,
                privacy,
                MOTION_UPDATE_DELAY_THRESHOLD_NS,
                FLUSH_BUFFER_THRESHOLD_NS,
                null
        ));
    }

    private void unwrapWindowCallback(@NonNull Window window) {
        Window.Callback callback = window.getCallback();
        if (callback instanceof RecorderWindowCallback) {
            Window.Callback wrappedCallback = ((RecorderWindowCallback) callback).getWrappedCallback();
            if (wrappedCallback instanceof NoOpWindowCallback) {
                window.setCallback(null);
            } else {
                window.setCallback(wrappedCallback);
            }
        }
    }
}
