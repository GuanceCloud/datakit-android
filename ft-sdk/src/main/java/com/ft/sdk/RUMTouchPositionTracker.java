package com.ft.sdk;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import com.ft.sdk.garble.utils.Constants;

import java.util.HashMap;

final class RUMTouchPositionTracker {
    private static final long TOUCH_MATCH_TIMEOUT_MS = 1000L;
    private static volatile LastTouchEvent lastTouchEvent;

    private RUMTouchPositionTracker() {
    }

    static void record(MotionEvent motionEvent) {
        if (motionEvent == null) {
            return;
        }
        int action = motionEvent.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
            lastTouchEvent = new LastTouchEvent(motionEvent.getRawX(), motionEvent.getRawY(),
                    motionEvent.getEventTime());
        }
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

        LastTouchEvent touchEvent = lastTouchEvent;
        if (touchEvent == null
                || SystemClock.uptimeMillis() - touchEvent.eventTime > TOUCH_MATCH_TIMEOUT_MS) {
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

    private static class LastTouchEvent {
        final float rawX;
        final float rawY;
        final long eventTime;

        LastTouchEvent(float rawX, float rawY, long eventTime) {
            this.rawX = rawX;
            this.rawY = rawY;
            this.eventTime = eventTime;
        }
    }
}
