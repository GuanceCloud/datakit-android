package com.ft.sdk.sessionreplay.internal.recorder.callback;

import android.os.Build;
import android.view.MotionEvent;

public class MotionEventUtils {

    private MotionEventUtils(){

    }

    /**
     * For Android SDK < 29 we will have to know the target View on which this event happened
     * and then to compute the absolute as target.absoluteX,Y + event.getX,Y(pointerIndex)
     * This will not be handled now as it is too complex and not very optimised. For now we will
     * not support multi finger gestures in the player for version < 29.
     */
    public static float getPointerAbsoluteX(MotionEvent event, int pointerIndex) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return event.getRawX(pointerIndex);
        } else {
            return event.getRawX();
        }
    }

    public static float getPointerAbsoluteY(MotionEvent event, int pointerIndex) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return event.getRawY(pointerIndex);
        } else {
            return event.getRawY();
        }
    }
}
