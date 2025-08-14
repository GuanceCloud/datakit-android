package com.ft.sdk.sessionreplay.utils;

public class GlobalBounds {

    private final long x;
    private final long y;
    private final long width;
    private final long height;

    public GlobalBounds(long x, long y, long width, long height) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GlobalBounds that = (GlobalBounds) o;

        if (x != that.x) return false;
        if (y != that.y) return false;
        if (width != that.width) return false;
        return height == that.height;
    }
}

