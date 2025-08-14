package com.ft.sdk.sessionreplay.internal.utils;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ft.sdk.sessionreplay.utils.Utils;

public class ImageViewUtils {

    private static class SingletonHolder {
        private static final ImageViewUtils INSTANCE = new ImageViewUtils();
    }

    public static ImageViewUtils get() {
        return ImageViewUtils.SingletonHolder.INSTANCE;
    }

    @NonNull
    public Rect resolveParentRectAbsPosition(@NonNull ImageView view) {
        int[] coords = new int[2];
        // this will always have size >= 2
        view.getLocationOnScreen(coords);

        return new Rect(
                coords[0],
                coords[1],
                coords[0] + view.getWidth(),
                coords[1] + view.getHeight()
        );
    }

    @NonNull
    public Rect calculateClipping(
            @NonNull Rect parentRect,
            @NonNull Rect childRect,
            float density
    ) {
        int left = childRect.left < parentRect.left ? parentRect.left - childRect.left : 0;
        int top = childRect.top < parentRect.top ? parentRect.top - childRect.top : 0;
        int right = childRect.right > parentRect.right ? childRect.right - parentRect.right : 0;
        int bottom = childRect.bottom > parentRect.bottom ? childRect.bottom - parentRect.bottom : 0;

        return new Rect(
                Utils.densityNormalized(left, density),
                Utils.densityNormalized(top, density),
                Utils.densityNormalized(right, density),
                Utils.densityNormalized(bottom, density)
        );
    }

    public Rect resolveContentRectWithScaling(ImageView imageView, Drawable drawable) {
        return resolveContentRectWithScaling(imageView, drawable, null);
    }

    @NonNull
    public Rect resolveContentRectWithScaling(
            @NonNull ImageView view,
            @NonNull Drawable drawable,
            @Nullable ImageView.ScaleType customScaleType
    ) {
        int drawableWidthPx = drawable.getIntrinsicWidth();
        int drawableHeightPx = drawable.getIntrinsicHeight();

        Rect parentRect = resolveParentRectAbsPosition(view);

        Rect childRect = new Rect(
                0,
                0,
                drawableWidthPx,
                drawableHeightPx
        );

        Rect resultRect;
        ImageView.ScaleType scaleType = (customScaleType != null) ? customScaleType : view.getScaleType();
        switch (scaleType) {
            case FIT_START:
                Rect contentRectFitStart = scaleRectToFitParent(parentRect, childRect);
                resultRect = positionRectAtStart(parentRect, contentRectFitStart);
                break;
            case FIT_END:
                Rect contentRectFitEnd = scaleRectToFitParent(parentRect, childRect);
                resultRect = positionRectAtEnd(parentRect, contentRectFitEnd);
                break;
            case FIT_CENTER:
                Rect contentRectFitCenter = scaleRectToFitParent(parentRect, childRect);
                resultRect = positionRectInCenter(parentRect, contentRectFitCenter);
                break;
            case CENTER_INSIDE:
                Rect contentRectCenterInside = scaleRectToCenterInsideParent(parentRect, childRect);
                resultRect = positionRectInCenter(parentRect, contentRectCenterInside);
                break;
            case CENTER:
                resultRect = positionRectInCenter(parentRect, childRect);
                break;
            case CENTER_CROP:
                Rect contentRectCenterCrop = scaleRectToCenterCrop(parentRect, childRect);
                resultRect = positionRectInCenter(parentRect, contentRectCenterCrop);
                break;
            case FIT_XY:
            case MATRIX:
            default:
                resultRect = new Rect(
                        parentRect.left,
                        parentRect.top,
                        parentRect.right,
                        parentRect.bottom
                );
                break;
        }

        return resultRect;
    }

    @NonNull
    private Rect scaleRectToCenterInsideParent(@NonNull Rect parentRect, @NonNull Rect childRect) {
        // it already fits inside the parent
        if (parentRect.width() > childRect.width() && parentRect.height() > childRect.height()) {
            return childRect;
        }

        float scaleX = (float) parentRect.width() / childRect.width();
        float scaleY = (float) parentRect.height() / childRect.height();
        float scaleFactor = Math.min(scaleX, scaleY);

        // center inside doesn't enlarge, it only reduces
        if (scaleFactor >= 1F) {
            scaleFactor = 1F;
        }

        int newWidth = (int) (childRect.width() * scaleFactor);
        int newHeight = (int) (childRect.height() * scaleFactor);

        Rect resultRect = new Rect();
        resultRect.left = parentRect.left;
        resultRect.top = parentRect.top;
        resultRect.right = resultRect.left + newWidth;
        resultRect.bottom = resultRect.top + newHeight;
        return resultRect;
    }

    @NonNull
    private Rect scaleRectToCenterCrop(@NonNull Rect parentRect, @NonNull Rect childRect) {
        float scaleX = (float) parentRect.width() / childRect.width();
        float scaleY = (float) parentRect.height() / childRect.height();
        float scaleFactor = Math.max(scaleX, scaleY);

        int newWidth = (int) (childRect.width() * scaleFactor);
        int newHeight = (int) (childRect.height() * scaleFactor);

        Rect resultRect = new Rect();
        resultRect.left = 0;
        resultRect.top = 0;
        resultRect.right = newWidth;
        resultRect.bottom = newHeight;
        return resultRect;
    }

    @NonNull
    private Rect scaleRectToFitParent(@NonNull Rect parentRect, @NonNull Rect childRect) {
        float scaleX = (float) parentRect.width() / childRect.width();
        float scaleY = (float) parentRect.height() / childRect.height();
        float scaleFactor = Math.min(scaleX, scaleY);

        int newWidth = (int) (childRect.width() * scaleFactor);
        int newHeight = (int) (childRect.height() * scaleFactor);

        Rect resultRect = new Rect();
        resultRect.left = 0;
        resultRect.top = 0;
        resultRect.right = newWidth;
        resultRect.bottom = newHeight;
        return resultRect;
    }

    @NonNull
    private Rect positionRectInCenter(@NonNull Rect parentRect, @NonNull Rect childRect) {
        int centerXParentPx = parentRect.centerX();
        int centerYParentPx = parentRect.centerY();
        int childRectWidthPx = childRect.width();
        int childRectHeightPx = childRect.height();

        Rect resultRect = new Rect();
        resultRect.left = centerXParentPx - (childRectWidthPx / 2);
        resultRect.top = centerYParentPx - (childRectHeightPx / 2);
        resultRect.right = resultRect.left + childRectWidthPx;
        resultRect.bottom = resultRect.top + childRectHeightPx;
        return resultRect;
    }

    @NonNull
    private Rect positionRectAtStart(@NonNull Rect parentRect, @NonNull Rect childRect) {
        int childRectWidthPx = childRect.width();
        int childRectHeightPx = childRect.height();

        Rect resultRect = new Rect();
        resultRect.left = parentRect.left;
        resultRect.top = parentRect.top;
        resultRect.right = resultRect.left + childRectWidthPx;
        resultRect.bottom = resultRect.top + childRectHeightPx;
        return resultRect;
    }

    @NonNull
    private Rect positionRectAtEnd(@NonNull Rect parentRect, @NonNull Rect childRect) {
        int childRectWidthPx = childRect.width();
        int childRectHeightPx = childRect.height();

        Rect resultRect = new Rect();
        resultRect.right = parentRect.right;
        resultRect.bottom = parentRect.bottom;
        resultRect.left = parentRect.right - childRectWidthPx;
        resultRect.top = parentRect.bottom - childRectHeightPx;
        return resultRect;
    }
}
