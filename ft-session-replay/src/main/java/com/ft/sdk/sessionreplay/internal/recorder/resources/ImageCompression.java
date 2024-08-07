package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.graphics.Bitmap;

public interface ImageCompression {

    /**
     * Compress the bitmap to a ByteArrayOutputStream.
     */
    byte[] compressBitmap(Bitmap bitmap);
}
