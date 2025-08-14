package com.ft.sdk.sessionreplay.utils;

public class StringUtils {

    /**
     * Pads the beginning of this string to the specified {@code minLength} by prepending
     * the specified {@code padChar} as many times as needed.
     *
     * @param input    the original string to be padded
     * @param minLength the minimum length of the resulting padded string
     * @param padChar  the character to pad the string with
     * @return the padded string
     */
    public static String padStart(String input, int minLength, char padChar) {
        if (input.length() >= minLength) {
            return input;
        } else {
            StringBuilder sb = new StringBuilder(minLength);
            for (int i = input.length(); i < minLength; i++) {
                sb.append(padChar);
            }
            sb.append(input);
            return sb.toString();
        }
    }

    // Overloaded method without specifying padChar (default to '0')
    public static String padStart(String input, int minLength) {
        return padStart(input, minLength, '0');
    }
}


