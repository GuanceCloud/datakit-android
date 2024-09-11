package com.ft.sdk.sessionreplay.utils;

import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RequiresApi(Build.VERSION_CODES.Q)
public class AndroidQDrawableToColorMapper extends AndroidMDrawableToColorMapper {
    private static final String TAG = "AndroidQDrawableToColor";

    @Override
    protected Integer resolveGradientDrawable(GradientDrawable drawable, InternalLogger internalLogger) {
        Paint fillPaint;
        try {
            fillPaint = (Paint) fillPaintField.get(drawable);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            fillPaint = null;
        }

        if (fillPaint == null) {
            return null;
        }

        ColorFilter colorFilter = fillPaint.getColorFilter();
        int fillColor = fillPaint.getColor();
        int fillAlpha = (fillPaint.getAlpha() * drawable.getAlpha()) / MAX_ALPHA_VALUE;

        if (fillAlpha == 0) {
            return null;
        } else {
            if (colorFilter != null) {
                fillColor = resolveBlendModeColorFilter(fillColor, colorFilter, internalLogger);
            }
            return mergeColorAndAlpha(fillColor, fillAlpha);
        }
    }

    private int resolveBlendModeColorFilter(int fillColor, ColorFilter colorFilter, InternalLogger internalLogger) {
        if (colorFilter instanceof BlendModeColorFilter) {
            BlendModeColorFilter blendModeColorFilter = (BlendModeColorFilter) colorFilter;
            BlendMode mode = blendModeColorFilter.getMode();
            if (blendModesReturningBlendColor.contains(mode)) {
                return blendModeColorFilter.getColor();
            } else if (blendModesReturningOriginalColor.contains(mode)) {
                return fillColor;
            } else {
                internalLogger.i(TAG, "No mapper found for gradient blend mode " + mode + ","
                        + Map.of("replay.gradient.blend_mode", mode),true);
                return fillColor;
            }
        } else {
            internalLogger.i(TAG, "No mapper found for gradient color filter " + colorFilter.getClass() + ","
                    + Map.of("replay.gradient.filter_type", colorFilter.getClass().getCanonicalName()),true);
            return fillColor;
        }
    }

    private static final List<BlendMode> blendModesReturningBlendColor = Arrays.asList(
            BlendMode.SRC,
            BlendMode.SRC_ATOP,
            BlendMode.SRC_IN,
            BlendMode.SRC_OUT,
            BlendMode.SRC_OVER
    );

    private static final List<BlendMode> blendModesReturningOriginalColor = Arrays.asList(
            BlendMode.DST,
            BlendMode.DST_ATOP,
            BlendMode.DST_IN,
            BlendMode.DST_OUT,
            BlendMode.DST_OVER
    );
}
