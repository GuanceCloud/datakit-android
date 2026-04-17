package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.graphics.Bitmap;

import androidx.collection.LruCache;

public class DefaultAlpha8ResourceCache implements Alpha8ResourceCache {
    private final BitmapSignatureGenerator signatureGenerator;
    private final LruCache<Alpha8CacheKey, String> cache;

    public DefaultAlpha8ResourceCache(BitmapSignatureGenerator signatureGenerator) {
        this.signatureGenerator = signatureGenerator;
        this.cache = new LruCache<Alpha8CacheKey, String>(MAX_CACHE_MEMORY_SIZE_BYTES) {
            @Override
            protected int sizeOf(Alpha8CacheKey key, String value) {
                return (value.length() * 2) + STRING_OBJECT_OVERHEAD_BYTES;
            }
        };
    }

    @Override
    public Alpha8CacheKey generateKey(Bitmap bitmap) {
        Long signature = signatureGenerator.generateSignature(bitmap);
        if (signature == null) {
            return null;
        }
        return new Alpha8CacheKey(bitmap.getWidth(), bitmap.getHeight(), signature);
    }

    @Override
    public String get(Alpha8CacheKey key) {
        return cache.get(key);
    }

    @Override
    public void put(Alpha8CacheKey key, String resourceId) {
        cache.put(key, resourceId);
    }

    @Override
    public void onTrimMemory(int level) {
        int onLowMemorySizeBytes = cache.maxSize() / 2;
        int onModerateMemorySizeBytes = (cache.maxSize() / 4) * 3;

        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                cache.evictAll();
                break;
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
                cache.trimToSize(onModerateMemorySizeBytes);
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
                cache.trimToSize(onLowMemorySizeBytes);
                break;
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                break;
            default:
                cache.evictAll();
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {}

    @Override
    public void onLowMemory() {
        cache.evictAll();
    }

    public static final int MAX_CACHE_MEMORY_SIZE_BYTES = 4 * 1024 * 1024; // 4MB
    public static final int STRING_OBJECT_OVERHEAD_BYTES = 40;
}