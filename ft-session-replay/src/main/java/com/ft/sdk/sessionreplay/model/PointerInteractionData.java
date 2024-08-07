package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class PointerInteractionData extends MobileIncrementalData {
    public final PointerEventType pointerEventType;
    public final PointerType pointerType;
    public final Long pointerId;
    public final Number x;
    public final Number y;
    public final Long source = 9L;

    public PointerInteractionData(PointerEventType pointerEventType, PointerType pointerType, Long pointerId, Number x, Number y) {
        this.pointerEventType = pointerEventType;
        this.pointerType = pointerType;
        this.pointerId = pointerId;
        this.x = x;
        this.y = y;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("source", source);
        json.add("pointerEventType", pointerEventType.toJson());
        json.add("pointerType", pointerType.toJson());
        json.addProperty("pointerId", pointerId);
        json.addProperty("x", x);
        json.addProperty("y", y);
        return json;
    }

    public static PointerInteractionData fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type PointerInteractionData", e);
        }
    }

    public static PointerInteractionData fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            PointerEventType pointerEventType = PointerEventType.fromJson(jsonObject.get("pointerEventType").getAsString());
            PointerType pointerType = PointerType.fromJson(jsonObject.get("pointerType").getAsString());
            Long pointerId = jsonObject.get("pointerId").getAsLong();
            Number x = jsonObject.get("x").getAsNumber();
            Number y = jsonObject.get("y").getAsNumber();
            return new PointerInteractionData(pointerEventType, pointerType, pointerId, x, y);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type PointerInteractionData", e);
        }
    }
}
