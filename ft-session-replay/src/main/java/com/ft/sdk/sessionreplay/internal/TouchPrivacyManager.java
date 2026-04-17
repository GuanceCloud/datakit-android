package com.ft.sdk.sessionreplay.internal;

import android.graphics.Point;
import android.graphics.Rect;

import androidx.annotation.UiThread;
import androidx.annotation.VisibleForTesting;

import com.ft.sdk.sessionreplay.TouchPrivacy;

import java.util.HashMap;
import java.util.Map;

public class TouchPrivacyManager {
    private final TouchPrivacy globalTouchPrivacy;

    // areas on screen where overrides are applied
    private final Map<Rect, TouchPrivacy> currentOverrideAreas = new HashMap<>();

    // Built during the view traversal and copied to currentOverrideAreas at the end
    // We use two hashmaps because touch handling happens in parallel to the view traversal
    // and we don't know which will happen first.
    // Secondly, because we don't want to have to keep track of the lifecycle of the overridden views in order to remove
    // the overrides when they are no longer needed.
    private final Map<Rect, TouchPrivacy> nextOverrideAreas = new HashMap<>();

    public TouchPrivacyManager(TouchPrivacy globalTouchPrivacy) {
        this.globalTouchPrivacy = globalTouchPrivacy;
    }

    /**
     * Adds touch area with {@link TouchPrivacy} override.
     */
    @UiThread
    public void addTouchOverrideArea(Rect bounds, TouchPrivacy touchPrivacy) {
        nextOverrideAreas.put(bounds, touchPrivacy);
    }

    @UiThread
    public void updateCurrentTouchOverrideAreas() {
        currentOverrideAreas.clear();
        currentOverrideAreas.putAll(nextOverrideAreas);
        nextOverrideAreas.clear();
    }

    @UiThread
    public boolean shouldRecordTouch(Point touchLocation) {
        boolean isOverriddenToShowTouch = false;

        for (Map.Entry<Rect, TouchPrivacy> entry : currentOverrideAreas.entrySet()) {
            Rect area = entry.getKey();
            TouchPrivacy overrideValue = entry.getValue();

            if (area.contains(touchLocation.x, touchLocation.y)) {
                if (overrideValue == TouchPrivacy.HIDE) {
                    return false;
                } else if (overrideValue == TouchPrivacy.SHOW) {
                    isOverriddenToShowTouch = true;
                }
            }
        }

        return isOverriddenToShowTouch || globalTouchPrivacy == TouchPrivacy.SHOW;
    }

    @VisibleForTesting
    Map<Rect, TouchPrivacy> getCurrentOverrideAreas() {
        return currentOverrideAreas;
    }

    @VisibleForTesting
    Map<Rect, TouchPrivacy> getNextOverrideAreas() {
        return nextOverrideAreas;
    }
}