package com.ft.sdk.sessionreplay.resources;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DefaultDrawableCopier implements DrawableCopier {

    @Override
    @Nullable
    public Drawable copy(@NonNull Drawable originalDrawable, @NonNull Resources resources) {
        return originalDrawable.getConstantState() != null
                ? originalDrawable.getConstantState().newDrawable(resources)
                : null;
    }
}