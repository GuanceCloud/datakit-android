package com.ft.sdk.sessionreplay.internal.wrappers;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.DisplayMetrics;

import com.ft.sdk.sessionreplay.utils.InternalLogger;

public class BitmapWrapper {

    private final InternalLogger logger;

    public BitmapWrapper(InternalLogger logger) {
        this.logger = logger;
    }

    public Bitmap createBitmap(DisplayMetrics displayMetrics, int bitmapWidth, int bitmapHeight, Config config) {
        try {
            return Bitmap.createBitmap(displayMetrics, bitmapWidth, bitmapHeight, config);
        } catch (IllegalArgumentException e) {
            //fixme
//            logger.log(InternalLogger.Level.ERROR, InternalLogger.Target.MAINTAINER,
//                    BitmapWrapper.FAILED_TO_CREATE_BITMAP, e);
            return null;
        }
    }

    public Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter) {
        try {
            return Bitmap.createScaledBitmap(src, dstWidth, dstHeight, filter);
        } catch (IllegalArgumentException e) {
            //fixme
//            logger.log(InternalLogger.Level.ERROR, InternalLogger.Target.MAINTAINER,
//                    BitmapWrapper.FAILED_TO_CREATE_SCALED_BITMAP, e);
            return null;
        }
    }

    private static final String FAILED_TO_CREATE_BITMAP = "Failed to create bitmap";
    private static final String FAILED_TO_CREATE_SCALED_BITMAP = "Failed to create scaled bitmap";
}
