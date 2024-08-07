package com.ft.sdk.sessionreplay;

public class TouchPosition {
    float x;
    float y;
    int id;
    private final long time;

    public TouchPosition(int id, float x, float y, long time) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.time = time;
    }

    @Override
    public String toString() {
        return "TouchPosition{" +
                "x=" + x +
                ", y=" + y +
                ", id=" + id +
                ", time=" + time +
                '}';
    }
}
