package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public enum PointerEventType {
    DOWN("down"),
    UP("up"),
    MOVE("move");

    private final String jsonValue;

    PointerEventType(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    public JsonElement toJson() {
        return new JsonPrimitive(jsonValue);
    }

    public static PointerEventType fromJson(String jsonString) {
        for (PointerEventType eventType : values()) {
            if (eventType.jsonValue.equals(jsonString)) {
                return eventType;
            }
        }
        throw new IllegalArgumentException("Unknown PointerEventType: " + jsonString);
    }
}
