package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.graphics.Bitmap;
import androidx.annotation.WorkerThread;

public interface BitmapConverter {
    @WorkerThread
    Bitmap convertAlpha8BitmapToArgb8888(Bitmap bitmap);
}