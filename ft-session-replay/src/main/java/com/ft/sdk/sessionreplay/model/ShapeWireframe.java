package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class ShapeWireframe extends Wireframe {
    private final Long id;
    private final Long x;
    private final Long y;
    private final Long width;
    private final Long height;
    private final WireframeClip clip;
    private final ShapeStyle shapeStyle;
    private final ShapeBorder border;
    private final String type = "shape";

    public ShapeWireframe(long id, long x, long y, long width, long height, WireframeClip clip, ShapeStyle shapeStyle, ShapeBorder border) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.clip = clip;
        this.shapeStyle = shapeStyle;
        this.border = border;
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
        return json;
    }

    public static ShapeWireframe fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException(
                    "Unable to parse json into type ShapeWireframe",
                    e
            );
        }
    }

    public static ShapeWireframe fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            long id = jsonObject.get("id").getAsLong();
            long x = jsonObject.get("x").getAsLong();
            long y = jsonObject.get("y").getAsLong();
            long width = jsonObject.get("width").getAsLong();
            long height = jsonObject.get("height").getAsLong();
            WireframeClip clip = jsonObject.get("clip") != null ? WireframeClip.fromJsonObject(jsonObject.get("clip").getAsJsonObject()) : null;
            ShapeStyle shapeStyle = jsonObject.get("shapeStyle") != null ? ShapeStyle.fromJsonObject(jsonObject.get("shapeStyle").getAsJsonObject()) : null;
            ShapeBorder border = jsonObject.get("border") != null ? ShapeBorder.fromJsonObject(jsonObject.get("border").getAsJsonObject()) : null;
            return new ShapeWireframe(id, x, y, width, height, clip, shapeStyle, border);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException(
                    "Unable to parse json into type ShapeWireframe",
                    e
            );
        }
    }

    public Wireframe setClip(WireframeClip clip) {
        return new ShapeWireframe(id, x, y, width, height, clip, shapeStyle, border);
    }

    @Override
    public boolean hasOpaqueBackground() {
        return shapeStyle != null && shapeStyle.isFullyOpaque() && shapeStyle.hasNonTranslucentColor();
    }
}
