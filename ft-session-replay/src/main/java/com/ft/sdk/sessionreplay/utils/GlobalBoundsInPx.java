package com.ft.sdk.sessionreplay.utils;

public class GlobalBoundsInPx {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public GlobalBoundsInPx(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

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

