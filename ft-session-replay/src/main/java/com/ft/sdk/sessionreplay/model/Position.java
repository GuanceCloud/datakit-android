package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class Position {
    public final Long id;
    public final Long x;
    public final Long y;
    public final Long timestamp;

    public Position(Long id, Long x, Long y, Long timestamp) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.timestamp = timestamp;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("x", x);
        json.addProperty("y", y);
        json.addProperty("timestamp", timestamp);
        return json;
    }

    public static Position fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type Position", e);
        }
    }

    public static Position fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            Long id = jsonObject.get("id").getAsLong();
            Long x = jsonObject.get("x").getAsLong();
            Long y = jsonObject.get("y").getAsLong();
            Long timestamp = jsonObject.get("timestamp").getAsLong();
            return new Position(id, x, y, timestamp);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type Position", e);
        }
    }

}
