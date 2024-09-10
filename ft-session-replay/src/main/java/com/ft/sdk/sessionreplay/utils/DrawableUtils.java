package com.ft.sdk.sessionreplay.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Pair;

import androidx.annotation.WorkerThread;

import com.ft.sdk.sessionreplay.internal.recorder.resources.BitmapCachesManager;
import com.ft.sdk.sessionreplay.internal.recorder.resources.ResourceResolver;
import com.ft.sdk.sessionreplay.internal.wrappers.BitmapWrapper;
import com.ft.sdk.sessionreplay.internal.wrappers.CanvasWrapper;

import java.util.concurrent.ExecutorService;

public class DrawableUtils {

    private static final String TAG = "DrawableUtils";
    private final InternalLogger internalLogger;
    private final BitmapCachesManager bitmapCachesManager;
    private final ExecutorService executorService;
    private final BitmapWrapper bitmapWrapper;
    private final CanvasWrapper canvasWrapper;

    public DrawableUtils(InternalLogger internalLogger, BitmapCachesManager bitmapCachesManager,
                         ExecutorService executorService) {
        this.internalLogger = internalLogger;
        this.bitmapCachesManager = bitmapCachesManager;
        this.executorService = executorService;
        this.bitmapWrapper = new BitmapWrapper(internalLogger);
        this.canvasWrapper = new CanvasWrapper(internalLogger);
    }

    @WorkerThread
    public void createBitmapOfApproxSizeFromDrawable(Resources resources, Drawable drawable,
                                                     int drawableWidth, int drawableHeight,
                                                     DisplayMetrics displayMetrics,
                                                     int requestedSizeInBytes, Config config,
                                                     ResourceResolver.BitmapCreationCallback bitmapCreationCallback) {
        createScaledBitmap(drawableWidth, drawableHeight, requestedSizeInBytes, displayMetrics, config,
                new ResizeBitmapCallback() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                drawOnCanvas(resources, bitmap, drawable, bitmapCreationCallback);
                            }
                        });
                    }

                    @Override
                    public void onFailure() {
                        internalLogger.e(TAG, FAILED_TO_CREATE_SCALED_BITMAP_ERROR);
                        bitmapCreationCallback.onFailure();
                    }
                });
    }

    @WorkerThread
    public Bitmap createScaledBitmap(Bitmap bitmap, int requestedSizeInBytes) {
        Pair<Integer, Integer> scaledDimensions = getScaledWidthAndHeight(bitmap.getWidth(), bitmap.getHeight(), requestedSizeInBytes);
        return bitmapWrapper.createScaledBitmap(bitmap, scaledDimensions.first, scaledDimensions.second, false);
    }

    private interface ResizeBitmapCallback {
        @WorkerThread
        void onSuccess(Bitmap bitmap);

        @WorkerThread
        void onFailure();
    }

    @WorkerThread
    private void drawOnCanvas(Resources resources, Bitmap bitmap, Drawable drawable,
                              ResourceResolver.BitmapCreationCallback bitmapCreationCallback) {
        Drawable newDrawable = drawable.getConstantState().newDrawable(resources);
        Canvas canvas = canvasWrapper.createCanvas(bitmap);

        if (canvas == null || newDrawable == null) {
            bitmapCreationCallback.onFailure();
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
            newDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            newDrawable.draw(canvas);
            bitmapCreationCallback.onReady(bitmap);
        }
    }

    @WorkerThread
    private void createScaledBitmap(int drawableWidth, int drawableHeight, int requestedSizeInBytes,
                                    DisplayMetrics displayMetrics, Config config,
                                    ResizeBitmapCallback resizeBitmapCallback) {
        Pair<Integer, Integer> scaledDimensions = getScaledWidthAndHeight(drawableWidth, drawableHeight, requestedSizeInBytes);
        Bitmap result = getBitmapBySize(displayMetrics, scaledDimensions.first, scaledDimensions.second, config);
        if (result == null) {
            resizeBitmapCallback.onFailure();
        } else {
            resizeBitmapCallback.onSuccess(result);
        }
    }

    private Pair<Integer, Integer> getScaledWidthAndHeight(int drawableWidth, int drawableHeight,
                                                           int requestedSizeInBytes) {
        int width = drawableWidth;
        int height = drawableHeight;
        int sizeAfterCreation = width * height * ARGB_8888_PIXEL_SIZE_BYTES;

        if (sizeAfterCreation > requestedSizeInBytes) {
            double bitmapRatio = (double) width / (double) height;
            double totalMaxPixels = (double) (requestedSizeInBytes / ARGB_8888_PIXEL_SIZE_BYTES);
            int maxSize = (int) Math.sqrt(totalMaxPixels);

            width = maxSize;
            height = maxSize;

            if (bitmapRatio > 1) {
                height = (int) (maxSize / bitmapRatio);
            } else {
                width = (int) (maxSize * bitmapRatio);
            }
        }

        return new Pair<>(width, height);
    }

    @WorkerThread
    private Bitmap getBitmapBySize(DisplayMetrics displayMetrics, int width, int height, Config config) {
        return bitmapCachesManager.getBitmapByProperties(width, height, config);
    }

    public static final int MAX_BITMAP_SIZE_BYTES_WITH_RESOURCE_ENDPOINT = 10 * 1024 * 1024; // 10mb
    private static final int ARGB_8888_PIXEL_SIZE_BYTES = 4;
    public static final String FAILED_TO_CREATE_SCALED_BITMAP_ERROR =
            "Failed to create a scaled bitmap from the drawable";
}
