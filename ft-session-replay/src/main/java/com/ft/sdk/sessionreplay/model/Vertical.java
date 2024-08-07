package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public enum Vertical {
    TOP("top"),
    BOTTOM("bottom"),
    CENTER("center");

    private final String jsonValue;

    Vertical(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    public JsonElement toJson() {
        return new JsonPrimitive(jsonValue);
    }

    public static Vertical fromJson(String jsonString) {
        for (Vertical value : Vertical.values()) {
            if (value.jsonValue.equals(jsonString)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid JSON string for Vertical: " + jsonString);
    }
}
