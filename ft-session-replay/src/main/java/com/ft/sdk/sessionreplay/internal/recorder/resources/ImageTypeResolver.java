package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import com.ft.sdk.sessionreplay.utils.Utils;

public class ImageTypeResolver {

    public boolean isDrawablePII(Drawable drawable, float density) {
        boolean isNotGradient = !(drawable instanceof GradientDrawable);
        float widthInDp = Utils.densityNormalized(drawable.getIntrinsicWidth(), density);
        float heightInDp = Utils.densityNormalized(drawable.getIntrinsicHeight(), density);
        boolean widthAboveThreshold = widthInDp >= IMAGE_DIMEN_CONSIDERED_PII_IN_DP;
        boolean heightAboveThreshold = heightInDp >= IMAGE_DIMEN_CONSIDERED_PII_IN_DP;

        return isNotGradient && (widthAboveThreshold || heightAboveThreshold);
    }

    public boolean isPIIByDimensions(int width, int height) {
        return width >= IMAGE_DIMEN_CONSIDERED_PII_IN_DP || height >= IMAGE_DIMEN_CONSIDERED_PII_IN_DP;
    }

    public static final int IMAGE_DIMEN_CONSIDERED_PII_IN_DP = 100;
}
