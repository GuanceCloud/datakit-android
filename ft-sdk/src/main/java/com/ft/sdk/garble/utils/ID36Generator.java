package com.ft.sdk.garble.utils;

/**
 * Generate serialized id in base 36, resets when reaching {@link #MAX_VALUE} limit, used for data marking with {@link Constants#KEY_SDK_DATA_FLAG}
 */
public class ID36Generator {
    private static final char[] BASE36_CHARS = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final long MAX_VALUE = 2821109907456L; //36 to the 8th power, greater than 1 day in microseconds
    private static final int LENGTH = 8;
    private static final int DIGIT = 36;

    private long currentValue;

    public ID36Generator() {
        this.currentValue = 0;
    }


    private String getBase36String(long tempValue) {
        char[] idChars = new char[LENGTH];
        for (int i = 7; i >= 0; i--) {
            idChars[i] = BASE36_CHARS[(int) (tempValue % DIGIT)];
            tempValue /= DIGIT;
        }
        return normalize(idChars);
    }


    public void next() {
        currentValue++; // Increment
        if (currentValue > MAX_VALUE) {
            currentValue = 0;
        }
    }

    public String getCurrentId() {
        return getBase36String(currentValue);
    }


    public String generateID() {
        String result = getCurrentId();
        next();
        return result;
    }

    private String normalize(char[] idChars) {
        StringBuilder sb = new StringBuilder();
        boolean leadingZero = true;
        for (char c : idChars) {
            if (leadingZero && c == '0') {
                continue;
            }
            leadingZero = false;
            sb.append(c);
        }
        return sb.length() == 0 ? "0" : sb.toString();
    }
}
