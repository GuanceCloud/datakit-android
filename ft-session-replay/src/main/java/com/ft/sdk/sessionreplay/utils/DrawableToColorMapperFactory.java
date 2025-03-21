package com.ft.sdk.sessionreplay.utils;

import android.os.Build;

public class DrawableToColorMapperFactory {

    /**
     * Provides a default implementation.
     *
     * @return a default implementation based on the device API level
     */
    //fixme
    public static DrawableToColorMapper getDefault() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new AndroidQDrawableToColorMapper();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new AndroidMDrawableToColorMapper();
        } else {
            return new LegacyDrawableToColorMapper();
        }
    }
}
