package com.ft.test.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineProtocolData {
    private final String measurement;
    private final Map<String, Object> tagMap;
    private final Map<String, Object> fieldMap;
    private final String timestamp;

    public LineProtocolData(String lineProtocolContent) {
        String[] parts = splitRespectingEscapedSpaces(lineProtocolContent, ' ', 2);
        String measurementAndTags = parts[0];
        String fields = parts[1];
        this.timestamp = parts[2];

        String[] measurementAndTagsParts = splitRespectingEscapedSpaces(measurementAndTags, ',', 1);
        this.measurement = measurementAndTagsParts[0];
        String tagSet = measurementAndTagsParts.length > 1 ? measurementAndTagsParts[1] : "";

        tagMap = parseKeyValuePairs(tagSet, false);
        fieldMap = parseKeyValuePairs(fields, true);
    }

    private Map<String, Object> parseKeyValuePairs(String input, boolean parseValues) {
        Map<String, Object> map = new HashMap<>();
        if (input != null && !input.isEmpty()) {
            String[] pairs = splitRespectingEscapedSpaces(input, ',', -1);
            for (String pair : pairs) {
                String[] keyValue = splitRespectingEscapedSpaces(pair, '=', 1);
                if (keyValue.length == 2) {
                    String key = unescapeSpaces(keyValue[0]);
                    String value = unescapeSpaces(keyValue[1]);
                    if (parseValues) {
                        map.put(key, parseValue(value));
                    } else {
                        map.put(key, value);
                    }
                }
            }
        }
        return map;
    }

    private Object parseValue(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                long longValue = Long.parseLong(value);
                if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                    return (int) longValue;
                }
                return longValue;
            }
        } catch (NumberFormatException e) {
            return value;
        }
    }

    public String getMeasurement() {
        return measurement;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Object getField(String key) {
        return fieldMap.get(key);
    }

    public Object getTag(String key) {
        return tagMap.get(key);
    }

    public int tagSize() {
        return tagMap == null ? 0 : tagMap.size();
    }

    public int getFieldSize() {
        return fieldMap == null ? 0 : fieldMap.size();
    }

    public String getFieldAsString(String key) {
        return getAsType(fieldMap, key, String.class, null);
    }

    public String getFieldAsString(String key, String defaultValue) {
        return getAsType(fieldMap, key, String.class, defaultValue);
    }

    public Boolean getFieldAsBoolean(String key) {
        return getAsType(fieldMap, key, Boolean.class, null);
    }

    public Boolean getFieldAsBoolean(String key, Boolean defaultValue) {
        return getAsType(fieldMap, key, Boolean.class, defaultValue);
    }

    public Integer getFieldAsInt(String key) {
        return getAsType(fieldMap, key, Integer.class, null);
    }

    public Integer getFieldAsInt(String key, Integer defaultValue) {
        return getAsType(fieldMap, key, Integer.class, defaultValue);
    }

    public Long getFieldAsLong(String key) {
        return getAsType(fieldMap, key, Long.class, null);
    }

    public Long getFieldAsLong(String key, Long defaultValue) {
        return getAsType(fieldMap, key, Long.class, defaultValue);
    }

    public Double getFieldAsDouble(String key) {
        return getAsType(fieldMap, key, Double.class, null);
    }

    public Double getFieldAsDouble(String key, Double defaultValue) {
        return getAsType(fieldMap, key, Double.class, defaultValue);
    }

    public String getTagAsString(String key) {
        return getAsType(tagMap, key, String.class, null);
    }

    public String getTagAsString(String key, String defaultValue) {
        return getAsType(tagMap, key, String.class, defaultValue);
    }

    public Boolean getTagAsBoolean(String key) {
        return getAsType(tagMap, key, Boolean.class, null);
    }

    public Boolean getTagAsBoolean(String key, Boolean defaultValue) {
        return getAsType(tagMap, key, Boolean.class, defaultValue);
    }

    public Integer getTagAsInt(String key) {
        return getAsType(tagMap, key, Integer.class, null);
    }

    public Integer getTagAsInt(String key, Integer defaultValue) {
        return getAsType(tagMap, key, Integer.class, defaultValue);
    }

    public Long getTagAsLong(String key) {
        return getAsType(tagMap, key, Long.class, null);
    }

    public Long getTagAsLong(String key, Long defaultValue) {
        return getAsType(tagMap, key, Long.class, defaultValue);
    }

    public Double getTagAsDouble(String key) {
        return getAsType(tagMap, key, Double.class, null);
    }

    public Double getTagAsDouble(String key, Double defaultValue) {
        return getAsType(tagMap, key, Double.class, defaultValue);
    }

    private <T> T getAsType(Map<String, Object> map, String key, Class<T> type, T defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue; // Return the default value if the key is not present or the value is null
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return defaultValue; // Return the default value if the type does not match
    }

    private String[] splitRespectingEscapedSpaces(String input, char delimiter, int limit) {
        StringBuilder sb = new StringBuilder();
        List<String> parts = new ArrayList<>();
        boolean escaped = false;
        boolean insideQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '\\' && !escaped) {
                escaped = true;
            } else {
                if (c == delimiter && !escaped && !insideQuotes) {
                    parts.add(sb.toString());
                    sb.setLength(0);
                    if (limit > 0 && parts.size() >= limit) {
                        sb.append(input.substring(i + 1));
                        break;
                    }
                } else {
                    if (c == '"' && !escaped) {
                        insideQuotes = !insideQuotes;  // Toggle quote state
                    }
                    sb.append(c);
                }
                escaped = false;
            }
        }
        parts.add(sb.toString());

        return parts.toArray(new String[0]);
    }

    private String unescapeSpaces(String input) {
        return input.replace("\\ ", " ");
    }
}
