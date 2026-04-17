package com.ft.sdk.sessionreplay.utils;

import static com.ft.sdk.sessionreplay.ColorConstant.ALPHA_SHIFT_ANDROID;
import static com.ft.sdk.sessionreplay.ColorConstant.ALPHA_SHIFT_WEB;
import static com.ft.sdk.sessionreplay.ColorConstant.MASK_ALPHA;
import static com.ft.sdk.sessionreplay.ColorConstant.MASK_COLOR;
import static com.ft.sdk.sessionreplay.ColorConstant.WEB_COLOR_STR_LENGTH;

public class DefaultColorStringFormatter implements ColorStringFormatter {

    private static class SingletonHolder {
        private static final DefaultColorStringFormatter INSTANCE = new DefaultColorStringFormatter();
    }

    public static DefaultColorStringFormatter get() {
        return DefaultColorStringFormatter.SingletonHolder.INSTANCE;
    }


    @Override
    public String formatColorAsHexString(int color) {
        long alpha = (color & MASK_ALPHA) >>> ALPHA_SHIFT_ANDROID;
        long colorRGBA = ((long) color << ALPHA_SHIFT_WEB) | alpha;
        String hexString = Long.toHexString(MASK_COLOR & colorRGBA);
//        System.out.println("formatColorAsHexString: " + color + ", " + hexString);
        return "#" + StringUtils.padStart(hexString, WEB_COLOR_STR_LENGTH, '0');
    }

    @Override
    public String formatColorAndAlphaAsHexString(int color, int alpha) {
        long colorRGBA = ((long) color << ALPHA_SHIFT_WEB) | (long) alpha;

        // We are going to use the `Long.toString(radius)` method to produce the hex
        // representation of the color and alpha long value because it is much faster than the
        // String.format(..) approach. Based on our benchmarks, because String.format uses regular
        // expressions under the hood, this approach is at least 2 times faster.

        // We remove the original alpha value from the color by masking with 0xffffffff
        String hexString = Long.toHexString(MASK_COLOR & colorRGBA);
        return "#" + StringUtils.padStart(hexString, WEB_COLOR_STR_LENGTH, '0');
    }
}