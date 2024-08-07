package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.LayerDrawable;
import android.util.LruCache;

import androidx.annotation.VisibleForTesting;

import com.ft.sdk.sessionreplay.internal.utils.InvocationUtils;
import com.ft.sdk.sessionreplay.utils.CacheUtils;

public class ResourcesLRUCache implements Cache<Drawable, byte[]>, ComponentCallbacks2 {

    private final CacheUtils<String, byte[]> cacheUtils;
    private final InvocationUtils invocationUtils;
    private final LruCache<String, byte[]> cache;

    @VisibleForTesting
    static final int MAX_CACHE_MEMORY_SIZE_BYTES = 4 * 1024 * 1024; // 4MB

    private static final String FAILURE_MSG_EVICT_CACHE_CONTENTS = "Failed to evict cache entries";
    private static final String FAILURE_MSG_PUT_CACHE = "Failed to put item in cache";
    private static final String FAILURE_MSG_GET_CACHE = "Failed to get item from cache";

    public ResourcesLRUCache() {
        this(new CacheUtils<>(), new InvocationUtils());
    }

    public ResourcesLRUCache(CacheUtils<String, byte[]> cacheUtils, InvocationUtils invocationUtils) {
        this.cacheUtils = cacheUtils;
        this.invocationUtils = invocationUtils;
        this.cache = new LruCache<String, byte[]>(MAX_CACHE_MEMORY_SIZE_BYTES) {
            @Override
            protected int sizeOf(String key, byte[] value) {
                return value.length;
            }
        };
    }

    @Override
    public void onTrimMemory(int level) {
        cacheUtils.handleTrimMemory(level, cache);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // No-op
    }

    @Override
    public void onLowMemory() {
        try {
            invocationUtils.safeCallWithErrorLogging(() -> {
                cache.evictAll();
                return null;
            }, FAILURE_MSG_EVICT_CACHE_CONTENTS);
        } catch (Exception e) {
            // Log the exception if needed
        }
    }

    @Override
    public void put(byte[] value) {

    }

    @Override
    public synchronized void put(Drawable element, byte[] value) {
        String key = generateKey(element);
        try {
            invocationUtils.safeCallWithErrorLogging(() -> {
                cache.put(key, value);
                return null;
            }, FAILURE_MSG_PUT_CACHE);
        } catch (Exception e) {
            // Log the exception if needed
        }
    }

    @Override
    public synchronized byte[] get(Drawable element) {
        try {
            return invocationUtils.safeCallWithErrorLogging(() ->
                            cache.get(generateKey(element)),
                    FAILURE_MSG_GET_CACHE
            );
        } catch (Exception e) {
            // Log the exception if needed
            return null;
        }
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public synchronized void clear() {
        try {
            invocationUtils.safeCallWithErrorLogging(() -> {
                cache.evictAll();
                return null;
            }, FAILURE_MSG_EVICT_CACHE_CONTENTS);
        } catch (Exception e) {
            // Log the exception if needed
        }
    }

    @VisibleForTesting
    String generateKey(Drawable drawable) {
        return generatePrefix(drawable) + System.identityHashCode(drawable);
    }

    private String generatePrefix(Drawable drawable) {
        if (drawable instanceof DrawableContainer) {
            return getPrefixForDrawableContainer((DrawableContainer) drawable);
        } else if (drawable instanceof LayerDrawable) {
            return getPrefixForLayerDrawable((LayerDrawable) drawable);
        } else {
            return "";
        }
    }

    private String getPrefixForDrawableContainer(DrawableContainer drawable) {
        if (!(drawable instanceof AnimationDrawable)) {
            DrawableContainer.DrawableContainerState state = (DrawableContainer.DrawableContainerState) drawable.getConstantState();
            if (state != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < state.getChildCount(); i++) {
                    sb.append(System.identityHashCode(state.getChild(i))).append("-");
                }
                return sb.toString();
            }
        }
        return "";
    }

    private String getPrefixForLayerDrawable(LayerDrawable drawable) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < drawable.getNumberOfLayers(); i++) {
            Drawable layer = drawable.getDrawable(i);
            sb.append(System.identityHashCode(layer)).append("-");
        }
        return sb.toString();
    }
}
