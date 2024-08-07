package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class Add {
    public final Long previousId;
    public final Wireframe wireframe;

    public Add(Long previousId, Wireframe wireframe) {
        this.previousId = previousId;
        this.wireframe = wireframe;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        if (previousId != null) {
            json.addProperty("previousId", previousId);
        }
        json.add("wireframe", wireframe.toJson());
        return json;
    }

    public static Add fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type Add", e);
        }
    }

    public static Add fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            Long previousId = jsonObject.has("previousId") ? jsonObject.get("previousId").getAsLong() : null;
            Wireframe wireframe = Wireframe.fromJsonObject(jsonObject.getAsJsonObject("wireframe"));
            return new Add(previousId, wireframe);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type Add", e);
        }
    }
}
