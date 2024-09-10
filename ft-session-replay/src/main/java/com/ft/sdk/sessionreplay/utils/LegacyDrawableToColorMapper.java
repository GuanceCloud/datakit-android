package com.ft.sdk.sessionreplay.utils;

import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class LegacyDrawableToColorMapper implements DrawableToColorMapper {

    private static final String TAG = "LegacyDrawableToColorMapper";
    private static final long MASK_COLOR = 0x00FFFFFFL;
    private static final int ALPHA_SHIFT_ANDROID = 24;
    protected static final int MAX_ALPHA_VALUE = 255;

    @Override
    public Integer mapDrawableToColor(Drawable drawable, InternalLogger internalLogger) {
        Integer result;
        if (drawable instanceof ColorDrawable ) {
            result = resolveColorDrawable((ColorDrawable) drawable);
        } else if (drawable instanceof RippleDrawable) {
            result = resolveRippleDrawable((RippleDrawable) drawable, internalLogger);
        } else if (drawable instanceof LayerDrawable) {
            result = resolveLayerDrawable((LayerDrawable) drawable, internalLogger);
        } else if (drawable instanceof InsetDrawable) {
            result = resolveInsetDrawable((InsetDrawable) drawable, internalLogger);
        } else if (drawable instanceof GradientDrawable) {
            result = resolveGradientDrawable((GradientDrawable) drawable, internalLogger);
        } else {
            String drawableType = drawable.getClass().getCanonicalName();
            if (drawableType == null) {
                drawableType = drawable.getClass().getName();
            }
            final String drawableTypeFinal = drawableType;
            Map<String, Object> additionalProperties = new HashMap<>();
            additionalProperties.put("replay.drawable.type", drawableType);
            internalLogger.i(TAG, "No mapper found for drawable " + drawableTypeFinal
                    + ",additionalProperties:" + additionalProperties,true);
            result = null;
        }
        return result;
    }

    protected Integer resolveColorDrawable(ColorDrawable drawable) {
        return mergeColorAndAlpha(drawable.getColor(), drawable.getAlpha());
    }

    protected Integer resolveRippleDrawable(RippleDrawable drawable, InternalLogger internalLogger) {
        return resolveLayerDrawable(drawable, internalLogger);
    }

    protected Integer resolveLayerDrawable(LayerDrawable drawable, InternalLogger internalLogger) {
        return resolveLayerDrawable(drawable, internalLogger, (idx, childDrawable) -> true);
    }

    protected Integer resolveLayerDrawable(LayerDrawable drawable, InternalLogger internalLogger, DrawablePredicate predicate) {
        for (int i = 0; i < drawable.getNumberOfLayers(); i++) {
            Drawable childDrawable = drawable.getDrawable(i);
            if (childDrawable != null && predicate.test(i, childDrawable)) {
                Integer color = mapDrawableToColor(childDrawable, internalLogger);
                if (color != null) {
                    return color;
                }
            }
        }
        return null;
    }

    protected Integer resolveGradientDrawable(GradientDrawable drawable, InternalLogger internalLogger) {
        Paint fillPaint = null;
        try {
            fillPaint = (Paint) fillPaintField.get(drawable);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            internalLogger.e(TAG, "Unable to read GradientDrawable.mFillPaint field through reflection", e);
        }

        if (fillPaint == null) return null;

        int fillColor = fillPaint.getColor();
        int fillAlpha = (fillPaint.getAlpha() * drawable.getAlpha()) / MAX_ALPHA_VALUE;

        if (fillAlpha == 0) {
            return null;
        } else {
            // TODO RUM-3469 resolve other color filter types
            return mergeColorAndAlpha(fillColor, fillAlpha);
        }
    }

    protected Integer resolveInsetDrawable(InsetDrawable drawable, InternalLogger internalLogger) {
        return null;
    }

    protected int mergeColorAndAlpha(int color, int alpha) {
        return (int) (((long) color & MASK_COLOR) | ((long) alpha << ALPHA_SHIFT_ANDROID));
    }

    @FunctionalInterface
    protected interface DrawablePredicate {
        boolean test(int idx, Drawable drawable);
    }

    protected static final Field fillPaintField;

    static {
        Field tempField = null;
        try {
            tempField = GradientDrawable.class.getDeclaredField("mFillPaint");
            tempField.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException | NullPointerException e) {
            // Ignored: if the field is not accessible, fillPaintField will remain null
        }
        fillPaintField = tempField;
    }
}
