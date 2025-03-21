package com.ft.sdk.sessionreplay.material.internal;

import android.graphics.drawable.Drawable;

import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.google.android.material.shape.MaterialShapeDrawable;

public class MaterialDrawableToColorMapper implements DrawableToColorMapper {

    @Override
    public Integer mapDrawableToColor(Drawable drawable, InternalLogger internalLogger) {
        if (drawable instanceof MaterialShapeDrawable) {
            return resolveMaterialShapeDrawable((MaterialShapeDrawable) drawable);
        }
        return null;
    }

    private Integer resolveMaterialShapeDrawable(MaterialShapeDrawable shapeDrawable) {
        return shapeDrawable.getFillColor() != null ? shapeDrawable.getFillColor().getDefaultColor() : null;
    }
}