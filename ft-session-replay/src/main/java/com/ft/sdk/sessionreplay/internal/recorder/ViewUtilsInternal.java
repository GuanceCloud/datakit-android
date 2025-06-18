package com.ft.sdk.sessionreplay.internal.recorder;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewStub;
import android.widget.Toolbar;

import androidx.appcompat.widget.ActionBarContextView;

import com.ft.sdk.sessionreplay.internal.recorder.resources.DefaultImageWireframeHelper;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.Utils;

import java.util.HashSet;
import java.util.Set;

public class ViewUtilsInternal {

    private final Set<Integer> systemViewIds = new HashSet<>();

    public ViewUtilsInternal() {
        systemViewIds.add(android.R.id.navigationBarBackground);
        systemViewIds.add(android.R.id.statusBarBackground);
    }

    public boolean isNotVisible(View view) {
        return !view.isShown() || view.getWidth() <= 0 || view.getHeight() <= 0;
    }

    @SuppressLint("RestrictedApi")
    // ActionBarContextView is public, but has @RestrictTo(LIBRARY_GROUP_PREFIX)
    public boolean isSystemNoise(View view) {
        return systemViewIds.contains(view.getId()) || view instanceof ViewStub || view instanceof ActionBarContextView;
    }

    public boolean isToolbar(View view) {
        return Toolbar.class.isAssignableFrom(view.getClass()) ||
                android.widget.Toolbar.class.isAssignableFrom(view.getClass());
    }

    public GlobalBounds resolveDrawableBounds(View view, Drawable drawable, float pixelsDensity) {
        int[] coordinates = new int[2];
        view.getLocationOnScreen(coordinates);
        long x = (long) Utils.densityNormalized(coordinates[0], pixelsDensity);
        long y = (long) Utils.densityNormalized(coordinates[1], pixelsDensity);
        long width = (long) Utils.densityNormalized(drawable.getIntrinsicWidth(), pixelsDensity);
        long height = (long) Utils.densityNormalized(drawable.getIntrinsicHeight(), pixelsDensity);
        return new GlobalBounds(x, y, height, width);
    }

    public GlobalBounds resolveCompoundDrawableBounds(
            View view,
            Drawable drawable,
            float pixelsDensity,
            DefaultImageWireframeHelper.CompoundDrawablePositions position
    ) {
        int[] coordinates = new int[2];
        view.getLocationOnScreen(coordinates);

        long viewXPosition = (long) Utils.densityNormalized(coordinates[0], pixelsDensity);
        long viewYPosition = (long) Utils.densityNormalized(coordinates[1], pixelsDensity);
        long drawableWidth = (long) Utils.densityNormalized(drawable.getIntrinsicWidth(), pixelsDensity);
        long drawableHeight = (long) Utils.densityNormalized(drawable.getIntrinsicHeight(), pixelsDensity);
        long viewWidth = (long) Utils.densityNormalized(view.getWidth(), pixelsDensity);
        long viewHeight = (long) Utils.densityNormalized(view.getHeight(), pixelsDensity);
        long viewPaddingStart = (long) Utils.densityNormalized(view.getPaddingStart(), pixelsDensity);
        long viewPaddingTop = (long) Utils.densityNormalized(view.getPaddingTop(), pixelsDensity);
        long viewPaddingBottom = (long) Utils.densityNormalized(view.getPaddingBottom(), pixelsDensity);
        long viewPaddingEnd = (long) Utils.densityNormalized(view.getPaddingEnd(), pixelsDensity);
        long xPosition = 0;
        long yPosition = 0;

        switch (position) {
            case LEFT:
                xPosition = viewPaddingStart;
                yPosition = getCenterVerticalOffset(viewHeight, drawableHeight);
                break;
            case TOP:
                xPosition = getCenterHorizontalOffset(viewWidth, drawableWidth);
                yPosition = viewPaddingTop;
                break;
            case RIGHT:
                xPosition = viewWidth - (drawableWidth + viewPaddingEnd);
                yPosition = getCenterVerticalOffset(viewHeight, drawableHeight);
                break;
            case BOTTOM:
                xPosition = getCenterHorizontalOffset(viewWidth, drawableWidth);
                yPosition = viewHeight - (drawableHeight + viewPaddingBottom);
                break;
        }

        xPosition += viewXPosition;
        yPosition += viewYPosition;

        return new GlobalBounds(xPosition, yPosition, drawableHeight, drawableWidth);
    }

    private long getCenterHorizontalOffset(long viewWidth, long drawableWidth) {
        return (viewWidth / 2) - (drawableWidth / 2);
    }

    private long getCenterVerticalOffset(long viewHeight, long drawableHeight) {
        return (viewHeight / 2) - (drawableHeight / 2);
    }
}
