package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class ViewportResizeData extends MobileIncrementalData {
    public final Long width;
    public final Long height;
    public final Long source = 4L;

    public ViewportResizeData(Long width, Long height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("source", source);
        json.addProperty("width", width);
        json.addProperty("height", height);
        return json;
    }

    public static ViewportResizeData fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type ViewportResizeData", e);
        }
    }

    public static ViewportResizeData fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            Long width = jsonObject.get("width").getAsLong();
            Long height = jsonObject.get("height").getAsLong();
            return new ViewportResizeData(width, height);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type ViewportResizeData", e);
        }
    }
}
