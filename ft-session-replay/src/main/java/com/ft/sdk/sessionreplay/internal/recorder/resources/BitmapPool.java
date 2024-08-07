package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;

import androidx.annotation.VisibleForTesting;

import com.ft.sdk.sessionreplay.utils.CacheUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class BitmapPool implements Cache<String, Bitmap>, ComponentCallbacks2 {
    private final BitmapPoolHelper bitmapPoolHelper;
    private final CacheUtils<String, Bitmap> cacheUtils;
    @VisibleForTesting
    final HashMap<String, HashSet<Bitmap>> bitmapsBySize;
    @VisibleForTesting
    final HashSet<Bitmap> usedBitmaps;
    private final LruCache<String, Bitmap> cache;
    private final AtomicInteger bitmapIndex = new AtomicInteger(0);

    @VisibleForTesting
    static final int MAX_CACHE_MEMORY_SIZE_BYTES = 4 * 1024 * 1024; // 4MB

    public BitmapPool() {
        this.bitmapPoolHelper = new BitmapPoolHelper();
        this.cacheUtils = new CacheUtils<>();
        this.bitmapsBySize = new HashMap<>();
        this.usedBitmaps = new HashSet<>();
        this.cache = new LruCache<String, Bitmap>(MAX_CACHE_MEMORY_SIZE_BYTES) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getAllocationByteCount();
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                bitmapPoolHelper.safeCall(() -> {
                    super.entryRemoved(evicted, key, oldValue, newValue);
                    return null;
                });

                String dimensionsKey = bitmapPoolHelper.generateKey(oldValue);
                HashSet<Bitmap> bitmapGroup = bitmapsBySize.get(dimensionsKey);

                bitmapPoolHelper.safeCall(() -> {
                    bitmapGroup.remove(oldValue);
                    return null;
                });

                bitmapPoolHelper.safeCall(() -> {
                    usedBitmaps.remove(oldValue);
                    return null;
                });

                oldValue.recycle();
            }
        };
    }

    @Override
    public synchronized void put(Bitmap value) {
        if (!value.isMutable() || value.isRecycled()) {
            return;
        }

        String key = bitmapPoolHelper.generateKey(value);

        boolean bitmapExistsInPool = bitmapPoolHelper.safeCall(() -> bitmapsBySize.containsKey(key) && bitmapsBySize.get(key).contains(value));

        if (!bitmapExistsInPool) {
            addBitmapToPool(key, value);
        }

        markBitmapAsFree(value);
    }

    @Override
    public void put(String element, Bitmap value) {

    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public synchronized void clear() {
        bitmapPoolHelper.safeCall(() -> {
            cache.evictAll();
            return null;
        });
    }

    @Override
    public synchronized Bitmap get(String element) {
        HashSet<Bitmap> bitmapsWithReqDimensions = bitmapsBySize.get(element);
        if (bitmapsWithReqDimensions == null) {
            return null;
        }

        for (Bitmap bitmap : bitmapsWithReqDimensions) {
            boolean isUnused = bitmapPoolHelper.safeCall(() -> !usedBitmaps.contains(bitmap));
            if (isUnused) {
                markBitmapAsUsed(bitmap);
                return bitmap;
            }
        }

        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    }

    @Override
    public synchronized void onLowMemory() {
        bitmapPoolHelper.safeCall(() -> {
            cache.evictAll();
            return null;
        });
    }

    @Override
    public synchronized void onTrimMemory(int level) {
        cacheUtils.handleTrimMemory(level, cache);
    }

    public Bitmap getBitmapByProperties(int width, int height, Bitmap.Config config) {
        String key = bitmapPoolHelper.generateKey(width, height, config);
        return get(key);
    }

    private void markBitmapAsFree(Bitmap bitmap) {
        bitmapPoolHelper.safeCall(() -> {
            usedBitmaps.remove(bitmap);
            return null;
        });
    }

    private void markBitmapAsUsed(Bitmap bitmap) {
        bitmapPoolHelper.safeCall(() -> {
            usedBitmaps.add(bitmap);
            return null;
        });
    }

    private void addBitmapToPool(String key, Bitmap bitmap) {
        int cacheIndex = bitmapIndex.incrementAndGet();
        String cacheKey = key + "-" + cacheIndex;

        bitmapPoolHelper.safeCall(() -> {
            cache.put(cacheKey, bitmap);
            return null;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            bitmapPoolHelper.safeCall(() -> {
                bitmapsBySize.putIfAbsent(key, new HashSet<>());
                return null;
            });
        } else {
            if (!bitmapsBySize.containsKey(key)) {
                bitmapsBySize.put(key, new HashSet<>());
            }
        }

        bitmapPoolHelper.safeCall(() -> {
            bitmapsBySize.get(key).add(bitmap);
            return null;
        });
    }
}
