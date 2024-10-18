package com.ft.test.utils;

import com.ft.sdk.garble.utils.HashMapUtils;

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
        return HashMapUtils.getString(fieldMap, key, null);
    }

    public String getFieldAsString(String key, String defaultValue) {
        return HashMapUtils.getString(fieldMap, key, defaultValue);
    }

    public Boolean getFieldAsBoolean(String key) {
        return HashMapUtils.getBoolean(fieldMap, key, null);
    }

    public Boolean getFieldAsBoolean(String key, Boolean defaultValue) {
        return HashMapUtils.getBoolean(fieldMap, key, defaultValue);
    }

    public Integer getFieldAsInt(String key) {
        return HashMapUtils.getInt(fieldMap, key, null);
    }

    public Integer getFieldAsInt(String key, Integer defaultValue) {
        return HashMapUtils.getInt(fieldMap, key, defaultValue);
    }

    public Long getFieldAsLong(String key) {
        return HashMapUtils.getLong(fieldMap, key, null);
    }

    public Long getFieldAsLong(String key, Long defaultValue) {
        return HashMapUtils.getLong(fieldMap, key, defaultValue);
    }

    public Double getFieldAsDouble(String key) {
        return HashMapUtils.getDouble(fieldMap, key, null);
    }

    public Double getFieldAsDouble(String key, Double defaultValue) {
        return HashMapUtils.getDouble(fieldMap, key, defaultValue);
    }

    public String getTagAsString(String key) {
        return HashMapUtils.getString(tagMap, key, null);
    }

    public String getTagAsString(String key, String defaultValue) {
        return HashMapUtils.getString(tagMap, key, defaultValue);
    }

    public Boolean getTagAsBoolean(String key) {
        return HashMapUtils.getBoolean(tagMap, key, null);
    }

    public Boolean getTagAsBoolean(String key, Boolean defaultValue) {
        return HashMapUtils.getBoolean(tagMap, key, defaultValue);
    }

    public Integer getTagAsInt(String key) {
        return HashMapUtils.getInt(tagMap, key, null);
    }

    public Integer getTagAsInt(String key, Integer defaultValue) {
        return HashMapUtils.getInt(tagMap, key, defaultValue);
    }

    public Long getTagAsLong(String key) {
        return HashMapUtils.getLong(tagMap, key, null);
    }

    public Long getTagAsLong(String key, Long defaultValue) {
        return HashMapUtils.getLong(tagMap, key, defaultValue);
    }

    public Double getTagAsDouble(String key) {
        return HashMapUtils.getDouble(tagMap, key, null);
    }

    public Double getTagAsDouble(String key, Double defaultValue) {
        return HashMapUtils.getDouble(tagMap, key, defaultValue);
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
