package com.ft.sdk.sessionreplay.utils;

import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class DrawableToColorMapperFactory {

    /**
     * Provides a default implementation.
     *
     * @return a default implementation based on the device API level
     */
    public static DrawableToColorMapper getDefault(List<DrawableToColorMapper> list) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new AndroidQDrawableToColorMapper(list);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new AndroidMDrawableToColorMapper(list);
        } else {
            return new LegacyDrawableToColorMapper(list);
        }
    }

    public static DrawableToColorMapper getDefault() {
        return getDefault(new ArrayList<>());
    }
}
