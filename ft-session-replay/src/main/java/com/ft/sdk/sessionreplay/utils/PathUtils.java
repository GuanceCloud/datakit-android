package com.ft.sdk.sessionreplay.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;

import com.ft.sdk.sessionreplay.internal.recorder.resources.BitmapCachesManager;
import com.ft.sdk.sessionreplay.internal.recorder.resources.HashGenerator;
import com.ft.sdk.sessionreplay.internal.recorder.resources.MD5HashGenerator;
import com.ft.sdk.sessionreplay.internal.wrappers.BitmapWrapper;
import com.ft.sdk.sessionreplay.internal.wrappers.CanvasWrapper;

import org.jetbrains.annotations.Nullable;

public class PathUtils {
    private final InternalLogger logger;
    private final BitmapCachesManager bitmapCachesManager;
    private final CanvasWrapper canvasWrapper;
    private final BitmapWrapper bitmapWrapper;
    private final HashGenerator md5Generator;

    public PathUtils(InternalLogger logger,
                     BitmapCachesManager bitmapCachesManager,
                     CanvasWrapper canvasWrapper,
                     BitmapWrapper bitmapWrapper,
                     HashGenerator md5Generator) {
        this.logger = logger != null ? logger : new NoOpInternalLogger();
        this.bitmapCachesManager = bitmapCachesManager;
        this.canvasWrapper = canvasWrapper != null ? canvasWrapper : new CanvasWrapper(this.logger);
        this.bitmapWrapper = bitmapWrapper != null ? bitmapWrapper : new BitmapWrapper(this.logger);
        this.md5Generator = md5Generator != null ? md5Generator : new MD5HashGenerator(this.logger);
    }

    @Nullable
    public Bitmap convertPathToBitmap(Path checkPath, int checkmarkColor, int desiredWidth, int desiredHeight, int strokeWidth) {
        Path scaledPath = scalePathToTargetDimensions(checkPath, desiredWidth, desiredHeight);
        Bitmap mutableBitmap = bitmapCachesManager.getBitmapByProperties(desiredWidth, desiredHeight, Bitmap.Config.ARGB_8888);

        if (mutableBitmap == null) {
            mutableBitmap = bitmapWrapper.createBitmap(null, desiredWidth, desiredHeight, Bitmap.Config.ARGB_8888);
        }

        if (mutableBitmap == null) return null;

        return drawPathOntoBitmap(mutableBitmap, scaledPath, strokeWidth, checkmarkColor);
    }

    private void drawPathToBitmap(int checkmarkColor, Path path, int targetStrokeWidth, Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(checkmarkColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(targetStrokeWidth);
        paint.setAntiAlias(true);
        drawPathSafe(canvas, path, paint);
    }

    private Path scalePathToTargetDimensions(Path path, int targetWidth, int targetHeight) {
        RectF originalBounds = new RectF();
        path.computeBounds(originalBounds, true);

        float scaleX = targetWidth / originalBounds.width();
        float scaleY = targetHeight / originalBounds.height();
        float scaleFactor = Math.min(scaleX, scaleY);

        float currentCenterX = (originalBounds.left + originalBounds.right) / 2;
        float currentCenterY = (originalBounds.top + originalBounds.bottom) / 2;

        float newCenterX = targetWidth / 2.0f;
        float newCenterY = targetHeight / 2.0f;

        float scaledCenterX = currentCenterX * scaleFactor;
        float scaledCenterY = currentCenterY * scaleFactor;

        float translateX = newCenterX - scaledCenterX;
        float translateY = newCenterY - scaledCenterY;

        Matrix matrix = new Matrix();
        matrix.preTranslate(translateX, translateY);
        matrix.preScale(scaleFactor, scaleFactor);
        path.transform(matrix);

        return path;
    }

    @Nullable
    private Bitmap drawPathOntoBitmap(Bitmap bitmap, Path scaledPath, int strokeWidth, int checkmarkColor) {
        Canvas canvas = canvasWrapper.createCanvas(bitmap);
        if (canvas == null) return null;

        drawPathToBitmap(checkmarkColor, scaledPath, strokeWidth, canvas);
        return bitmap;
    }

    private void drawPathSafe(Canvas canvas, Path path, Paint paint) {
        try {
            if (canvas != null) {
                canvas.drawPath(path, paint);
            }
        } catch (IllegalArgumentException e) {
//            logger.w(InternalLogger.Target.MAINTAINER, InternalLogger.Level.WARN, () -> PATH_DRAW_ERROR, e);
        }
    }

    @Nullable
    public String generateKeyForPath(Path path, int maxPoints, float sampleInterval, PathMeasure pathMeasure) {
        if (pathMeasure == null) {
            pathMeasure = new PathMeasure(path, false);
        }

        float[] pos = new float[2];
        float[] tan = new float[2];
        StringBuilder sampledPoints = new StringBuilder();
        int pointCount = 0;

        float distance = 0f;
        while (distance < pathMeasure.getLength() && pointCount < maxPoints) {
            pathMeasure.getPosTan(distance, pos, tan);
            sampledPoints.append(pos[0]).append(",").append(pos[1]).append(";");
            pointCount++;
            distance += sampleInterval;
            if (!pathMeasure.nextContour()) break;
        }

        String points = sampledPoints.toString();
        return points.equals(EMPTY_POINTS) ? null : md5Generator.generate(points.getBytes());
    }

    private static final String PATH_DRAW_ERROR = "Failed to draw Path to Canvas";
    private static final String EMPTY_POINTS = "0.0,0.0;";
    public static final int DEFAULT_MAX_PATH_LENGTH = 1000;
    public static final float DEFAULT_SAMPLE_INTERVAL = 10f;
}