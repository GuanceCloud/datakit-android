package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class TextWireframeUpdate extends WireframeUpdateMutation {
    public long id;
    public Long x;
    public Long y;
    public Long width;
    public Long height;
    public WireframeClip clip;
    public ShapeStyle shapeStyle;
    public ShapeBorder border;
    public String text;
    public TextStyle textStyle;
    public TextPosition textPosition;
    public String type = "text";

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        if (x != null) {
            json.addProperty("x", x);
        }
        if (y != null) {
            json.addProperty("y", y);
        }
        if (width != null) {
            json.addProperty("width", width);
        }
        if (height != null) {
            json.addProperty("height", height);
        }
        if (clip != null) {
            json.add("clip", clip.toJson());
        }
        if (shapeStyle != null) {
            json.add("shapeStyle", shapeStyle.toJson());
        }
        if (border != null) {
            json.add("border", border.toJson());
        }
        json.addProperty("type", type);
        if (text != null) {
            json.addProperty("text", text);
        }
        if (textStyle != null) {
            json.add("textStyle", textStyle.toJson());
        }
        if (textPosition != null) {
            json.add("textPosition", textPosition.toJson());
        }
        return json;
    }

    public static TextWireframeUpdate fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type TextWireframeUpdate", e);
        }
    }

    public static TextWireframeUpdate fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            long id = jsonObject.get("id").getAsLong();
            Long x = jsonObject.has("x") ? jsonObject.get("x").getAsLong() : null;
            Long y = jsonObject.has("y") ? jsonObject.get("y").getAsLong() : null;
            Long width = jsonObject.has("width") ? jsonObject.get("width").getAsLong() : null;
            Long height = jsonObject.has("height") ? jsonObject.get("height").getAsLong() : null;
            WireframeClip clip = jsonObject.has("clip") ? WireframeClip.fromJsonObject(jsonObject.getAsJsonObject("clip")) : null;
            ShapeStyle shapeStyle = jsonObject.has("shapeStyle") ? ShapeStyle.fromJsonObject(jsonObject.getAsJsonObject("shapeStyle")) : null;
            ShapeBorder border = jsonObject.has("border") ? ShapeBorder.fromJsonObject(jsonObject.getAsJsonObject("border")) : null;
            String text = jsonObject.has("text") ? jsonObject.get("text").getAsString() : null;
            TextStyle textStyle = jsonObject.has("textStyle") ? TextStyle.fromJsonObject(jsonObject.getAsJsonObject("textStyle")) : null;
            TextPosition textPosition = jsonObject.has("textPosition") ? TextPosition.fromJsonObject(jsonObject.getAsJsonObject("textPosition")) : null;
            return new TextWireframeUpdate(id, x, y, width, height, clip, shapeStyle, border, text, textStyle, textPosition);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type TextWireframeUpdate", e);
        }
    }

    public TextWireframeUpdate(long id, Long x, Long y, Long width, Long height, WireframeClip clip, ShapeStyle shapeStyle, ShapeBorder border, String text, TextStyle textStyle, TextPosition textPosition) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.clip = clip;
        this.shapeStyle = shapeStyle;
        this.border = border;
        this.text = text;
        this.textStyle = textStyle;
        this.textPosition = textPosition;
    }

    public TextWireframeUpdate copyWithId(long id) {
        this.id = id;
        return this;
    }

    public TextWireframeUpdate setX(Long x) {
        this.x = x;
        return this;
    }

    public TextWireframeUpdate setY(Long y) {
        this.y = y;
        return this;
    }

    public TextWireframeUpdate setWidth(Long width) {
        this.width = width;
        return this;
    }

    public TextWireframeUpdate setHeight(Long height) {
        this.height = height;
        return this;
    }

    public TextWireframeUpdate setClip(WireframeClip clip) {
        this.clip = clip;
        return this;
    }

    public TextWireframeUpdate setShapeStyle(ShapeStyle shapeStyle) {
        this.shapeStyle = shapeStyle;
        return this;
    }

    public TextWireframeUpdate setBorder(ShapeBorder border) {
        this.border = border;
        return this;
    }

    public TextWireframeUpdate setText(String text) {
        this.text = text;
        return this;
    }

    public TextWireframeUpdate setTextStyle(TextStyle textStyle) {
        this.textStyle = textStyle;
        return this;
    }

    public TextWireframeUpdate setTextPosition(TextPosition textPosition) {
        this.textPosition = textPosition;
        return this;
    }

    public TextWireframeUpdate setType(String type) {
        this.type = type;
        return this;
    }
}
