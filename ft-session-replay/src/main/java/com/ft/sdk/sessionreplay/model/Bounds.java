package com.ft.sdk.sessionreplay.model;

public class Bounds {
    private final long x;
    private final long y;
    private final long width;
    private final long height;

    public Bounds(long x, long y, long width, long height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    public long getWidth() {
        return width;
    }

    public long getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "Bounds{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
