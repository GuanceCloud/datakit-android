package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.Objects;

public class WireframeClip {
    private final Long top;
    private final Long bottom;
    private final Long left;
    private final Long right;

    public WireframeClip(Long top, Long bottom, Long left, Long right) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    public long getTop() {
        return top == null ? 0L : top;
    }

    public long getBottom() {
        return bottom == null ? 0L : bottom;
    }

    public long getLeft() {
        return left == null ? 0L : left;
    }

    public long getRight() {
        return right == null ? 0L : right;
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

    public static WireframeClip fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException(
                    "Unable to parse json into type WireframeClip",
                    e
            );
        }
    }

    public static WireframeClip fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            Long top = jsonObject.get("top") != null ? jsonObject.get("top").getAsLong() : null;
            Long bottom = jsonObject.get("bottom") != null ? jsonObject.get("bottom").getAsLong() : null;
            Long left = jsonObject.get("left") != null ? jsonObject.get("left").getAsLong() : null;
            Long right = jsonObject.get("right") != null ? jsonObject.get("right").getAsLong() : null;
            return new WireframeClip(top, bottom, left, right);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException(
                    "Unable to parse json into type WireframeClip",
                    e
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WireframeClip that = (WireframeClip) o;
        return Objects.equals(top, that.top) && Objects.equals(bottom, that.bottom) && Objects.equals(left, that.left) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(top, bottom, left, right);
    }
}
