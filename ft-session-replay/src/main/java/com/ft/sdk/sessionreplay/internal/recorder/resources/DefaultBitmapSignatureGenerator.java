package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.graphics.Bitmap;

public class DefaultBitmapSignatureGenerator implements BitmapSignatureGenerator {
    private final BuildSdkVersionProvider buildSdkVersionProvider;

    public DefaultBitmapSignatureGenerator(){
        this(null);
    }

    public DefaultBitmapSignatureGenerator(BuildSdkVersionProvider buildSdkVersionProvider) {
        this.buildSdkVersionProvider = buildSdkVersionProvider != null ? buildSdkVersionProvider : BuildSdkVersionProvider.DEFAULT;
    }

    @Override
    public Long generateSignature(Bitmap bitmap) {
        if (isValidBitmap(bitmap)) {
            return computeHash(bitmap);
        } else {
            return null;
        }
    }

    private boolean isValidBitmap(Bitmap bitmap) {
        // HARDWARE bitmaps don't support getPixel() - they exist only in GPU memory
        boolean isHardwareBitmap = buildSdkVersionProvider.isAtLeastO() &&
                bitmap.getConfig() == Bitmap.Config.HARDWARE;
        return !bitmap.isRecycled() &&
                bitmap.getWidth() > 0 &&
                bitmap.getHeight() > 0 &&
                !isHardwareBitmap;
    }

    /**
     * Computes a hash by sampling pixels in an evenly-spaced grid across the bitmap.
     *
     * Instead of reading every pixel (which would be slow for large bitmaps), we sample
     * up to [SAMPLES_PER_AXIS] x [SAMPLES_PER_AXIS] pixels spread evenly across the image.
     * This gives us a representative fingerprint while keeping computation fast.
     *
     * Uses a polynomial rolling hash: hash = hash * 31 + value
     * This is the same approach used by Java's String.hashCode() and provides good distribution.
     */
    private long computeHash(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Start with prime number to reduce collisions for small inputs
        long hash = HASH_PRIME_SEED;

        // Include dimensions in hash - same pixels at different sizes should have different signatures
        hash = HASH_MULTIPLIER * hash + width;
        hash = HASH_MULTIPLIER * hash + height;

        // Calculate stride to sample evenly across the bitmap
        // For a 100px wide bitmap with 16 samples, stride = 6, sampling at x = 0, 6, 12, 18, ...
        int strideX = Math.max(width / SAMPLES_PER_AXIS, 1);
        int strideY = Math.max(height / SAMPLES_PER_AXIS, 1);

        for (int x = 0; x < width; x += strideX) {
            for (int y = 0; y < height; y += strideY) {
                int pixelColor = bitmap.getPixel(x, y);
                hash = HASH_MULTIPLIER * hash + pixelColor;
            }
        }

        return hash;
    }

    private static final long HASH_PRIME_SEED = 17L;
    private static final long HASH_MULTIPLIER = 31L;
    private static final int SAMPLES_PER_AXIS = 16;
}