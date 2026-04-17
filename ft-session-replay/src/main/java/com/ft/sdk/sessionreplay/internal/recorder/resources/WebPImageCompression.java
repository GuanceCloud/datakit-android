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

    public WebPImageCompression(InternalLogger logger) {
        this.logger = logger;
    }

    @WorkerThread
    @Override
    public byte[] compressBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(bitmap.getAllocationByteCount());
        Bitmap.CompressFormat imageFormat = getImageCompressionFormat();

        try {
            bitmap.compress(imageFormat, IMAGE_QUALITY, byteArrayOutputStream);
        } catch (IllegalStateException e) {
            logger.e(TAG, IMAGE_COMPRESSION_ERROR, e);
            return EMPTY_BYTEARRAY;
        }

        return byteArrayOutputStream.toByteArray();
    }

    private Bitmap.CompressFormat getImageCompressionFormat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Bitmap.CompressFormat.WEBP_LOSSY;
        } else {
            return Bitmap.CompressFormat.WEBP;
        }
    }
}
