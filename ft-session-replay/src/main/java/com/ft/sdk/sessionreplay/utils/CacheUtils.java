package com.ft.sdk.sessionreplay.utils;

import android.content.ComponentCallbacks2;
import android.util.LruCache;

import com.ft.sdk.sessionreplay.internal.utils.InvocationUtils;

public class CacheUtils<K, V> {
    private InvocationUtils invocationUtils;

    public CacheUtils() {
        this.invocationUtils = new InvocationUtils();
    }

    public CacheUtils(InvocationUtils invocationUtils) {
        this.invocationUtils = invocationUtils;
    }

    public void handleTrimMemory(int level, LruCache<K, V> cache) {
        final int onLowMemorySizeBytes = cache.maxSize() / 2;
        final int onModerateMemorySizeBytes = (cache.maxSize() / 4) * 3;

        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                evictAll(cache);
                break;

            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
                trimToSize(cache, onModerateMemorySizeBytes);
                break;

            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
                trimToSize(cache, onLowMemorySizeBytes);
                break;

            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                // No action needed for UI hidden
                break;

            default:
                evictAll(cache);
                break;
        }
    }

    private void evictAll(LruCache<K, V> cache) {
        try {
            cache.evictAll();
        } catch (Exception e) {
            invocationUtils.safeCallWithErrorLogging(
                () -> {
                    cache.evictAll();
                    return null;
                },
                "Failed to evict cache entries"
            );
        }
    }

    private void trimToSize(LruCache<K, V> cache, int targetSize) {
        try {
            cache.trimToSize(targetSize);
        } catch (Exception e) {
            invocationUtils.safeCallWithErrorLogging(
                () -> {
                    cache.trimToSize(targetSize);
                    return null;
                },
                "Failed to trim cache to size"
            );
        }
    }
}
