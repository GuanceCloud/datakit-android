//
//  * https://github.com/firebase/firebase-android-sdk/blob/master/firebase-perf/src/main/java/com/google/firebase/perf/util/FirstDrawDoneListener.java
// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
//
// You may obtain a copy of the License at
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.ft.sdk.garble.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.concurrent.atomic.AtomicReference;

/**
 * OnDrawListener that unregisters itself and invokes callback when the next draw is done. This API
 * 16+ implementation is an approximation of the initial-display-time defined by Android Vitals.
 */
public class FirstDrawDoneListener implements ViewTreeObserver.OnDrawListener {
    // TODO(b/258263016): Migrate to go/firebase-android-executors
    @SuppressLint("ThreadPoolCreation")
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private final AtomicReference<View> viewReference;
    private final Runnable callback;

    /**
     * Registers a post-draw callback for the next draw of a view.
     */
    public static void registerForNextDraw(View view, Runnable drawDoneCallback) {
        registerForNextDrawCancellable(view, drawDoneCallback);
    }

    /** Registers a candidate whose pending listener can be released when its Activity leaves. */
    public static FirstDrawDoneListener registerForNextDrawCancellable(
            View view, Runnable drawDoneCallback) {
        final FirstDrawDoneListener listener = new FirstDrawDoneListener(view, drawDoneCallback);
        // Prior to API 26 the floating ViewTreeObserver is not merged during attachment.
        if (Build.VERSION.SDK_INT < 26 && !isAliveAndAttached(view)) {
            listener.attachListener = new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View attachedView) {
                    if (listener.viewReference.get() != null) {
                        attachedView.getViewTreeObserver().addOnDrawListener(listener);
                    }
                    attachedView.removeOnAttachStateChangeListener(this);
                    listener.attachListener = null;
                }

                @Override
                public void onViewDetachedFromWindow(View detachedView) {
                    detachedView.removeOnAttachStateChangeListener(this);
                    listener.attachListener = null;
                }
            };
            view.addOnAttachStateChangeListener(listener.attachListener);
        } else {
            view.getViewTreeObserver().addOnDrawListener(listener);
        }
        return listener;
    }

    private View.OnAttachStateChangeListener attachListener;

    /**
     * Releases an undrawn candidate on the main thread. An already observed draw still delivers
     * its queued callback; the startup owner decides which observed frame completes the measurement.
     */
    public void cancel() {
        View view = viewReference.getAndSet(null);
        if (view != null) {
            removeListeners(view);
        }
    }

    private void removeListeners(View view) {
        if (attachListener != null) {
            view.removeOnAttachStateChangeListener(attachListener);
            attachListener = null;
        }
        ViewTreeObserver observer = view.getViewTreeObserver();
        if (observer.isAlive()) {
            observer.removeOnDrawListener(this);
        }
    }

    private FirstDrawDoneListener(View view, Runnable callback) {
        this.viewReference = new AtomicReference<>(view);
        this.callback = callback;
    }

    @Override
    public void onDraw() {
        // Set viewReference to null so any onDraw past the first is a no-op
        View view = viewReference.getAndSet(null);
        if (view == null) {
            return;
        }
        // Removing an OnDrawListener inside onDraw is forbidden. Queue removal outside traversal.
        mainThreadHandler.post(() -> removeListeners(view));
        mainThreadHandler.postAtFrontOfQueue(callback);
    }

    /**
     * Helper to avoid <a
     * href="https://android.googlesource.com/platform/frameworks/base/+/9f8ec54244a5e0343b9748db3329733f259604f3">bug
     * prior to API 26</a>, where the floating ViewTreeObserver's OnDrawListeners are not merged into
     * the real ViewTreeObserver during attach.
     *
     * @return true if the View is already attached and the ViewTreeObserver is not a floating
     * placeholder.
     */
    private static boolean isAliveAndAttached(View view) {
        return view.getViewTreeObserver().isAlive() && isAttachedToWindow(view);
    }

    /**
     * Backport {@link View#isAttachedToWindow()} which is API 19+ only.
     */
    private static boolean isAttachedToWindow(View view) {
        if (Build.VERSION.SDK_INT >= 19) {
            return view.isAttachedToWindow();
        }
        return view.getWindowToken() != null;
    }
}