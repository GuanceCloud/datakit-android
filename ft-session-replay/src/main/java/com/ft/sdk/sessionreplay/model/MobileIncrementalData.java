package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public abstract class MobileIncrementalData {
    public abstract JsonElement toJson();

    public static MobileIncrementalData fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into one of type MobileIncrementalData", e);
        }
    }

    public static MobileIncrementalData fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        List<Throwable> errors = new ArrayList<>();
        MobileIncrementalData asMobileMutationData = null;
        MobileIncrementalData asTouchData = null;
        MobileIncrementalData asViewportResizeData = null;
        MobileIncrementalData asPointerInteractionData = null;

        try {
            asMobileMutationData = MobileMutationData.fromJsonObject(jsonObject);
        } catch (JsonParseException e) {
            errors.add(e);
        }

        try {
            asTouchData = TouchData.fromJsonObject(jsonObject);
        } catch (JsonParseException e) {
            errors.add(e);
        }

        try {
            asViewportResizeData = ViewportResizeData.fromJsonObject(jsonObject);
        } catch (JsonParseException e) {
            errors.add(e);
        }

        try {
            asPointerInteractionData = PointerInteractionData.fromJsonObject(jsonObject);
        } catch (JsonParseException e) {
            errors.add(e);
        }

        MobileIncrementalData result = null;
        for (MobileIncrementalData data : new MobileIncrementalData[]{asMobileMutationData, asTouchData, asViewportResizeData, asPointerInteractionData}) {
            if (data != null) {
                result = data;
                break;
            }
        }

        if (result == null) {
            String message = "Unable to parse json into one of type MobileIncrementalData\n";
            for (Throwable error : errors) {
                message += error.getMessage() + "\n";
            }
            throw new JsonParseException(message);
        }

        return result;
    }
}
