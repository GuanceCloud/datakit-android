package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class FocusRecord extends MobileRecord {
    public final long timestamp;
    public final String slotId;
    public final Data2 data;
    public final long type = 6L;

    public FocusRecord(long timestamp, String slotId, Data2 data) {
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
        json.addProperty("type", type);
        json.add("data", data.toJson());
        return json;
    }

    public static FocusRecord fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type FocusRecord", e);
        }
    }

    public static FocusRecord fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            long timestamp = jsonObject.get("timestamp").getAsLong();
            String slotId = jsonObject.has("slotId") ? jsonObject.get("slotId").getAsString() : null;
            Data2 data = Data2.fromJsonObject(jsonObject.get("data").getAsJsonObject());
            return new FocusRecord(timestamp, slotId, data);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type FocusRecord", e);
        }
    }
}
