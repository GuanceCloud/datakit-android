package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class Remove {
    public final Long id;

    public Remove(Long id) {
        this.id = id;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        return json;
    }

    public static Remove fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type Remove", e);
        }
    }

    public static Remove fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            Long id = jsonObject.get("id").getAsLong();
            return new Remove(id);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type Remove", e);
        }
    }
}
