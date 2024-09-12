package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class Session {
    public final String id;

    public Session(String id) {
        this.id = id;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        return json;
    }

    public static Session fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type Session", e);
        }
    }

    public static Session fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            String id = jsonObject.get("id").getAsString();
            return new Session(id);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type Session", e);
        }
    }
}