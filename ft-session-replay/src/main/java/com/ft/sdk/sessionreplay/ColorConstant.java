package com.ft.sdk.sessionreplay;

public class ColorConstant {
    public static final int ALPHA_SHIFT_ANDROID = 24;
    public static final int ALPHA_SHIFT_WEB = 8;

    public static final int MAX_ALPHA_VALUE = 0xFF;
    public static final int WEB_COLOR_STR_LENGTH = 8;

    public static final long MASK_ALPHA = 0xFF000000L;
    public static final long MASK_RGB = 0x00FFFFFFL;
    public static final long MASK_COLOR = 0xFFFFFFFFL;

    public static final int OPAQUE_ALPHA_VALUE = MAX_ALPHA_VALUE;
    public static final int PARTIALLY_OPAQUE_ALPHA_VALUE = 0x40;
}
