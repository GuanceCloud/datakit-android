package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public enum Source {
    ANDROID("android"),
    IOS("ios"),
    FLUTTER("flutter"),
    REACT_NATIVE("react-native");

    private final String jsonValue;

    Source(String jsonValue) {
        this.jsonValue = jsonValue;
    }

    public JsonElement toJson() {
        return new JsonPrimitive(jsonValue);
    }

    public static Source fromJson(String jsonString) {
        for (Source source : values()) {
            if (source.jsonValue.equals(jsonString)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Invalid JSON string: " + jsonString);
    }
}