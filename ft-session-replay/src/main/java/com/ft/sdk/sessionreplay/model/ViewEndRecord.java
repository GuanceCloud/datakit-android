package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class ViewEndRecord extends MobileRecord {
    public final long timestamp;
    public final String slotId;
    public final long type = 7L;

    public ViewEndRecord(long timestamp, String slotId) {
        this.timestamp = timestamp;
        this.slotId = slotId;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("timestamp", timestamp);
        if (slotId != null) {
            json.addProperty("slotId", slotId);
        }
        json.addProperty("type", type);
        return json;
    }

    public static ViewEndRecord fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type ViewEndRecord", e);
        }
    }

    public static ViewEndRecord fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            long timestamp = jsonObject.get("timestamp").getAsLong();
            String slotId = jsonObject.has("slotId") ? jsonObject.get("slotId").getAsString() : null;
            return new ViewEndRecord(timestamp, slotId);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type ViewEndRecord", e);
        }
    }
}
