package com.ft.sdk.sessionreplay.internal.recorder.resources;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import com.ft.sdk.sessionreplay.utils.Utils;

public class ImageTypeResolver {

    public static boolean isDrawablePII(Drawable drawable, float density) {
        boolean isNotGradient = !(drawable instanceof GradientDrawable);
        float widthInDp = Utils.densityNormalized(drawable.getIntrinsicWidth(), density);
        float heightInDp = Utils.densityNormalized(drawable.getIntrinsicHeight(), density);
        boolean widthAboveThreshold = widthInDp >= Companion.IMAGE_DIMEN_CONSIDERED_PII_IN_DP;
        boolean heightAboveThreshold = heightInDp >= Companion.IMAGE_DIMEN_CONSIDERED_PII_IN_DP;

        return isNotGradient && (widthAboveThreshold || heightAboveThreshold);
    }

    public static final class Companion {
        // material design icon size is up to 48x48, but use 100 to match more images
        public static final int IMAGE_DIMEN_CONSIDERED_PII_IN_DP = 100;
    }
}
