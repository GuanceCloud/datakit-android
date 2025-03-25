package com.ft.sdk.sessionreplay.internal.recorder;

import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.internal.TouchPrivacyManager;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.util.List;
import java.util.WeakHashMap;

public class ViewOnDrawInterceptor {

    private static final String TAG = "ViewOnDrawInterceptor";
    private final InternalLogger internalLogger;
    private final OnDrawListenerProducer onDrawListenerProducer;
    private final TouchPrivacyManager touchPrivacyManager;

    private final WeakHashMap<View, ViewTreeObserver.OnDrawListener> decorOnDrawListeners = new WeakHashMap<>();

    public ViewOnDrawInterceptor(@NonNull InternalLogger internalLogger,
                                 @NonNull OnDrawListenerProducer onDrawListenerProducer,
                                 @NonNull TouchPrivacyManager touchPrivacyManager
    ) {
        this.internalLogger = internalLogger;
        this.onDrawListenerProducer = onDrawListenerProducer;
        this.touchPrivacyManager = touchPrivacyManager;
    }

    public void intercept(@NonNull List<View> decorViews, TextAndInputPrivacy textAndInputPrivacy,
                          ImagePrivacy imagePrivacy) {
        stopInterceptingAndRemove(decorViews);
        ViewTreeObserver.OnDrawListener onDrawListener = onDrawListenerProducer.create(decorViews,
                textAndInputPrivacy, imagePrivacy, touchPrivacyManager);
        for (View decorView : decorViews) {
            ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
            if (viewTreeObserver != null && viewTreeObserver.isAlive()) {
                try {
                    viewTreeObserver.addOnDrawListener(onDrawListener);
                    decorOnDrawListeners.put(decorView, onDrawListener);
                } catch (IllegalStateException e) {
                    internalLogger.w(TAG, "Unable to add onDrawListener onto viewTreeObserver", e);
                }
            }
        }

        // Force onDraw here to ensure at least one snapshot is taken if the window changes quickly
        onDrawListener.onDraw();
    }

    public void stopIntercepting(@NonNull List<View> decorViews) {
        stopInterceptingAndRemove(decorViews);
    }

    public void stopIntercepting() {
        for (WeakHashMap.Entry<View, ViewTreeObserver.OnDrawListener> entry : decorOnDrawListeners.entrySet()) {
            stopInterceptingSafe(entry.getKey(), entry.getValue());
        }
        decorOnDrawListeners.clear();
    }

    private void stopInterceptingAndRemove(@NonNull List<View> decorViews) {
        for (View decorView : decorViews) {
            ViewTreeObserver.OnDrawListener listener = decorOnDrawListeners.remove(decorView);
            if (listener != null) {
                stopInterceptingSafe(decorView, listener);
            }
        }
    }

    private void stopInterceptingSafe(@NonNull View decorView, @NonNull ViewTreeObserver.OnDrawListener listener) {
        ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            try {
                viewTreeObserver.removeOnDrawListener(listener);
            } catch (IllegalStateException e) {
                internalLogger.w(TAG, "Unable to remove onDrawListener from viewTreeObserver", e);
            }
        }
    }
}
