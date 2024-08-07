package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class WebviewWireframe extends Wireframe {
    private final Long id;
    private final Long x;
    private final Long y;
    private final Long width;
    private final Long height;
    private final WireframeClip clip;
    private final ShapeStyle shapeStyle;
    private final ShapeBorder border;
    private final String slotId;
    private final Boolean isVisible;
    private final String type = "webview";

    public WebviewWireframe(Long id, Long x, Long y, Long width, Long height, WireframeClip clip, ShapeStyle shapeStyle, ShapeBorder border, String slotId, Boolean isVisible) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.clip = clip;
        this.shapeStyle = shapeStyle;
        this.border = border;
        this.slotId = slotId;
        this.isVisible = isVisible;
    }

    public Long getId() {
        return id;
    }

    public Long getX() {
        return x;
    }

    public Long getY() {
        return y;
    }

    public Long getWidth() {
        return width;
    }

    public Long getHeight() {
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

    public String getSlotId() {
        return slotId;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public String getType() {
        return type;
    }

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
        json.addProperty("slotId", slotId);
        if (isVisible != null) {
            json.addProperty("isVisible", isVisible);
        }
        return json;
    }

    public static WebviewWireframe fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type WebviewWireframe", e);
        }
    }

    public static WebviewWireframe fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            Long id = jsonObject.get("id").getAsLong();
            Long x = jsonObject.get("x").getAsLong();
            Long y = jsonObject.get("y").getAsLong();
            Long width = jsonObject.get("width").getAsLong();
            Long height = jsonObject.get("height").getAsLong();
            WireframeClip clip = jsonObject.has("clip") ? WireframeClip.fromJsonObject(jsonObject.get("clip").getAsJsonObject()) : null;
            ShapeStyle shapeStyle = jsonObject.has("shapeStyle") ? ShapeStyle.fromJsonObject(jsonObject.get("shapeStyle").getAsJsonObject()) : null;
            ShapeBorder border = jsonObject.has("border") ? ShapeBorder.fromJsonObject(jsonObject.get("border").getAsJsonObject()) : null;
            String slotId = jsonObject.get("slotId").getAsString();
            Boolean isVisible = jsonObject.has("isVisible") ? jsonObject.get("isVisible").getAsBoolean() : null;
            return new WebviewWireframe(id, x, y, width, height, clip, shapeStyle, border, slotId, isVisible);
        } catch (IllegalStateException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type WebviewWireframe", e);
        }
    }

    @Override
    public Wireframe setClip(WireframeClip clip) {
        return new WebviewWireframe(id, x, y, width, height, clip, shapeStyle, border, slotId, isVisible);
    }

    @Override
    public boolean hasOpaqueBackground() {
        return true;
    }
}
