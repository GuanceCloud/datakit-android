package com.ft.sdk.sessionreplay.utils;

public class Utils {

    public static double densityNormalized(long size, float density) {
        if (density == 0f) {
            return size;
        }
        return size / density;
    }

    public static double densityNormalized(float size, float density) {
        if (density == 0f) {
            return size;
        }
        return size / density;
    }

    public static double densityNormalized(int value, float density) {
        if (density == 0f) {
            return value;
        }
        return value / density;
    }

    public static CharSequence[] convertIntArrayToCharSequenceArray(int[] intArray) {
        if (intArray == null) {
            return null;
        }

        CharSequence[] charSequenceArray = new CharSequence[intArray.length];

        for (int i = 0; i < intArray.length; i++) {
            charSequenceArray[i] = String.valueOf(intArray[i]);
        }

        return charSequenceArray;
    }
}
