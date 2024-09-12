package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class View {
    private final String id;

    public View(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        return json;
    }

    public static View fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type View", e);
        }
    }

    public static View fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            String id = jsonObject.get("id").getAsString();
            return new View(id);
        } catch (IllegalStateException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type View", e);
        }
    }
}
