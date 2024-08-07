package com.ft.sdk.sessionreplay.internal.wrappers;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.ft.sdk.sessionreplay.utils.InternalLogger;

public class CanvasWrapper {
    private static final String TAG = "CanvasWrapper";

    private final InternalLogger logger;

    public CanvasWrapper(InternalLogger logger) {
        this.logger = logger;
    }

    public Canvas createCanvas(Bitmap bitmap) throws RuntimeException {
        if (bitmap.isRecycled() || !bitmap.isMutable()) {
            logger.e(TAG, CanvasWrapper.INVALID_BITMAP);
            return null;
        }

        try {
            return new Canvas(bitmap);
        } catch (IllegalStateException e) {
            logger.e(TAG, CanvasWrapper.FAILED_TO_CREATE_CANVAS, e);
            return null;
        }
    }

    private static final String INVALID_BITMAP = "Cannot create canvas: bitmap is either already recycled or immutable";
    private static final String FAILED_TO_CREATE_CANVAS = "Failed to create canvas";
}
