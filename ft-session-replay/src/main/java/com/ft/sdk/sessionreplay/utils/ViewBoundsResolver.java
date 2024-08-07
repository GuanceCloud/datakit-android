package com.ft.sdk.sessionreplay.utils;

import android.view.View;

public interface ViewBoundsResolver {

    /**
     * Resolves the View bounds in device space, and normalizes them based on the screen density.
     * Example: if a device has a DPI = 2, the value of the dimension or position is divided by
     * 2 to get a normalized value.
     * @param view the View
     * @param screenDensity the current device screen density
     * @return the computed view bounds
     */
    GlobalBounds resolveViewGlobalBounds(View view, float screenDensity);

    /**
     * Resolves the View bounds in device space excluding the padding, and normalizes them based on the screen density.
     * Example: if a device has a DPI = 2, the value of the padding is divided by
     * 2 to get a normalized value.
     * @param view the View
     * @param screenDensity the current device screen density
     * @return the computed view padding
     */
    GlobalBounds resolveViewPaddedBounds(View view, float screenDensity);
}
