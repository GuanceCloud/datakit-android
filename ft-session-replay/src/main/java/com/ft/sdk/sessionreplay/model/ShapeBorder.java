package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.Objects;

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
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShapeBorder that = (ShapeBorder) o;
        return width == that.width && Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, width);
    }
}
