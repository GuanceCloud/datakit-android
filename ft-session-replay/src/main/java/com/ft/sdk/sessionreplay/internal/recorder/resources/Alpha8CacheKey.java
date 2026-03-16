package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import androidx.collection.LruCache;



public class Alpha8CacheKey {
    private final int width;
    private final int height;
    private final long signature;

    public Alpha8CacheKey(int width, int height, long signature) {
        this.width = width;
        this.height = height;
        this.signature = signature;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getSignature() {
        return signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alpha8CacheKey that = (Alpha8CacheKey) o;
        return width == that.width && height == that.height && signature == that.signature;
    }

    @Override
    public int hashCode() {
        return 31 * width + 31 * height + (int) (31 * signature);
    }
}