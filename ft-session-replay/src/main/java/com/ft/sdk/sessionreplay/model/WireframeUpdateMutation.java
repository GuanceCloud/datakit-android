package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public abstract class WireframeUpdateMutation {
    public abstract JsonElement toJson();

    public static WireframeUpdateMutation fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
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
            // Collect all error messages manually
            StringBuilder message = new StringBuilder("Unable to parse json into one of type WireframeUpdateMutation");
            for (Throwable error : errors) {
                message.append("\n").append(error.getMessage());
            }
            throw new JsonParseException(message.toString());
        }

        return result;
    }
}

