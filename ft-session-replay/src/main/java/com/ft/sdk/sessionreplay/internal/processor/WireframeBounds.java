package com.ft.sdk.sessionreplay.internal.processor;

public class WireframeBounds {
    private final long left;
    private final long right;
    private final long top;
    private final long bottom;
    private final long width;
    private final long height;

    public WireframeBounds(long left, long right, long top, long bottom, long width, long height) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.width = width;
        this.height = height;
    }

    public long getLeft() {
        return left;
    }

    public long getRight() {
        return right;
    }

    public long getTop() {
        return top;
    }

    public long getBottom() {
        return bottom;
    }

    public long getWidth() {
        return width;
    }

    public long getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "WireframeBounds{" +
                "left=" + left +
                ", right=" + right +
                ", top=" + top +
                ", bottom=" + bottom +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
