package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.graphics.Bitmap;

public interface BitmapSignatureGenerator {
    Long generateSignature(Bitmap bitmap);
}