package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;

public class TouchData extends MobileIncrementalData {
    public final Long source;
    public final List<Position> positions;

    public TouchData(Long source, List<Position> positions) {
        this.source = source;
        this.positions = positions;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("source", source);
        if (positions != null) {
            JsonArray positionsArray = new JsonArray();
            for (Position position : positions) {
                positionsArray.add(position.toJson());
            }
            json.add("positions", positionsArray);
        }
        return json;
    }

    public static TouchData fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type TouchData", e);
        }
    }

    public static TouchData fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            JsonArray positionsJsonArray = jsonObject.getAsJsonArray("positions");
            List<Position> positions = new ArrayList<>();
            if (positionsJsonArray != null) {
                for (JsonElement element : positionsJsonArray) {
                    positions.add(Position.fromJsonObject(element.getAsJsonObject()));
                }
            }
            Long source = jsonObject.get("source").getAsLong();
            return new TouchData(source, positions);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type TouchData", e);
        }
    }
}
