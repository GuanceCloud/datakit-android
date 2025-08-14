package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public enum PointerType {
    MOUSE("mouse"),
    TOUCH("touch"),
    PEN("pen");

    private final String jsonValue;

    PointerType(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    public JsonElement toJson() {
        return new JsonPrimitive(jsonValue);
    }

    public static PointerType fromJson(String jsonString) {
        for (PointerType pointerType : values()) {
            if (pointerType.jsonValue.equals(jsonString)) {
                return pointerType;
            }
        }
        throw new IllegalArgumentException("Unknown PointerType: " + jsonString);
    }
}
