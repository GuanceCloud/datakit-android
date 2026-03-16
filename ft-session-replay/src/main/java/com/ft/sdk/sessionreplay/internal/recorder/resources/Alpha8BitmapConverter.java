package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.annotation.WorkerThread;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

public class Alpha8BitmapConverter implements BitmapConverter {
    public static final String TAG = "Alpha8BitmapConverter";
    private final InternalLogger logger;

    public Alpha8BitmapConverter(InternalLogger logger) {
        this.logger = logger;
    }

    @WorkerThread
    @Override
    public Bitmap convertAlpha8BitmapToArgb8888(Bitmap bitmap) {
        if (bitmap.isRecycled() || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
            return null;
        }

        Bitmap argbBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        try {
            return drawAlpha8ToArgb(argbBitmap, bitmap);
        } catch (IllegalStateException e) {
            argbBitmap.recycle();
            logger.e(TAG, "Failed to draw alpha8 bitmap to ARGB_8888", e);
            return null;
        }
    }

    private Bitmap drawAlpha8ToArgb(Bitmap destination, Bitmap source) {
        Canvas canvas = new Canvas(destination);
        canvas.drawColor(Color.BLACK);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);

        canvas.drawBitmap(source, 0f, 0f, paint);

        return destination;
    }
}