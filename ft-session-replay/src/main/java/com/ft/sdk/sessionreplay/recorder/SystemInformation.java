package com.ft.sdk.sessionreplay.recorder;

import com.ft.sdk.sessionreplay.utils.GlobalBounds;

public class SystemInformation {
    private final GlobalBounds screenBounds;
    private final int screenOrientation;
    private final float screenDensity;
    private final String themeColor;

    public SystemInformation(GlobalBounds screenBounds, int screenOrientation, float screenDensity, String themeColor) {
        this.screenBounds = screenBounds;
        this.screenOrientation = screenOrientation;
        this.screenDensity = screenDensity;
        this.themeColor = themeColor;
    }

    public GlobalBounds getScreenBounds() {
        return screenBounds;
    }

    public int getScreenOrientation() {
        return screenOrientation;
    }

    public float getScreenDensity() {
        return screenDensity;
    }

    public String getThemeColor() {
        return themeColor;
    }
}
