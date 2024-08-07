package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class TextStyle {
    private final String family;
    private final long size;
    private final String color;

    public TextStyle(String family, long size, String color) {
        this.family = family;
        this.size = size;
        this.color = color;
    }

    public String getFamily() {
        return family;
    }

    public long getSize() {
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
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
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
            long size = jsonObject.get("size").getAsLong();
            String color = jsonObject.get("color").getAsString();
            return new TextStyle(family, size, color);
        } catch (IllegalStateException | NullPointerException e) {
            throw new JsonParseException(
                    "Unable to parse json into type TextStyle",
                    e
            );
        }
    }
}
