package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public enum Horizontal {
    LEFT("left"),
    RIGHT("right"),
    CENTER("center");

    private final String jsonValue;

    Horizontal(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    public JsonElement toJson() {
        return new JsonPrimitive(jsonValue);
    }

    public static Horizontal fromJson(String jsonString) {
        for (Horizontal value : Horizontal.values()) {
            if (value.jsonValue.equals(jsonString)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid JSON string for Horizontal: " + jsonString);
    }
}
