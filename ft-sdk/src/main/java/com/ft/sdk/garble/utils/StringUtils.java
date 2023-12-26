package com.ft.sdk.garble.utils;

/**
 * @author Brandon
 */
public class StringUtils {


    /**
     * 删除最后的逗号
     *
     * @param sb
     */
    public static void deleteLastCharacter(StringBuilder sb, String character) {
        if (sb == null) {
            return;
        }
        int index = sb.lastIndexOf(character);
        if (index > 0 && index == sb.length() - 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
    }
}
