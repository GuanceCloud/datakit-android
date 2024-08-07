package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class Application {
    private final String id;

    public Application(String id) {
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

    public static Application fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException(
                    "Unable to parse json into type Application",
                    e
            );
        }
    }

    public static Application fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            String id = jsonObject.get("id").getAsString();
            return new Application(id);
        } catch (IllegalStateException | NullPointerException e) {
            throw new JsonParseException(
                    "Unable to parse json into type Application",
                    e
            );
        }
    }

}








