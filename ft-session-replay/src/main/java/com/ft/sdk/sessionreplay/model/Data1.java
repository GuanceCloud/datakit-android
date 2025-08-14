package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class Data1 {
    public final long width;
    public final long height;
    public final String href;

    public Data1(long width, long height, String href) {
        this.width = width;
        this.height = height;
        this.href = href;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("width", width);
        json.addProperty("height", height);
        if (href != null) {
            json.addProperty("href", href);
        }
        return json;
    }

    public static Data1 fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type Data1", e);
        }
    }

    public static Data1 fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            long width = jsonObject.get("width").getAsLong();
            long height = jsonObject.get("height").getAsLong();
            String href = jsonObject.has("href") ? jsonObject.get("href").getAsString() : null;
            return new Data1(width, height, href);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type Data1", e);
        }
    }
}
