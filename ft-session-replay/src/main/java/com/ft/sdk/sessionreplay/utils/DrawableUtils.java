package com.ft.sdk.sessionreplay.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import androidx.annotation.WorkerThread;

import com.ft.sdk.sessionreplay.internal.recorder.resources.BitmapCachesManager;
import com.ft.sdk.sessionreplay.internal.recorder.resources.ResourceResolver;
import com.ft.sdk.sessionreplay.internal.utils.ExecutorUtils;
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

    public DrawableUtils(
            InternalLogger internalLogger,
            BitmapCachesManager bitmapCachesManager,
            ExecutorService executorService
    ) {
        this.internalLogger = internalLogger;
        this.bitmapCachesManager = bitmapCachesManager;
        this.executorService = executorService;
        this.bitmapWrapper = new BitmapWrapper(internalLogger);
        this.canvasWrapper = new CanvasWrapper(internalLogger);
    }

    /**
     * This method attempts to create a bitmap from a drawable, such that the bitmap file size will
     * be equal or less than a given size. It does so by modifying the dimensions of the
     * bitmap, since the file size of a bitmap can be known by the formula width * height * color depth.
     */
    @WorkerThread
    public void createBitmapOfApproxSizeFromDrawable(
            Drawable drawable,
            int drawableWidth,
            int drawableHeight,
            DisplayMetrics displayMetrics,
            int requestedSizeInBytes,
            Bitmap.Config config,
            ResourceResolver.BitmapCreationCallback bitmapCreationCallback
    ) {
        createScaledBitmap(
                drawableWidth,
                drawableHeight,
                requestedSizeInBytes,
                displayMetrics,
                config,
                new ResizeBitmapCallback() {
                    @Override
                    @WorkerThread
                    public void onSuccess(Bitmap bitmap) {
                        ExecutorUtils.executeSafe(executorService, "drawOnCanvas", internalLogger,
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        drawOnCanvas(bitmap, drawable, bitmapCreationCallback);
                                    }
                                }
                        );
                    }

                    @Override
                    @WorkerThread
                    public void onFailure() {
                        internalLogger.e(TAG, FAILED_TO_CREATE_SCALED_BITMAP_ERROR);
                        bitmapCreationCallback.onFailure();
                    }
                }
        );
    }

    @WorkerThread
    public Bitmap createScaledBitmap(Bitmap bitmap, int requestedSizeInBytes) {
        int[] widthHeight = getScaledWidthAndHeight(bitmap.getWidth(), bitmap.getHeight(), requestedSizeInBytes);
        return bitmapWrapper.createScaledBitmap(bitmap, widthHeight[0], widthHeight[1], false);
    }

    public interface ResizeBitmapCallback {
        @WorkerThread
        void onSuccess(Bitmap bitmap);

        @WorkerThread
        void onFailure();
    }

    @WorkerThread
    private void drawOnCanvas(Bitmap bitmap, Drawable drawable, ResourceResolver.BitmapCreationCallback bitmapCreationCallback) {
        Canvas canvas = canvasWrapper.createCanvas(bitmap);

        if (canvas == null) {
            bitmapCreationCallback.onFailure();
        } else {
            // Erase the canvas
            // Needed because overdrawing an already used bitmap causes unusual visual artifacts
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);

            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            try {
                drawable.draw(canvas);
            } catch (RuntimeException e) {
                logDrawableDrawException(e);
                bitmapCreationCallback.onFailure();
                return;
            }
            bitmapCreationCallback.onReady(bitmap);
        }
    }

    private void logDrawableDrawException(RuntimeException runtimeException) {
        internalLogger.w(TAG, "drawOnCanvas fail with" + runtimeException.getMessage());
    }

    @WorkerThread
    private void createScaledBitmap(
            int drawableWidth,
            int drawableHeight,
            int requestedSizeInBytes,
            DisplayMetrics displayMetrics,
            Bitmap.Config config,
            ResizeBitmapCallback resizeBitmapCallback
    ) {
        int[] widthHeight = getScaledWidthAndHeight(drawableWidth, drawableHeight, requestedSizeInBytes);
        Bitmap result = getBitmapBySize(displayMetrics, widthHeight[0], widthHeight[1], config);

        if (result == null) {
            resizeBitmapCallback.onFailure();
        } else {
            resizeBitmapCallback.onSuccess(result);
        }
    }

    private int[] getScaledWidthAndHeight(int drawableWidth, int drawableHeight, int requestedSizeInBytes) {
        int width = drawableWidth;
        int height = drawableHeight;
        int sizeAfterCreation = width * height * ARGB_8888_PIXEL_SIZE_BYTES;

        if (sizeAfterCreation > requestedSizeInBytes) {
            double bitmapRatio = (double) width / height;
            double totalMaxPixels = (double) requestedSizeInBytes / ARGB_8888_PIXEL_SIZE_BYTES;
            int maxSize = (int) Math.sqrt(totalMaxPixels);
            width = maxSize;
            height = maxSize;

            if (bitmapRatio > 1) { // width > height
                height = (int) (maxSize / bitmapRatio);
            } else {
                width = (int) (maxSize * bitmapRatio);
            }
        }

        return new int[]{width, height};
    }

    private Bitmap getBitmapBySize(DisplayMetrics displayMetrics, int width, int height, Bitmap.Config config) {
        Bitmap cachedBitmap = bitmapCachesManager.getBitmapByProperties(width, height, config);
        return cachedBitmap != null
                ? cachedBitmap
                : bitmapWrapper.createBitmap(displayMetrics, width, height, config);
    }

    public static final int MAX_BITMAP_SIZE_BYTES_WITH_RESOURCE_ENDPOINT = 10 * 1024 * 1024; // 10MB
    private static final int ARGB_8888_PIXEL_SIZE_BYTES = 4;
    private static final String FAILED_TO_CREATE_SCALED_BITMAP_ERROR = "Failed to create a scaled bitmap from the drawable";
}
