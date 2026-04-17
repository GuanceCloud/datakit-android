package com.ft.sdk.sessionreplay.utils;

public class Utils {

    public static long densityNormalized(long size, float density) {
        if (density == 0f) {
            return size;
        }
        return (long) (size / density);
    }

    public static long densityNormalized(float size, float density) {
        if (density == 0f) {
            return (long) size;
        }
        return (long) (size / density);
    }

    public static int densityNormalized(int value, float density) {
        if (density == 0f) {
            return value;
        }
        return (int) (value / density);
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
