package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.WorkerThread;

import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.io.ByteArrayOutputStream;

public class WebPImageCompression implements ImageCompression {

    private static final String TAG = "WebPImageCompression";
    private static final byte[] EMPTY_BYTEARRAY = new byte[0];
    private static final int IMAGE_QUALITY = 75;
    private static final String IMAGE_COMPRESSION_ERROR = "Error while compressing the image.";

    private final InternalLogger logger;

    public static final class BudgetedResult {
        private final byte[] bytes;
        private final int width;
        private final int height;

        private BudgetedResult(byte[] bytes, int width, int height) {
            this.bytes = bytes;
            this.width = width;
            this.height = height;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    public WebPImageCompression(InternalLogger logger) {
        this.logger = logger;
    }

    @WorkerThread
    @Override
    public byte[] compressBitmap(Bitmap bitmap) {
        return compressBitmap(bitmap, IMAGE_QUALITY);
    }

    @WorkerThread
    public byte[] compressBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bitmap.getAllocationByteCount());
        Bitmap.CompressFormat imageFormat = getImageCompressionFormat();

        try {
            bitmap.compress(imageFormat, Math.max(0, Math.min(100, quality)), byteArrayOutputStream);
        } catch (IllegalStateException e) {
            logger.e(TAG, IMAGE_COMPRESSION_ERROR, e);
            return EMPTY_BYTEARRAY;
        }

        return byteArrayOutputStream.toByteArray();
    }

    @WorkerThread
    public BudgetedResult compressBitmapWithBudget(Bitmap bitmap, float quality, int maxFrameBytes) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int requestedQuality = Math.max(10, Math.min(100, Math.round(quality * 100f)));
        byte[] bytes = compressBitmap(bitmap, requestedQuality);
        if (bytes.length <= maxFrameBytes) return new BudgetedResult(bytes, width, height);

        requestedQuality = Math.max(20, Math.round(requestedQuality * 0.7f));
        bytes = compressBitmap(bitmap, requestedQuality);
        if (bytes.length <= maxFrameBytes) return new BudgetedResult(bytes, width, height);

        if (width <= 1 || height <= 1) return new BudgetedResult(EMPTY_BYTEARRAY, width, height);
        width = Math.max(1, (int) Math.floor(width * 0.75));
        height = Math.max(1, (int) Math.floor(height * 0.75));
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, true);
        try {
            bytes = compressBitmap(scaled, requestedQuality);
            return new BudgetedResult(
                    bytes.length <= maxFrameBytes ? bytes : EMPTY_BYTEARRAY,
                    width,
                    height);
        } finally {
            if (scaled != bitmap && !scaled.isRecycled()) scaled.recycle();
        }
    }

    private Bitmap.CompressFormat getImageCompressionFormat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Bitmap.CompressFormat.WEBP_LOSSY;
        } else {
            return Bitmap.CompressFormat.WEBP;
        }
    }
}
