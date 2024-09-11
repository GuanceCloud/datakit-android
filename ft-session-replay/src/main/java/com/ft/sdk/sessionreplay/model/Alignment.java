package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.Objects;

public class Alignment {
    private final Horizontal horizontal;
    private final Vertical vertical;

    public Alignment(Horizontal horizontal, Vertical vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public Horizontal getHorizontal() {
        return horizontal;
    }

    public Vertical getVertical() {
        return vertical;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        if (horizontal != null) {
            json.add("horizontal", horizontal.toJson());
        }
        if (vertical != null) {
            json.add("vertical", vertical.toJson());
        }
        return json;
    }

    public static Alignment fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type Alignment", e);
        }
    }

    public static Alignment fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            Horizontal horizontal = jsonObject.has("horizontal") ? Horizontal.fromJson(jsonObject.get("horizontal").getAsString()) : null;
            Vertical vertical = jsonObject.has("vertical") ? Vertical.fromJson(jsonObject.get("vertical").getAsString()) : null;
            return new Alignment(horizontal, vertical);
        } catch (IllegalStateException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type Alignment", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alignment alignment = (Alignment) o;
        return horizontal == alignment.horizontal && vertical == alignment.vertical;
    }

    @Override
    public int hashCode() {
        return Objects.hash(horizontal, vertical);
    }
}
