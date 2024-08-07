package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class ShapeBorder {
    private final String color;
    private final long width;

    public ShapeBorder(String color, long width) {
        this.color = color;
        this.width = width;
    }

    public String getColor() {
        return color;
    }

    public long getWidth() {
        return width;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("color", color);
        json.addProperty("width", width);
        return json;
    }

    public static ShapeBorder fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException(
                    "Unable to parse json into type ShapeBorder",
                    e
            );
        }
    }

    public static ShapeBorder fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            String color = jsonObject.get("color").getAsString();
            long width = jsonObject.get("width").getAsLong();
            return new ShapeBorder(color, width);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException(
                    "Unable to parse json into type ShapeBorder",
                    e
            );
        }
    }

}
