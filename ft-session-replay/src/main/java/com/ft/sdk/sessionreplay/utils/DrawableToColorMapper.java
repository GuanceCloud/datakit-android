package com.ft.sdk.sessionreplay.utils;

import android.graphics.drawable.Drawable;

public interface DrawableToColorMapper {

    /**
     * Maps the drawable to its meaningful color, or null if the drawable is mostly invisible.
     *
     * @param drawable       the drawable to convert
     * @param internalLogger the internalLogger to report warnings
     * @return the color as an Integer (in 0xAARRGGBB order), or null if the drawable is mostly invisible
     */
    Integer mapDrawableToColor(Drawable drawable, InternalLogger internalLogger);

}
