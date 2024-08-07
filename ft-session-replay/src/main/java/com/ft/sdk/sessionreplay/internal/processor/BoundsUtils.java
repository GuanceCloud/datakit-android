package com.ft.sdk.sessionreplay.internal.processor;

import com.ft.sdk.sessionreplay.model.ImageWireframe;
import com.ft.sdk.sessionreplay.model.PlaceholderWireframe;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.TextWireframe;
import com.ft.sdk.sessionreplay.model.WebviewWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.model.WireframeClip;

public class BoundsUtils {

    public WireframeBounds resolveBounds(Wireframe wireframe) {
        if (wireframe instanceof ShapeWireframe) {
            return bounds((ShapeWireframe) wireframe);
        } else if (wireframe instanceof TextWireframe) {
            return bounds((TextWireframe) wireframe);
        } else if (wireframe instanceof ImageWireframe) {
            return bounds((ImageWireframe) wireframe);
        } else if (wireframe instanceof PlaceholderWireframe) {
            return bounds((PlaceholderWireframe) wireframe);
        } else if (wireframe instanceof WebviewWireframe) {
            return bounds((WebviewWireframe) wireframe);
        } else {
            throw new IllegalArgumentException("Unknown wireframe type");
        }
    }

    public boolean isCovering(WireframeBounds top, WireframeBounds bottom) {
        return top.getLeft() <= bottom.getLeft() &&
                top.getRight() >= bottom.getRight() &&
                top.getTop() <= bottom.getTop() &&
                top.getBottom() >= bottom.getBottom();
    }

    private WireframeBounds bounds(ShapeWireframe wireframe) {
        return resolveBounds(wireframe.getX(), wireframe.getY(), wireframe.getWidth(), wireframe.getHeight(), wireframe.getClip());
    }

    private WireframeBounds bounds(TextWireframe wireframe) {
        return resolveBounds(wireframe.getX(), wireframe.getY(), wireframe.getWidth(), wireframe.getHeight(), wireframe.getClip());
    }

    private WireframeBounds bounds(ImageWireframe wireframe) {
        return resolveBounds(wireframe.getX(), wireframe.getY(), wireframe.getWidth(), wireframe.getHeight(), wireframe.getClip());
    }

    private WireframeBounds bounds(PlaceholderWireframe wireframe) {
        return resolveBounds(wireframe.getX(), wireframe.getY(), wireframe.getWidth(), wireframe.getHeight(), wireframe.getClip());
    }

    private WireframeBounds bounds(WebviewWireframe wireframe) {
        return resolveBounds(wireframe.getX(), wireframe.getY(), wireframe.getWidth(), wireframe.getHeight(), wireframe.getClip());
    }

    private WireframeBounds resolveBounds(long x, long y, long width, long height, WireframeClip clip) {
        long left = x + (clip != null ? clip.getLeft() : 0);
        long right = x + width - (clip != null ? clip.getRight() : 0);
        long top = y + (clip != null ? clip.getTop() : 0);
        long bottom = y + height - (clip != null ? clip.getBottom() : 0);
        return new WireframeBounds(left, right, top, bottom, width, height);
    }
}
