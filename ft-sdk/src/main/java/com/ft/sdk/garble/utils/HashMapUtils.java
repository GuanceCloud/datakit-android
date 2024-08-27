package com.ft.sdk.garble.utils;

import java.util.Map;

public class HashMapUtils {

    public static String getString(Map<String, Object> tag, String key) {
        return getAsType(tag, key, String.class, null);
    }

    public static String getString(Map<String, Object> tag, String key, String defaultValue) {
        return getAsType(tag, key, String.class, defaultValue);
    }

    public static Boolean getBoolean(Map<String, Object> tag, String key) {
        return getAsType(tag, key, Boolean.class, null);
    }

    public static Boolean getBoolean(Map<String, Object> tag, String key, Boolean defaultValue) {
        return getAsType(tag, key, Boolean.class, defaultValue);
    }

    public static Integer getInt(Map<String, Object> tag, String key) {
        return getAsType(tag, key, Integer.class, null);
    }

    public static Integer getInt(Map<String, Object> tag, String key, Integer defaultValue) {
        return getAsType(tag, key, Integer.class, defaultValue);
    }

    public static Long getLong(Map<String, Object> tag, String key) {
        return getAsType(tag, key, Long.class, null);
    }

    public static Long getLong(Map<String, Object> tag, String key, Long defaultValue) {
        return getAsType(tag, key, Long.class, defaultValue);
    }

    public static Double getDouble(Map<String, Object> tag, String key) {
        return getAsType(tag, key, Double.class, null);
    }

    public static Double getDouble(Map<String, Object> tag, String key, Double defaultValue) {
        return getAsType(tag, key, Double.class, defaultValue);
    }

    private static <T> T getAsType(Map<String, Object> map, String key, Class<T> type, T defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue; // Return the default value if the key is not present or the value is null
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return defaultValue; // Return the default value if the type does not match
    }
}
