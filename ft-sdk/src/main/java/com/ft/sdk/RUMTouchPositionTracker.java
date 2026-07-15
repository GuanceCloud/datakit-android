package com.ft.sdk;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ft.sdk.garble.utils.Constants;

import java.lang.ref.WeakReference;
import java.util.HashMap;

final class RUMTouchPositionTracker {
    private static final long TOUCH_MATCH_TIMEOUT_MS = 1000L;
    private static volatile LastTouchEvent lastTouchEvent;

    private RUMTouchPositionTracker() {
    }

    static void record(MotionEvent motionEvent) {
        record(motionEvent, null);
    }

    static void record(MotionEvent motionEvent, View rootView) {
        if (motionEvent == null) {
            return;
        }
        int action = motionEvent.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
            float rawX = motionEvent.getRawX();
            float rawY = motionEvent.getRawY();
            lastTouchEvent = new LastTouchEvent(rawX, rawY, motionEvent.getEventTime(),
                    findTouchTarget(rootView, rawX, rawY));
        }
    }

    static View resolveLastTouchTarget() {
        LastTouchEvent touchEvent = getRecentTouchEvent();
        if (touchEvent == null || touchEvent.targetView == null) {
            return null;
        }
        View targetView = touchEvent.targetView.get();
        if (targetView == null
                || targetView.getVisibility() != View.VISIBLE
                || targetView.getWidth() <= 0
                || targetView.getHeight() <= 0) {
            return null;
        }
        return targetView;
    }

    static void appendPositionIfAvailable(HashMap<String, Object> properties,
                                          View targetView,
                                          MotionEvent motionEvent) {
        if (properties == null
                || properties.containsKey(Constants.KEY_RUM_ACTION_POSITION)
                || targetView == null
                || motionEvent != null) {
            return;
        }

        LastTouchEvent touchEvent = getRecentTouchEvent();
        if (touchEvent == null) {
            return;
        }

        int[] location = new int[2];
        targetView.getLocationOnScreen(location);
        float x = touchEvent.rawX - location[0];
        float y = touchEvent.rawY - location[1];
        if (x < 0 || y < 0 || x > targetView.getWidth() || y > targetView.getHeight()) {
            return;
        }

        properties.put(Constants.KEY_RUM_ACTION_POSITION,
                RUMHeatMapPropertyBuilder.buildActionPositionJson(x, y));
    }

    private static LastTouchEvent getRecentTouchEvent() {
        LastTouchEvent touchEvent = lastTouchEvent;
        if (touchEvent == null) {
            return null;
        }
        long elapsed = SystemClock.uptimeMillis() - touchEvent.eventTime;
        if (elapsed < 0 || elapsed > TOUCH_MATCH_TIMEOUT_MS) {
            return null;
        }
        return touchEvent;
    }

    private static View findTouchTarget(View view, float rawX, float rawY) {
        if (!contains(view, rawX, rawY)) {
            return null;
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
                View target = findTouchTarget(viewGroup.getChildAt(i), rawX, rawY);
                if (target != null) {
                    return target;
                }
            }
        }
        return view.isClickable() ? view : null;
    }

    private static boolean contains(View view, float rawX, float rawY) {
        if (view == null
                || view.getVisibility() != View.VISIBLE
                || view.getWidth() <= 0
                || view.getHeight() <= 0) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return rawX >= location[0]
                && rawY >= location[1]
                && rawX < location[0] + view.getWidth()
                && rawY < location[1] + view.getHeight();
    }

    private static class LastTouchEvent {
        final float rawX;
        final float rawY;
        final long eventTime;
        final WeakReference<View> targetView;

        LastTouchEvent(float rawX, float rawY, long eventTime, View targetView) {
            this.rawX = rawX;
            this.rawY = rawY;
            this.eventTime = eventTime;
            this.targetView = targetView == null ? null : new WeakReference<>(targetView);
        }
    }
}
