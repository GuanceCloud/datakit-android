package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.Objects;

public class TextPosition {
    private final Padding padding;
    private final Alignment alignment;

    public TextPosition(Padding padding, Alignment alignment) {
        this.padding = padding;
        this.alignment = alignment;
    }

    public Padding getPadding() {
        return padding;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        if (padding != null) {
            json.add("padding", padding.toJson());
        }
        if (alignment != null) {
            json.add("alignment", alignment.toJson());
        }
        return json;
    }

    public static TextPosition fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException(
                    "Unable to parse json into type TextPosition",
                    e
            );
        }
    }

    public static TextPosition fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            Padding padding = jsonObject.has("padding") ? Padding.fromJsonObject(jsonObject.getAsJsonObject("padding")) : null;
            Alignment alignment = jsonObject.has("alignment") ? Alignment.fromJsonObject(jsonObject.getAsJsonObject("alignment")) : null;
            return new TextPosition(padding, alignment);
        } catch (IllegalStateException | NullPointerException e) {
            throw new JsonParseException(
                    "Unable to parse json into type TextPosition",
                    e
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextPosition that = (TextPosition) o;
        return Objects.equals(padding, that.padding) && Objects.equals(alignment, that.alignment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(padding, alignment);
    }
}
