package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class ImageWireframe extends Wireframe {
    private final Long id;
    private final Long x;
    private final Long y;
    private final Long width;
    private final Long height;
    private final WireframeClip clip;
    private final ShapeStyle shapeStyle;
    private final ShapeBorder border;
    private String base64;
    private String resourceId;
    private String mimeType;
    private Boolean isEmpty;

    public ImageWireframe(Long id, Long x, Long y, Long width, Long height, WireframeClip clip, ShapeStyle shapeStyle, ShapeBorder border, String base64, String resourceId, String mimeType, Boolean isEmpty) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.clip = clip;
        this.shapeStyle = shapeStyle;
        this.border = border;
        this.base64 = base64;
        this.resourceId = resourceId;
        this.mimeType = mimeType;
        this.isEmpty = isEmpty;
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

    public String getBase64() {
        return base64;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Boolean getIsEmpty() {
        return isEmpty;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Boolean getEmpty() {
        return isEmpty;
    }

    public void setEmpty(Boolean empty) {
        isEmpty = empty;
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
        json.addProperty("type", "image");
        if (base64 != null) {
            json.addProperty("base64", base64);
        }
        if (resourceId != null) {
            json.addProperty("resourceId", resourceId);
        }
        if (mimeType != null) {
            json.addProperty("mimeType", mimeType);
        }
        if (isEmpty != null) {
            json.addProperty("isEmpty", isEmpty);
        }
        return json;
    }

    public static ImageWireframe fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type ImageWireframe", e);
        }
    }

    public static ImageWireframe fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            Long id = jsonObject.get("id").getAsLong();
            Long x = jsonObject.get("x").getAsLong();
            Long y = jsonObject.get("y").getAsLong();
            Long width = jsonObject.get("width").getAsLong();
            Long height = jsonObject.get("height").getAsLong();
            WireframeClip clip = jsonObject.has("clip") ? WireframeClip.fromJsonObject(jsonObject.get("clip").getAsJsonObject()) : null;
            ShapeStyle shapeStyle = jsonObject.has("shapeStyle") ? ShapeStyle.fromJsonObject(jsonObject.get("shapeStyle").getAsJsonObject()) : null;
            ShapeBorder border = jsonObject.has("border") ? ShapeBorder.fromJsonObject(jsonObject.get("border").getAsJsonObject()) : null;
            String base64 = jsonObject.has("base64") ? jsonObject.get("base64").getAsString() : null;
            String resourceId = jsonObject.has("resourceId") ? jsonObject.get("resourceId").getAsString() : null;
            String mimeType = jsonObject.has("mimeType") ? jsonObject.get("mimeType").getAsString() : null;
            Boolean isEmpty = jsonObject.has("isEmpty") ? jsonObject.get("isEmpty").getAsBoolean() : null;
            return new ImageWireframe(id, x, y, width, height, clip, shapeStyle, border, base64, resourceId, mimeType, isEmpty);
        } catch (IllegalStateException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type ImageWireframe", e);
        }
    }

    @Override
    public Wireframe setClip(WireframeClip clip) {
        return new ImageWireframe(id, x, y, width, height, clip, shapeStyle, border,
                base64, resourceId, mimeType, isEmpty);
    }

    @Override
    public boolean hasOpaqueBackground() {
        return false;
    }
}
