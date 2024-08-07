package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class TextWireframe extends Wireframe {
    private long id;
    private long x;
    private long y;
    private long width;
    private long height;
    private WireframeClip clip;
    private ShapeStyle shapeStyle;
    private ShapeBorder border;
    private String text;
    private TextStyle textStyle;
    private TextPosition textPosition;
    private String type = "text";

    public TextWireframe(long id, long x, long y, long width, long height, WireframeClip clip, ShapeStyle shapeStyle,
                         ShapeBorder border, String text, TextStyle textStyle, TextPosition textPosition) {
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

    public Long getId() {
        return id;
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    public long getWidth() {
        return width;
    }

    public long getHeight() {
        return height;
    }

    public WireframeClip getClip() {
        return clip;
    }

    public ShapeStyle getShapeStyle() {
        return shapeStyle;
    }

    public ShapeBorder getBorder() {
        return border;
    }

    public String getText() {
        return text;
    }

    public TextStyle getTextStyle() {
        return textStyle;
    }

    public TextPosition getTextPosition() {
        return textPosition;
    }

    public String getType() {
        return type;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("x", x);
        json.addProperty("y", y);
        json.addProperty("width", width);
        json.addProperty("height", height);
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
        json.addProperty("text", text);
        json.add("textStyle", textStyle.toJson());
        if (textPosition != null) {
            json.add("textPosition", textPosition.toJson());
        }
        return json;
    }

    public static TextWireframe fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException(
                    "Unable to parse json into type TextWireframe",
                    e
            );
        }
    }

    public static TextWireframe fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            long id = jsonObject.get("id").getAsLong();
            long x = jsonObject.get("x").getAsLong();
            long y = jsonObject.get("y").getAsLong();
            long width = jsonObject.get("width").getAsLong();
            long height = jsonObject.get("height").getAsLong();
            WireframeClip clip = jsonObject.get("clip") != null ? WireframeClip.fromJsonObject(jsonObject.get("clip").getAsJsonObject()) : null;
            ShapeStyle shapeStyle = jsonObject.get("shapeStyle") != null ? ShapeStyle.fromJsonObject(jsonObject.get("shapeStyle").getAsJsonObject()) : null;
            ShapeBorder border = jsonObject.get("border") != null ? ShapeBorder.fromJsonObject(jsonObject.get("border").getAsJsonObject()) : null;
            String text = jsonObject.get("text").getAsString();
            TextStyle textStyle = TextStyle.fromJsonObject(jsonObject.get("textStyle").getAsJsonObject());
            TextPosition textPosition = jsonObject.get("textPosition") != null ? TextPosition.fromJsonObject(jsonObject.get("textPosition").getAsJsonObject()) : null;
            return new TextWireframe(id, x, y, width, height, clip, shapeStyle, border, text, textStyle, textPosition);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException(
                    "Unable to parse json into type TextWireframe",
                    e
            );
        }
    }

    public TextWireframe setClip(WireframeClip clip) {
        this.clip = clip;
        return this;
    }


    public TextWireframe copyWithId(long id) {
        this.id = id;
        return this;
    }

    public TextWireframe setX(Long x) {
        this.x = x;
        return this;
    }

    public TextWireframe setY(Long y) {
        this.y = y;
        return this;
    }

    public TextWireframe setWidth(Long width) {
        this.width = width;
        return this;
    }

    public TextWireframe setHeight(Long height) {
        this.height = height;
        return this;
    }


    public TextWireframe setShapeStyle(ShapeStyle shapeStyle) {
        this.shapeStyle = shapeStyle;
        return this;
    }

    public TextWireframe setBorder(ShapeBorder border) {
        this.border = border;
        return this;
    }

    public TextWireframe setText(String text) {
        this.text = text;
        return this;
    }

    public TextWireframe setTextStyle(TextStyle textStyle) {
        this.textStyle = textStyle;
        return this;
    }

    public TextWireframe setTextPosition(TextPosition textPosition) {
        this.textPosition = textPosition;
        return this;
    }

    public TextWireframe setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public boolean hasOpaqueBackground() {
        return shapeStyle != null && shapeStyle.isFullyOpaque() && shapeStyle.hasNonTranslucentColor();
    }


}
