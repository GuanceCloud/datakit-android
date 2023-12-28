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


    /***
     * 对数据进行脱敏
     * @param str
     * @return
     */
    public static String maskHalfCharacter(String str) {
        StringBuilder sb = new StringBuilder();
        int length = str.length();
        for (int i = 0; i < length; i++) {
            if (i > length / 2) {
                sb.append(str.charAt(i));
            } else {
                sb.append("*");
            }
        }
        return sb.toString();
    }
}
