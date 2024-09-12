package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class Data2 {
    public final boolean hasFocus;

    public Data2(boolean hasFocus) {
        this.hasFocus = hasFocus;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("has_focus", hasFocus);
        return json;
    }

    public static Data2 fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type Data2", e);
        }
    }

    public static Data2 fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            boolean hasFocus = jsonObject.get("has_focus").getAsBoolean();
            return new Data2(hasFocus);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type Data2", e);
        }
    }
}
