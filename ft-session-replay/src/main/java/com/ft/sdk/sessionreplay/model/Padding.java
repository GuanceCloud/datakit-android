package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.Objects;

public class Padding {
    private final Long top;
    private final Long bottom;
    private final Long left;
    private final Long right;

    public Padding(Long top, Long bottom, Long left, Long right) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    public Long getTop() {
        return top;
    }

    public Long getBottom() {
        return bottom;
    }

    public Long getLeft() {
        return left;
    }

    public Long getRight() {
        return right;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        if (top != null) {
            json.addProperty("top", top);
        }
        if (bottom != null) {
            json.addProperty("bottom", bottom);
        }
        if (left != null) {
            json.addProperty("left", left);
        }
        if (right != null) {
            json.addProperty("right", right);
        }
        return json;
    }

    public static Padding fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type Padding", e);
        }
    }

    public static Padding fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            Long top = jsonObject.has("top") ? jsonObject.get("top").getAsLong() : null;
            Long bottom = jsonObject.has("bottom") ? jsonObject.get("bottom").getAsLong() : null;
            Long left = jsonObject.has("left") ? jsonObject.get("left").getAsLong() : null;
            Long right = jsonObject.has("right") ? jsonObject.get("right").getAsLong() : null;
            return new Padding(top, bottom, left, right);
        } catch (IllegalStateException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type Padding", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Padding padding = (Padding) o;
        return Objects.equals(top, padding.top) && Objects.equals(bottom, padding.bottom) && Objects.equals(left, padding.left) && Objects.equals(right, padding.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(top, bottom, left, right);
    }
}
