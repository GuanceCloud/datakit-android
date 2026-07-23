package com.ft.sdk.sessionreplay.utils;

/**
 * Immutable view bounds in raw pixels.
 */
public class GlobalBoundsInPx {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    /**
     * Creates pixel bounds.
     *
     * @param x horizontal position in pixels
     * @param y vertical position in pixels
     * @param width width in pixels
     * @param height height in pixels
     */
    public GlobalBoundsInPx(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the horizontal position in pixels.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the vertical position in pixels.
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the width in pixels.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height in pixels.
     */
    public int getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GlobalBoundsInPx that = (GlobalBoundsInPx) o;

        if (x != that.x) return false;
        if (y != that.y) return false;
        if (width != that.width) return false;
        return height == that.height;
    }
}
