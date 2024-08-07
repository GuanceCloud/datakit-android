package com.ft.sdk.sessionreplay.internal.recorder.wrappers;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.DisplayMetrics;

import com.ft.sdk.sessionreplay.utils.InternalLogger;

public class BitmapWrapper {

    private static final String TAG = "BitmapWrapper";

    private final InternalLogger logger;

    public BitmapWrapper(InternalLogger logger) {
        this.logger = logger;
    }

    public Bitmap createBitmap(DisplayMetrics displayMetrics, int bitmapWidth, int bitmapHeight, Config config) {
        try {
            return Bitmap.createBitmap(displayMetrics, bitmapWidth, bitmapHeight, config);
        } catch (IllegalArgumentException e) {
            // should never happen since config is given as valid type and width/height are
            // normalized to be at least 1
            // TODO RUM-806 Add logs here once the sdkLogger is added
            logger.e(TAG, "Failed to create bitmap", e);
            return null;
        }
    }

    public Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter) {
        try {
            return Bitmap.createScaledBitmap(src, dstWidth, dstHeight, filter);
        } catch (IllegalArgumentException e) {
            // should never happen since config is given as valid type and width/height are
            // normalized to be at least 1
            // TODO RUM-806 Add logs here once the sdkLogger is added
            logger.e(TAG, "Failed to create scaled bitmap", e);
            return null;
        }
    }
}
