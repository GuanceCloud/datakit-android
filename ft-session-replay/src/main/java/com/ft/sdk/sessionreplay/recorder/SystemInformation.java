package com.ft.sdk.sessionreplay.recorder;

import com.ft.sdk.sessionreplay.utils.GlobalBounds;

/**
 * Device and screen data available while mapping Session Replay wireframes.
 */
public class SystemInformation {
    private final GlobalBounds screenBounds;
    private final int screenOrientation;
    private final float screenDensity;
    private final String themeColor;

    /**
     * Creates a system information snapshot.
     *
     * @param screenBounds screen bounds normalized by density
     * @param screenOrientation current screen orientation
     * @param screenDensity current screen density
     * @param themeColor current theme color as a hexadecimal string, when available
     */
    public SystemInformation(GlobalBounds screenBounds, int screenOrientation, float screenDensity, String themeColor) {
        this.screenBounds = screenBounds;
        this.screenOrientation = screenOrientation;
        this.screenDensity = screenDensity;
        this.themeColor = themeColor;
    }

    /**
     * Returns the screen bounds normalized by density.
     */
    public GlobalBounds getScreenBounds() {
        return screenBounds;
    }

    /**
     * Returns the current screen orientation.
     */
    public int getScreenOrientation() {
        return screenOrientation;
    }

    /**
     * Returns the current screen density.
     */
    public float getScreenDensity() {
        return screenDensity;
    }

    /**
     * Returns the current theme color, when available.
     */
    public String getThemeColor() {
        return themeColor;
    }
}
