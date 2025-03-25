package com.ft.sdk.sessionreplay.utils;

import static com.ft.sdk.sessionreplay.ColorConstant.ALPHA_SHIFT_ANDROID;
import static com.ft.sdk.sessionreplay.ColorConstant.MASK_COLOR;
import static com.ft.sdk.sessionreplay.ColorConstant.MAX_ALPHA_VALUE;

import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.VectorDrawable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LegacyDrawableToColorMapper implements DrawableToColorMapper {

    private static final String TAG = "LegacyDrawableToColorMapper";

    private final List<DrawableToColorMapper> extensionMappers;

    public LegacyDrawableToColorMapper(List<DrawableToColorMapper> extensionMappers) {
        this.extensionMappers = extensionMappers;
    }

    @Override
    public Integer mapDrawableToColor(Drawable drawable, InternalLogger internalLogger) {

        for (DrawableToColorMapper extensionMapper : extensionMappers) {
            Integer result = extensionMapper.mapDrawableToColor(drawable, internalLogger);
            if (result != null) {
                return result;
            }
        }

        Integer result;
        if (drawable instanceof ColorDrawable) {
            result = resolveColorDrawable((ColorDrawable) drawable);
        } else if (drawable instanceof RippleDrawable) {
            result = resolveRippleDrawable((RippleDrawable) drawable, internalLogger);
        } else if (drawable instanceof LayerDrawable) {
            result = resolveLayerDrawable((LayerDrawable) drawable, internalLogger);
        } else if (drawable instanceof InsetDrawable) {
            result = resolveInsetDrawable((InsetDrawable) drawable, internalLogger);
        } else if (drawable instanceof GradientDrawable) {
            result = resolveGradientDrawable((GradientDrawable) drawable, internalLogger);
        } else if (drawable instanceof ShapeDrawable) {
            result = resolveShapeDrawable((ShapeDrawable) drawable, internalLogger);
        } else if (drawable instanceof StateListDrawable) {
            result = resolveStateListDrawable((StateListDrawable) drawable, internalLogger);
        } else if (drawable instanceof BitmapDrawable || drawable instanceof VectorDrawable) {
            result = null; // return null without reporting them by telemetry.
        } else {
            String drawableType = drawable.getClass().getCanonicalName();
            if (drawableType == null) {
                drawableType = drawable.getClass().getName();
            }
            final String drawableTypeFinal = drawableType;
            Map<String, Object> additionalProperties = new HashMap<>();
            additionalProperties.put("replay.drawable.type", drawableType);
            internalLogger.i(TAG, "No mapper found for drawable " + drawableTypeFinal
                    + ",additionalProperties:" + additionalProperties, true);
            result = null;
        }
        return result;
    }

    protected Integer resolveColorDrawable(ColorDrawable drawable) {
        return mergeColorAndAlpha(drawable.getColor(), drawable.getAlpha());
    }

    private Integer resolveStateListDrawable(StateListDrawable drawable, InternalLogger internalLogger) {
        // Drawable.getCurrent() can return null in case <selector> doesn't have an item for the default case.
        Drawable current = drawable.getCurrent();
        return (current != null) ? mapDrawableToColor(current, internalLogger) : null;
    }

    protected Integer resolveShapeDrawable(ShapeDrawable drawable, InternalLogger internalLogger) {
        return drawable.getPaint().getColor();
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
