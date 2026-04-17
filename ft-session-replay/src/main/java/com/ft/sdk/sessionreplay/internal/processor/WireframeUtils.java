package com.ft.sdk.sessionreplay.internal.processor;

import com.ft.sdk.sessionreplay.model.ImageWireframe;
import com.ft.sdk.sessionreplay.model.PlaceholderWireframe;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.TextWireframe;
import com.ft.sdk.sessionreplay.model.WebviewWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.model.WireframeClip;

import java.util.List;

public class WireframeUtils {
    private final BoundsUtils boundsUtils;

    public WireframeUtils() {
        this(new BoundsUtils());
    }

    public WireframeUtils(BoundsUtils boundsUtils) {
        this.boundsUtils = boundsUtils;
    }

    public WireframeClip resolveWireframeClip(
            Wireframe wireframe,
            List<Wireframe> parents
    ) {
        WireframeClip previousClip = clip(wireframe);
        long clipTop = previousClip != null ? previousClip.getTop() : 0L;
        long clipLeft = previousClip != null ? previousClip.getLeft() : 0L;
        long clipRight = previousClip != null ? previousClip.getRight() : 0L;
        long clipBottom = previousClip != null ? previousClip.getBottom() : 0L;
        WireframeBounds wireframeBounds = boundsUtils.resolveBounds(wireframe);

        for (Wireframe parent : parents) {
            WireframeBounds parentBounds = boundsUtils.resolveBounds(parent);
            clipTop = Math.max(parentBounds.getTop() - wireframeBounds.getTop(), clipTop);
            clipBottom = Math.max(wireframeBounds.getBottom() - parentBounds.getBottom(), clipBottom);
            clipLeft = Math.max(parentBounds.getLeft() - wireframeBounds.getLeft(), clipLeft);
            clipRight = Math.max(wireframeBounds.getRight() - parentBounds.getRight(), clipRight);
        }

        if (clipTop > 0 || clipBottom > 0 || clipLeft > 0 || clipRight > 0) {
            return new WireframeClip(clipTop, clipBottom, clipLeft, clipRight);
        } else {
            return null;
        }
    }

    public boolean checkWireframeIsCovered(
            Wireframe wireframe,
            List<Wireframe> topWireframes
    ) {
        WireframeBounds wireframeBounds = boundsUtils.resolveBounds(wireframe);

        for (Wireframe topWireframe : topWireframes) {
            WireframeBounds topBounds = boundsUtils.resolveBounds(topWireframe);
            if (boundsUtils.isCovering(topBounds, wireframeBounds) && topWireframe.hasOpaqueBackground()) {
                return true;
            }
        }

        return false;
    }

    public boolean checkWireframeIsValid(Wireframe wireframe) {
        WireframeBounds wireframeBounds = boundsUtils.resolveBounds(wireframe);

        return wireframeBounds.getWidth() > 0 &&
                wireframeBounds.getHeight() > 0 &&
                !(wireframe instanceof ShapeWireframe &&
                        ((ShapeWireframe) wireframe).getShapeStyle() == null &&
                        ((ShapeWireframe) wireframe).getBorder() == null);
    }

    private WireframeClip clip(Wireframe wireframe) {
        if (wireframe instanceof ShapeWireframe) {
            return ((ShapeWireframe) wireframe).getClip();
        } else if (wireframe instanceof TextWireframe) {
            return ((TextWireframe) wireframe).getClip();
        } else if (wireframe instanceof ImageWireframe) {
            return ((ImageWireframe) wireframe).getClip();
        } else if (wireframe instanceof PlaceholderWireframe) {
            return ((PlaceholderWireframe) wireframe).getClip();
        } else if (wireframe instanceof WebviewWireframe) {
            return ((WebviewWireframe) wireframe).getClip();
        } else {
            return null;
        }
    }
}
