package com.ft.sdk;

import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;

final class RUMHeatMapActionPropertyBuilder {
    static final String EXTRA_MOTION_EVENT = "motionEvent";

    private RUMHeatMapActionPropertyBuilder() {
    }

    static HashMap<String, Object> build(Object object, HashMap<String, Object> extra) {
        View targetView = RUMActionTargetResolver.resolve(object, extra);
        MotionEvent motionEvent = null;
        if (extra != null && extra.get(EXTRA_MOTION_EVENT) instanceof MotionEvent) {
            motionEvent = (MotionEvent) extra.get(EXTRA_MOTION_EVENT);
        }
        HashMap<String, Object> properties =
                RUMHeatMapPropertyBuilder.buildActionProperties(targetView, motionEvent);
        RUMTouchPositionTracker.appendPositionIfAvailable(properties, targetView, motionEvent);
        return properties;
    }
}
