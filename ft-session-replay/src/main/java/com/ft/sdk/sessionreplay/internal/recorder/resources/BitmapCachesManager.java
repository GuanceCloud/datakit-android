package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.MainThread;

import com.ft.sdk.sessionreplay.utils.InternalLogger;

import kotlin.text.Charsets;

public class BitmapCachesManager {

    private static final String TAG = "BitmapCachesManager";
    private final Cache<String, byte[]> resourcesLRUCache;
    private final BitmapPool bitmapPool;
    private final InternalLogger logger;

    private boolean isResourcesCacheRegisteredForCallbacks = false;
    private boolean isBitmapPoolRegisteredForCallbacks = false;

    public BitmapCachesManager(Cache<String, byte[]> resourcesLRUCache, BitmapPool bitmapPool, InternalLogger logger) {
        this.resourcesLRUCache = resourcesLRUCache;
        this.bitmapPool = bitmapPool;
        this.logger = logger;
    }

    @MainThread
    public void registerCallbacks(Context applicationContext) {
        registerResourceLruCacheForCallbacks(applicationContext);
        registerBitmapPoolForCallbacks(applicationContext);
    }

    @MainThread
    private void registerResourceLruCacheForCallbacks(Context applicationContext) {
        if (isResourcesCacheRegisteredForCallbacks) {
            return;
        }

        if (resourcesLRUCache instanceof ComponentCallbacks2) {
            applicationContext.registerComponentCallbacks((ComponentCallbacks2) resourcesLRUCache);
            isResourcesCacheRegisteredForCallbacks = true;
        } else {
            logger.e(TAG, Cache.DOES_NOT_IMPLEMENT_COMPONENTCALLBACKS);
        }
    }

    @MainThread
    private void registerBitmapPoolForCallbacks(Context applicationContext) {
        if (isBitmapPoolRegisteredForCallbacks) {
            return;
        }

        applicationContext.registerComponentCallbacks(bitmapPool);
        isBitmapPoolRegisteredForCallbacks = true;
    }

    public void putInResourceCache(String key, String resourceId) {
        resourcesLRUCache.put(key, resourceId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public String getFromResourceCache(String key) {
        byte[] resourceId = resourcesLRUCache.get(key);
        return (resourceId != null) ? new String(resourceId, java.nio.charset.StandardCharsets.UTF_8) : null;
    }

    public String generateResourceKeyFromDrawable(Drawable drawable) {
        if (resourcesLRUCache instanceof ResourcesLRUCache) {
            return ((ResourcesLRUCache) resourcesLRUCache).generateKeyFromDrawable(drawable);
        }
        return null;
    }

    public void putInBitmapPool(Bitmap bitmap) {
        bitmapPool.put(bitmap);
    }

    public Bitmap getBitmapByProperties(int width, int height, Bitmap.Config config) {
        return bitmapPool.getBitmapByProperties(width, height, config);
    }
}