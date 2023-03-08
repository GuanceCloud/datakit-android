package com.ft.sdk.garble.utils;

import java.util.Collection;

/**
 *
 * @author Brandon
 */
public class StringUtils {
    public static <E extends String> String arrayToSeparateString(Collection<? extends E> list, String seperater) {
        if (list.size() > 0) {
            StringBuilder nameBuilder = new StringBuilder();

            for (String item : list) {
                nameBuilder.append(item).append(",");
            }

            nameBuilder.deleteCharAt(nameBuilder.length() - 1);

            return nameBuilder.toString();
        } else {
            return "";
        }
    }


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
