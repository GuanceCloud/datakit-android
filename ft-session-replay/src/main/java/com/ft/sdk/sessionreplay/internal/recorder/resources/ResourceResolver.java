package com.ft.sdk.sessionreplay.internal.recorder.resources;

import static com.ft.sdk.sessionreplay.utils.DrawableUtils.MAX_BITMAP_SIZE_BYTES_WITH_RESOURCE_ENDPOINT;
import static com.ft.sdk.sessionreplay.utils.PathUtils.DEFAULT_MAX_PATH_LENGTH;
import static com.ft.sdk.sessionreplay.utils.PathUtils.DEFAULT_SAMPLE_INTERVAL;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import com.ft.sdk.sessionreplay.internal.async.DataQueueHandler;
import com.ft.sdk.sessionreplay.internal.utils.ExecutorUtils;
import com.ft.sdk.sessionreplay.resources.DrawableCopier;
import com.ft.sdk.sessionreplay.utils.DrawableUtils;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.PathUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ResourceResolver {
    private final BitmapCachesManager bitmapCachesManager;
    private final ExecutorService threadPoolExecutor;
    private final DrawableUtils drawableUtils;
    private final PathUtils pathUtils;
    private final ImageCompression webPImageCompression;
    private final InternalLogger logger;
    private final MD5HashGenerator md5HashGenerator;
    private final DataQueueHandler recordedDataQueueHandler;
    private final String applicationId;
    private final ResourceItemCreationHandler resourceItemCreationHandler;

    public ResourceResolver(
            BitmapCachesManager bitmapCachesManager,
            ExecutorService threadPoolExecutor,
            PathUtils pathUtils,
            DrawableUtils drawableUtils,
            ImageCompression webPImageCompression,
            InternalLogger logger,
            MD5HashGenerator md5HashGenerator,
            DataQueueHandler recordedDataQueueHandler,
            String applicationId) {
        this.bitmapCachesManager = bitmapCachesManager;
        this.threadPoolExecutor = threadPoolExecutor != null ? threadPoolExecutor : THREADPOOL_EXECUTOR;
        this.drawableUtils = drawableUtils;
        this.pathUtils = pathUtils;
        this.webPImageCompression = webPImageCompression;
        this.logger = logger;
        this.md5HashGenerator = md5HashGenerator;
        this.recordedDataQueueHandler = recordedDataQueueHandler;
        this.applicationId = applicationId;
        this.resourceItemCreationHandler = new ResourceItemCreationHandler(recordedDataQueueHandler, applicationId);
    }

    @WorkerThread
    @SuppressWarnings("ReturnCount")
    private void resolveResourceHash(
            Drawable drawable,
            Bitmap bitmap,
            byte[] compressedBitmapBytes,
            boolean shouldCacheBitmap,
            String customResourceIdCacheKey,
            ResolveResourceCallback resolveResourceCallback
    ) {
        // failed to get image data
        if (compressedBitmapBytes == null || compressedBitmapBytes.length == 0) {
            // we are already logging the failure in webpImageCompression
            resolveResourceCallback.onFailed();
            return;
        }

        String resourceId = md5HashGenerator.generate(compressedBitmapBytes);

        // failed to resolve bitmap identifier
        if (resourceId == null) {
            // logging md5 generation failures inside md5HashGenerator
            resolveResourceCallback.onFailed();
            return;
        }

        cacheIfNecessary(
                shouldCacheBitmap,
                bitmap,
                resourceId,
                customResourceIdCacheKey,
                drawable
        );

        resolveResourceCallback.onResolved(resourceId, compressedBitmapBytes);
    }

    private void cacheIfNecessary(
            boolean shouldCacheBitmap,
            Bitmap bitmap,
            String resourceId,
            String customResourceIdCacheKey,
            Drawable drawable
    ) {
        if (shouldCacheBitmap) {
            bitmapCachesManager.putInBitmapPool(bitmap);
        }

        String key = (customResourceIdCacheKey != null)
                ? customResourceIdCacheKey
                : generateKey(drawable);

        if (key == null) {
            return;
        }

        bitmapCachesManager.putInResourceCache(key, resourceId);
    }

    private String generateKey(Drawable drawable) {
        return (drawable != null) ? bitmapCachesManager.generateResourceKeyFromDrawable(drawable) : null;
    }


    @WorkerThread
    private void tryToDrawNewBitmap(
            Drawable originalDrawable,
            Drawable copiedDrawable,
            int drawableWidth,
            int drawableHeight,
            DisplayMetrics displayMetrics,
            String customResourceIdCacheKey,
            ResolveResourceCallback resolveResourceCallback
    ) {
        drawableUtils.createBitmapOfApproxSizeFromDrawable(
                copiedDrawable,
                drawableWidth,
                drawableHeight,
                displayMetrics,
                MAX_BITMAP_SIZE_BYTES_WITH_RESOURCE_ENDPOINT,
                Bitmap.Config.ARGB_8888,
                new BitmapCreationCallback() {
                    @WorkerThread
                    @Override
                    public void onReady(Bitmap bitmap) {
                        compressAndCacheBitmap(
                                originalDrawable,
                                bitmap,
                                customResourceIdCacheKey,
                                resolveResourceCallback
                        );
                    }

                    @WorkerThread
                    @Override
                    public void onFailure() {
                        resolveResourceCallback.onFailed();
                    }
                }
        );
    }

    @WorkerThread
    private void compressAndCacheBitmap(
            Drawable drawable,
            Bitmap bitmap,
            String customResourceIdCacheKey,
            ResolveResourceCallback resolveResourceCallback
    ) {
        byte[] compressedBitmapBytes = webPImageCompression.compressBitmap(bitmap);

        // failed to compress bitmap
        if (compressedBitmapBytes.length == 0) {
            resolveResourceCallback.onFailed();
            return;
        }

        resolveResourceHash(
                drawable,
                bitmap,
                compressedBitmapBytes,
                true, // shouldCacheBitmap
                customResourceIdCacheKey,
                resolveResourceCallback
        );
    }

    @WorkerThread
    private Bitmap tryToGetBitmapFromBitmapDrawable(
            Drawable drawable,
            Bitmap bitmapFromDrawable,
            String customResourceIdCacheKey,
            ResolveResourceCallback resolveResourceCallback
    ) {
        Bitmap scaledBitmap = drawableUtils.createScaledBitmap(bitmapFromDrawable, MAX_BITMAP_SIZE_BYTES_WITH_RESOURCE_ENDPOINT);
        if (scaledBitmap == null) {
            return null;
        }

        byte[] compressedBitmapBytes = webPImageCompression.compressBitmap(scaledBitmap);

        // failed to get byteArray potentially because the bitmap was recycled before imageCompression
        if (compressedBitmapBytes.length == 0) {
            return null;
        }

        /**
         * Check whether the scaled bitmap is the same as the original.
         * Since Bitmap.createScaledBitmap will return the original bitmap if the
         * requested dimensions match the dimensions of the original.
         * Add a specific check for isRecycled, because getting width/height from a recycled bitmap
         * is undefined behavior.
         */
        boolean shouldCacheBitmap = !bitmapFromDrawable.isRecycled() &&
                (scaledBitmap.getWidth() < bitmapFromDrawable.getWidth() ||
                        scaledBitmap.getHeight() < bitmapFromDrawable.getHeight());

        resolveResourceHash(
                drawable,
                scaledBitmap,
                compressedBitmapBytes,
                shouldCacheBitmap,
                customResourceIdCacheKey,
                resolveResourceCallback
        );

        return scaledBitmap;
    }


    private String tryToGetResourceFromCache(Drawable drawable, String customResourceIdCacheKey) {
        String key = (customResourceIdCacheKey != null) ? customResourceIdCacheKey : generateKey(drawable);
        if (key == null) {
            return null;
        }
        return bitmapCachesManager.getFromResourceCache(key);
    }

    private boolean shouldUseDrawableBitmap(BitmapDrawable drawable) {
        return drawable.getBitmap() != null &&
                !drawable.getBitmap().isRecycled() &&
                drawable.getBitmap().getWidth() > 0 &&
                drawable.getBitmap().getHeight() > 0;
    }


    @MainThread
    void resolveResourceIdFromBitmap(Bitmap bitmap, ResourceResolverCallback resourceResolverCallback) {
        ExecutorUtils.executeSafe(threadPoolExecutor, RESOURCE_RESOLVER_ALIAS, logger,
                () -> getResourceIdFromBitmap(bitmap, resourceResolverCallback));

    }

    @WorkerThread
    private void getResourceIdFromBitmap(Bitmap bitmap, ResourceResolverCallback resourceResolverCallback) {
        byte[] compressedBitmapBytes = webPImageCompression.compressBitmap(bitmap);

        // failed to compress bitmap
        if (compressedBitmapBytes == null || compressedBitmapBytes.length == 0) {
            resourceResolverCallback.onFailure();
            return;
        }

        resolveBitmapHash(compressedBitmapBytes, new ResolveResourceCallback() {
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

    @WorkerThread
    private void createBitmapFromDrawable(
            Drawable drawable,
            Drawable copiedDrawable,
            int drawableWidth,
            int drawableHeight,
            DisplayMetrics displayMetrics,
            Bitmap bitmapFromDrawable,
            String customResourceIdCacheKey,
            ResolveResourceCallback resolveResourceCallback
    ) {
        Bitmap handledBitmap = (bitmapFromDrawable != null)
                ? tryToGetBitmapFromBitmapDrawable(
                drawable,
                bitmapFromDrawable,
                customResourceIdCacheKey,
                resolveResourceCallback
        )
                : null;

        if (handledBitmap == null) {
            tryToDrawNewBitmap(
                    drawable,
                    copiedDrawable,
                    drawableWidth,
                    drawableHeight,
                    displayMetrics,
                    customResourceIdCacheKey,
                    resolveResourceCallback
            );
        }
    }

    @WorkerThread
    private void resolveBitmapHash(byte[] compressedBitmapBytes, ResolveResourceCallback resolveResourceCallback) {
        // failed to get image data
        if (compressedBitmapBytes == null || compressedBitmapBytes.length == 0) {
            // we are already logging the failure in webpImageCompression
            resolveResourceCallback.onFailed();
            return;
        }

        String resourceId = md5HashGenerator.generate(compressedBitmapBytes);

        // failed to resolve bitmap identifier
        if (resourceId == null) {
            // logging md5 generation failures inside md5HashGenerator
            resolveResourceCallback.onFailed();
            return;
        }

        resolveResourceCallback.onResolved(resourceId, compressedBitmapBytes);
    }

    @MainThread
    void resolveResourceIdFromDrawable(
            Resources resources,
            Context applicationContext,
            DisplayMetrics displayMetrics,
            Drawable originalDrawable,
            DrawableCopier drawableCopier,
            int drawableWidth,
            int drawableHeight,
            String customResourceIdCacheKey,
            ResourceResolverCallback resourceResolverCallback
    ) {
        bitmapCachesManager.registerCallbacks(applicationContext);

        String resourceId = tryToGetResourceFromCache(originalDrawable, customResourceIdCacheKey);

        if (resourceId != null) {
            // If we got here it means we saw the bitmap before,
            // so we don't need to send the resource again
            resourceResolverCallback.onSuccess(resourceId);
            return;
        }

        Drawable copiedDrawable = drawableCopier.copy(originalDrawable, resources);
        if (copiedDrawable == null) {
            resourceResolverCallback.onFailure();
            return;
        }

        BitmapDrawable bitmapFromDrawable = null;
        if (copiedDrawable instanceof BitmapDrawable && shouldUseDrawableBitmap((BitmapDrawable) copiedDrawable)) {
            bitmapFromDrawable = (BitmapDrawable) copiedDrawable;
        }

        // Do in the background
        BitmapDrawable finalBitmapFromDrawable = bitmapFromDrawable;
        ExecutorUtils.executeSafe(threadPoolExecutor, RESOURCE_RESOLVER_ALIAS, logger, new Runnable() {
            @Override
            public void run() {
                createBitmapFromDrawable(
                        originalDrawable,
                        copiedDrawable,
                        drawableWidth,
                        drawableHeight,
                        displayMetrics,
                        finalBitmapFromDrawable != null ? finalBitmapFromDrawable.getBitmap() : null,
                        customResourceIdCacheKey,
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
                        }
                );
            }
        });
    }

    @MainThread
    void resolveResourceIdFromPath(
            Path path,
            Integer strokeColor,
            Integer strokeWidth,
            Integer desiredWidth,
            Integer desiredHeight,
            String customResourceIdCacheKey,
            ResourceResolverCallback resourceResolverCallback
    ) {
        ExecutorUtils.executeSafe(threadPoolExecutor, RESOURCE_RESOLVER_ALIAS, logger, new Runnable() {
            @Override
            public void run() {
                String key = (customResourceIdCacheKey != null)
                        ? customResourceIdCacheKey
                        : pathUtils.generateKeyForPath(path, DEFAULT_MAX_PATH_LENGTH, DEFAULT_SAMPLE_INTERVAL, new PathMeasure(path, false));

                String resourceId = tryToGetResourceFromCache(null, key);

                if (resourceId != null) {
                    // If the resource ID can be obtained from the cache, return directly
                    resourceResolverCallback.onSuccess(resourceId);
                    return;
                }

                Bitmap bitmap = pathUtils.convertPathToBitmap(
                        path,
                        strokeColor,
                        desiredWidth,
                        desiredHeight,
                        strokeWidth
                );

                if (bitmap == null) {
                    resourceResolverCallback.onFailure();
                    return;
                }

                compressAndCacheBitmap(
                        null,
                        bitmap,
                        customResourceIdCacheKey,
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
                        }
                );
            }
        });
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
    private static final String RESOURCE_RESOLVER_ALIAS = "resolveResourceId";
    private static final ExecutorService THREADPOOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_DEFAULT_POOL_SIZE,
            MAX_THREAD_COUNT,
            THREAD_POOL_MAX_KEEP_ALIVE_MS,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>()
    );
}
