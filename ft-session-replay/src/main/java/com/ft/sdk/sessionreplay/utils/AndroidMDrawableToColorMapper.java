package com.ft.sdk.sessionreplay.utils;

import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.ft.sdk.sessionreplay.R;

public class AndroidMDrawableToColorMapper extends LegacyDrawableToColorMapper {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected Integer resolveRippleDrawable(RippleDrawable drawable, InternalLogger internalLogger) {
        int maskLayerIndex = drawable.findIndexByLayerId(R.id.mask);
        return resolveLayerDrawable(drawable, internalLogger, (idx, childDrawable) -> idx != maskLayerIndex);
    }

    @Override
    protected Integer resolveInsetDrawable(InsetDrawable drawable, InternalLogger internalLogger) {
        Drawable innerDrawable = drawable.getDrawable();
        return innerDrawable != null ? mapDrawableToColor(innerDrawable, internalLogger) : null;
    }
}
