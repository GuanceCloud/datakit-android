package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class Data {
    public final List<Wireframe> wireframes;

    public Data(List<Wireframe> wireframes) {
        this.wireframes = wireframes;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        JsonArray wireframesArray = new JsonArray();
        for (Wireframe wireframe : wireframes) {
            wireframesArray.add(wireframe.toJson());
        }
        json.add("wireframes", wireframesArray);
        return json;
    }

    public static Data fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type Data", e);
        }
    }

    public static Data fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            JsonArray wireframesArray = jsonObject.get("wireframes").getAsJsonArray();
            List<Wireframe> wireframes = new ArrayList<>(wireframesArray.size());
            for (JsonElement element : wireframesArray) {
                wireframes.add(Wireframe.fromJsonObject(element.getAsJsonObject()));
            }
            return new Data(wireframes);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type Data", e);
        }
    }
}
