package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.Objects;

public class TextStyle {
    private final String family;
    private final double size;
    private final String color;

    public TextStyle(String family, double size, String color) {
        this.family = family;
        this.size = size;
        this.color = color;
    }

    public String getFamily() {
        return family;
    }

    public double getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("family", family);
        json.addProperty("size", size);
        json.addProperty("color", color);
        return json;
    }

    public static TextStyle fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException(
                    "Unable to parse json into type TextStyle",
                    e
            );
        }
    }

    public static TextStyle fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            String family = jsonObject.get("family").getAsString();
            double size = jsonObject.get("size").getAsDouble();
            String color = jsonObject.get("color").getAsString();
            return new TextStyle(family, size, color);
        } catch (IllegalStateException | NullPointerException e) {
            throw new JsonParseException(
                    "Unable to parse json into type TextStyle",
                    e
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextStyle textStyle = (TextStyle) o;
        return Double.compare(textStyle.size, size) == 0 && Objects.equals(family, textStyle.family) && Objects.equals(color, textStyle.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(family, size, color);
    }
}
