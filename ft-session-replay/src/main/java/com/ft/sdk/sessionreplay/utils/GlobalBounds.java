package com.ft.sdk.sessionreplay.utils;

/**
 * Immutable view bounds normalized by screen density.
 */
public class GlobalBounds {

    private final long x;
    private final long y;
    private final long width;
    private final long height;

    /**
     * Creates normalized bounds.
     *
     * @param x horizontal position
     * @param y vertical position
     * @param width width
     * @param height height
     */
    public GlobalBounds(long x, long y, long width, long height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the horizontal position.
     */
    public long getX() {
        return x;
    }

    /**
     * Returns the vertical position.
     */
    public long getY() {
        return y;
    }

    /**
     * Returns the width.
     */
    public long getWidth() {
        return width;
    }

    /**
     * Returns the height.
     */
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
