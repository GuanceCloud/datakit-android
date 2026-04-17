package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.content.ComponentCallbacks2;
import android.graphics.Bitmap;

public interface Alpha8ResourceCache extends ComponentCallbacks2 {
    /**
     * Generates a cache key for the bitmap. This is separated from get/put so the caller
     * can compute the key once and reuse it for both operations.
     * @return The cache key, or null if signature generation failed
     */
    Alpha8CacheKey generateKey(Bitmap bitmap);

    String get(Alpha8CacheKey key);

    void put(Alpha8CacheKey key, String resourceId);
}