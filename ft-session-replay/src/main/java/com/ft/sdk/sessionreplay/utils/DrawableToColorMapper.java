package com.ft.sdk.sessionreplay.utils;

import android.graphics.drawable.Drawable;
import android.os.Build;

public interface DrawableToColorMapper {

    /**
     * Maps the drawable to its meaningful color, or null if the drawable is mostly invisible.
     *
     * @param drawable       the drawable to convert
     * @param internalLogger the internalLogger to report warnings
     * @return the color as an Integer (in 0xAARRGGBB order), or null if the drawable is mostly invisible
     */
    Integer mapDrawableToColor(Drawable drawable, InternalLogger internalLogger);

    /**
     * Provides a default implementation.
     *
     * @return a default implementation based on the device API level
     */
    static DrawableToColorMapper getDefault() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new AndroidQDrawableToColorMapper();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new AndroidMDrawableToColorMapper();
        } else {
            return new LegacyDrawableToColorMapper();
        }
    }
}
