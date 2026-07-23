package com.ft.plugin.garble;

import java.util.Locale;

public class FTStringUtils {

    /**
     * Capitalize the first letter
     *
     * @param inputString
     * @return
     */
    public static String captitalizedString(String inputString) {
        if (inputString != null && !inputString.isEmpty()) {
            return inputString.substring(0, 1).toUpperCase(Locale.ROOT) + inputString.substring(1);

        } else {
            return inputString;
        }
    }
}
