package com.ft.plugin.garble;

public class FTStringUtils {

    /**
     * Capitalize the first letter
     *
     * @param inputString
     * @return
     */
    public static String captitalizedString(String inputString) {
        if (inputString != null && !inputString.isEmpty()) {
            return inputString.substring(0, 1).toUpperCase() + inputString.substring(1);

        } else {
            return inputString;
        }
    }
}
