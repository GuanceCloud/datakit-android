package com.ft.sdk.sessionreplay.internal.recorder.wrappers;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.ft.sdk.sessionreplay.utils.InternalLogger;

public class CanvasWrapper {

    private static final String TAG = "CanvasWrapper";
    private final InternalLogger logger;

    public CanvasWrapper(InternalLogger logger) {
        this.logger = logger;
    }

    public Canvas createCanvas(Bitmap bitmap) {
        if (bitmap.isRecycled() || !bitmap.isMutable()) {
            logger.e(TAG, "Cannot create canvas: bitmap is either already recycled or immutable");
            return null;
        }

        try {
            return new Canvas(bitmap);
        } catch (IllegalStateException e) {
            // should never happen since we are passing an immutable bitmap
            logger.e(TAG, "Failed to create canvas", e);
        } catch (RuntimeException e) {
            logger.e(TAG, "Failed to create canvas", e);
        }
        return null;
    }
}
