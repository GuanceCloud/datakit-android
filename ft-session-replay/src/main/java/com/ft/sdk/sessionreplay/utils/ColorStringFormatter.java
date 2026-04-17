package com.ft.sdk.sessionreplay.utils;

public interface ColorStringFormatter {

    /**
     * Converts a color as an int to a standard web hexadecimal representation, as RGBA (e.g.: #A538AFFF).
     * @param color the color value (with or without alpha in the first 8 bits)
     * @return new color value as an HTML formatted hexadecimal String
     */
    String formatColorAsHexString(int color);

    /**
     * Converts a color as an int to a standard web hexadecimal representation, as RGBA (e.g.: #A538AFFF).
     * It also overrides the color's alpha channel.
     * @param color the color value (with or without alpha in the first 8 bits)
     * @param alpha the override alpha in a [0…255] range
     * @return new color value as an HTML formatted hexadecimal String
     */
    String formatColorAndAlphaAsHexString(int color, int alpha);
}