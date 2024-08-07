package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public abstract class WireframeUpdateMutation {
    public abstract JsonElement toJson();

    public static WireframeUpdateMutation fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into one of type WireframeUpdateMutation", e);
        }
    }

    public static WireframeUpdateMutation fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        List<Throwable> errors = new ArrayList<>();
        WireframeUpdateMutation result = null;

        try {
            result = TextWireframeUpdate.fromJsonObject(jsonObject);
        } catch (JsonParseException e) {
            errors.add(e);
        }

        if (result == null) {
            try {
                result = ShapeWireframeUpdate.fromJsonObject(jsonObject);
            } catch (JsonParseException e) {
                errors.add(e);
            }
        }

        if (result == null) {
            try {
                result = ImageWireframeUpdate.fromJsonObject(jsonObject);
            } catch (JsonParseException e) {
                errors.add(e);
            }
        }

        if (result == null) {
            try {
                result = PlaceholderWireframeUpdate.fromJsonObject(jsonObject);
            } catch (JsonParseException e) {
                errors.add(e);
            }
        }

        if (result == null) {
            try {
                result = WebviewWireframeUpdate.fromJsonObject(jsonObject);
            } catch (JsonParseException e) {
                errors.add(e);
            }
        }

        if (result == null) {
            String message = "Unable to parse json into one of type WireframeUpdateMutation\n" +
                    errors.stream().map(Throwable::getMessage).reduce("", (acc, err) -> acc + "\n" + err);
            throw new JsonParseException(message);
        }

        return result;
    }
}
