package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.graphics.Bitmap;

import com.ft.sdk.sessionreplay.internal.utils.InvocationUtils;

import java.util.concurrent.Callable;

public class BitmapPoolHelper {

    private final InvocationUtils invocationUtils;

    public BitmapPoolHelper() {
        this(new InvocationUtils());
    }

    public BitmapPoolHelper(InvocationUtils invocationUtils) {
        this.invocationUtils = invocationUtils;
    }

    public String generateKey(Bitmap bitmap) {
        return generateKey(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
    }

    public String generateKey(int width, int height, Bitmap.Config config) {
        return width + "-" + height + "-" + config;
    }

    public <R> R safeCall(Callable<R> call) {
        return invocationUtils.safeCallWithErrorLogging(call, BITMAP_OPERATION_FAILED);
    }

    private static final String BITMAP_OPERATION_FAILED = "operation failed for bitmap pool";
}
