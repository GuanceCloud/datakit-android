package com.ft.sdk.sessionreplay.internal.recorder.resources;

import static com.ft.sdk.sessionreplay.utils.DrawableUtils.MAX_BITMAP_SIZE_BYTES_WITH_RESOURCE_ENDPOINT;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import com.ft.sdk.sessionreplay.internal.async.DataQueueHandler;
import com.ft.sdk.sessionreplay.utils.DrawableUtils;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ResourceResolver {
    private final BitmapCachesManager bitmapCachesManager;
    private final ExecutorService threadPoolExecutor;
    private final DrawableUtils drawableUtils;
    private final ImageCompression webPImageCompression;
    private final InternalLogger logger;
    private final MD5HashGenerator md5HashGenerator;
    private final DataQueueHandler recordedDataQueueHandler;
    private final String applicationId;
    private final ResourceItemCreationHandler resourceItemCreationHandler;

    public ResourceResolver(
            BitmapCachesManager bitmapCachesManager,
            ExecutorService threadPoolExecutor,
            DrawableUtils drawableUtils,
            ImageCompression webPImageCompression,
            InternalLogger logger,
            MD5HashGenerator md5HashGenerator,
            DataQueueHandler recordedDataQueueHandler,
            String applicationId) {
        this.bitmapCachesManager = bitmapCachesManager;
        this.threadPoolExecutor = threadPoolExecutor != null ? threadPoolExecutor : THREADPOOL_EXECUTOR;
        this.drawableUtils = drawableUtils;
        this.webPImageCompression = webPImageCompression;
        this.logger = logger;
        this.md5HashGenerator = md5HashGenerator;
        this.recordedDataQueueHandler = recordedDataQueueHandler;
        this.applicationId = applicationId;
        this.resourceItemCreationHandler = new ResourceItemCreationHandler(recordedDataQueueHandler, applicationId);
    }

    @MainThread
    public void resolveResourceId(
            Resources resources,
            Context applicationContext,
            DisplayMetrics displayMetrics,
            Drawable drawable,
            int drawableWidth,
            int drawableHeight,
            ResourceResolverCallback resourceResolverCallback) {
        bitmapCachesManager.registerCallbacks(applicationContext);

        String resourceId = tryToGetResourceFromCache(drawable);

        if (resourceId != null) {
            resourceResolverCallback.onSuccess(resourceId);
            return;
        }

        Bitmap bitmapFromDrawable = (drawable instanceof BitmapDrawable && shouldUseDrawableBitmap((BitmapDrawable) drawable))
                ? ((BitmapDrawable) drawable).getBitmap()
                : null;

        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                createBitmap(
                        resources,
                        drawable,
                        drawableWidth,
                        drawableHeight,
                        displayMetrics,
                        bitmapFromDrawable,
                        new ResolveResourceCallback() {
                            @Override
                            public void onResolved(String resourceId, byte[] resourceData) {
                                resourceItemCreationHandler.queueItem(resourceId, resourceData);
                                resourceResolverCallback.onSuccess(resourceId);
                            }

                            @Override
                            public void onFailed() {
                                resourceResolverCallback.onFailure();
                            }
                        });
            }
        });
    }

    @WorkerThread
    private void createBitmap(
            Resources resources,
            Drawable drawable,
            int drawableWidth,
            int drawableHeight,
            DisplayMetrics displayMetrics,
            Bitmap bitmapFromDrawable,
            ResolveResourceCallback resolveResourceCallback) {
        Bitmap handledBitmap = (bitmapFromDrawable != null)
                ? tryToGetBitmapFromBitmapDrawable((BitmapDrawable) drawable, bitmapFromDrawable, resolveResourceCallback)
                : null;

        if (handledBitmap == null) {
            tryToDrawNewBitmap(
                    resources,
                    drawable,
                    drawableWidth,
                    drawableHeight,
                    displayMetrics,
                    resolveResourceCallback);
        }
    }

    @WorkerThread
    private void resolveResourceHash(
            Drawable drawable,
            Bitmap bitmap,
            byte[] compressedBitmapBytes,
            boolean shouldCacheBitmap,
            ResolveResourceCallback resolveResourceCallback) {
        if (compressedBitmapBytes.length == 0) {
            resolveResourceCallback.onFailed();
            return;
        }

        String resourceId = md5HashGenerator.generate(compressedBitmapBytes);

        if (resourceId == null) {
            resolveResourceCallback.onFailed();
            return;
        }

        cacheIfNecessary(shouldCacheBitmap, bitmap, resourceId, drawable);
        resolveResourceCallback.onResolved(resourceId, compressedBitmapBytes);
    }

    private void cacheIfNecessary(boolean shouldCacheBitmap, Bitmap bitmap, String resourceId, Drawable drawable) {
        if (shouldCacheBitmap) {
            bitmapCachesManager.putInBitmapPool(bitmap);
        }
        bitmapCachesManager.putInResourceCache(drawable, resourceId);
    }

    @WorkerThread
    private void tryToDrawNewBitmap(
            Resources resources,
            Drawable drawable,
            int drawableWidth,
            int drawableHeight,
            DisplayMetrics displayMetrics,
            ResolveResourceCallback resolveResourceCallback) {
        drawableUtils.createBitmapOfApproxSizeFromDrawable(
                resources,
                drawable,
                drawableWidth,
                drawableHeight,
                displayMetrics,
                MAX_BITMAP_SIZE_BYTES_WITH_RESOURCE_ENDPOINT, Bitmap.Config.ARGB_8888,
                new BitmapCreationCallback() {
                    @WorkerThread
                    @Override
                    public void onReady(Bitmap bitmap) {
                        byte[] compressedBitmapBytes = webPImageCompression.compressBitmap(bitmap);

                        if (compressedBitmapBytes.length == 0) {
                            resolveResourceCallback.onFailed();
                            return;
                        }

                        resolveResourceHash(drawable, bitmap, compressedBitmapBytes, true, resolveResourceCallback);
                    }

                    @WorkerThread
                    @Override
                    public void onFailure() {
                        resolveResourceCallback.onFailed();
                    }
                });
    }

    @WorkerThread
    private Bitmap tryToGetBitmapFromBitmapDrawable(
            BitmapDrawable drawable,
            Bitmap bitmapFromDrawable,
            ResolveResourceCallback resolveResourceCallback) {
        Bitmap scaledBitmap = drawableUtils.createScaledBitmap(bitmapFromDrawable,
                MAX_BITMAP_SIZE_BYTES_WITH_RESOURCE_ENDPOINT);

        if (scaledBitmap == null) {
            return null;
        }

        byte[] compressedBitmapBytes = webPImageCompression.compressBitmap(scaledBitmap);

        if (compressedBitmapBytes.length == 0) {
            return null;
        }

        boolean shouldCacheBitmap = !bitmapFromDrawable.isRecycled() &&
                (scaledBitmap.getWidth() < bitmapFromDrawable.getWidth() ||
                        scaledBitmap.getHeight() < bitmapFromDrawable.getHeight());

        resolveResourceHash(drawable, scaledBitmap, compressedBitmapBytes, shouldCacheBitmap, resolveResourceCallback);
        return scaledBitmap;
    }

    private String tryToGetResourceFromCache(Drawable drawable) {
        return bitmapCachesManager.getFromResourceCache(drawable);
    }

    private boolean shouldUseDrawableBitmap(BitmapDrawable drawable) {
        return drawable.getBitmap() != null &&
                !drawable.getBitmap().isRecycled() &&
                drawable.getBitmap().getWidth() > 0 &&
                drawable.getBitmap().getHeight() > 0;
    }

    public interface BitmapCreationCallback {
        @WorkerThread
        void onReady(Bitmap bitmap);

        @WorkerThread
        void onFailure();
    }

    private static final long THREAD_POOL_MAX_KEEP_ALIVE_MS = 5000L;
    private static final int CORE_DEFAULT_POOL_SIZE = 1;
    private static final int MAX_THREAD_COUNT = 10;

    private static final ExecutorService THREADPOOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_DEFAULT_POOL_SIZE,
            MAX_THREAD_COUNT,
            THREAD_POOL_MAX_KEEP_ALIVE_MS,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>()
    );
}
