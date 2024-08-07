package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class VisualViewportRecord extends MobileRecord {
    public final long timestamp;
    public final String slotId;
    public final Data3 data;
    public final long type = 8L;

    public VisualViewportRecord(long timestamp, String slotId, Data3 data) {
        this.timestamp = timestamp;
        this.slotId = slotId;
        this.data = data;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("timestamp", timestamp);
        if (slotId != null) {
            json.addProperty("slotId", slotId);
        }
        json.add("data", data.toJson());
        json.addProperty("type", type);
        return json;
    }

    public static VisualViewportRecord fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type VisualViewportRecord", e);
        }
    }

    public static VisualViewportRecord fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            long timestamp = jsonObject.get("timestamp").getAsLong();
            String slotId = jsonObject.has("slotId") ? jsonObject.get("slotId").getAsString() : null;
            Data3 data = Data3.fromJsonObject(jsonObject.get("data").getAsJsonObject());
            return new VisualViewportRecord(timestamp, slotId, data);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type VisualViewportRecord", e);
        }
    }
}
