package com.ft.sdk.sessionreplay.resources;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public interface DrawableCopier {

    /**
     * Called to copy the drawable.
     *
     * @param originalDrawable the original drawable to copy
     * @param resources resources of the view.
     * @return New copied drawable.
     */
    Drawable copy(@NonNull Drawable originalDrawable, @NonNull Resources resources);
}