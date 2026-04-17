package com.ft.sdk.sessionreplay.utils;

import java.util.Map;

public class MapUtils {
    // --- int ---
    public static int getInt(Map<String, Object> map, String key) {
        return getInt(map, key, 0);
    }

    public static int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        return (value instanceof Number) ? ((Number) value).intValue() : defaultValue;
    }

    // --- long ---
    public static long getLong(Map<String, Object> map, String key) {
        return getLong(map, key, 0L);
    }

    public static long getLong(Map<String, Object> map, String key, long defaultValue) {
        Object value = map.get(key);
        return (value instanceof Number) ? ((Number) value).longValue() : defaultValue;
    }

    // --- float ---
    public static float getFloat(Map<String, Object> map, String key) {
        return getFloat(map, key, 0.0f);
    }

    public static float getFloat(Map<String, Object> map, String key, float defaultValue) {
        Object value = map.get(key);
        return (value instanceof Number) ? ((Number) value).floatValue() : defaultValue;
    }

    // --- double ---
    public static double getDouble(Map<String, Object> map, String key) {
        return getDouble(map, key, 0.0);
    }

    public static double getDouble(Map<String, Object> map, String key, double defaultValue) {
        Object value = map.get(key);
        return (value instanceof Number) ? ((Number) value).doubleValue() : defaultValue;
    }

    // --- boolean ---
    public static boolean getBoolean(Map<String, Object> map, String key) {
        return getBoolean(map, key, false);
    }

    public static boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        return (value instanceof Boolean) ? (Boolean) value : defaultValue;
    }

    // --- String ---
    public static String getString(Map<String, Object> map, String key) {
        return getString(map, key, "");
    }

    public static String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return (value instanceof String) ? (String) value : defaultValue;
    }

}
