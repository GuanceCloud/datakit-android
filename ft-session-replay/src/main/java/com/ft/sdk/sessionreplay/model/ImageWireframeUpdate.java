package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class ImageWireframeUpdate extends WireframeUpdateMutation {
    public long id;
    public Long x;
    public Long y;
    public Long width;
    public Long height;
    public WireframeClip clip;
    public ShapeStyle shapeStyle;
    public ShapeBorder border;
    public String base64;
    public String resourceId;
    public String mimeType;
    public Boolean isEmpty;
    public String type = "image";

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
//            json.addProperty("isEmpty", isEmpty);
            //fixme 图片功能加载完成后恢复这个参数
            json.addProperty("isEmpty", true);
        }
        return json;
    }

    public static ImageWireframeUpdate fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type ImageWireframeUpdate", e);
        }
    }

    public static ImageWireframeUpdate fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            long id = jsonObject.get("id").getAsLong();
            Long x = jsonObject.has("x") ? jsonObject.get("x").getAsLong() : null;
            Long y = jsonObject.has("y") ? jsonObject.get("y").getAsLong() : null;
            Long width = jsonObject.has("width") ? jsonObject.get("width").getAsLong() : null;
            Long height = jsonObject.has("height") ? jsonObject.get("height").getAsLong() : null;
            WireframeClip clip = jsonObject.has("clip") ? WireframeClip.fromJsonObject(jsonObject.getAsJsonObject("clip")) : null;
            ShapeStyle shapeStyle = jsonObject.has("shapeStyle") ? ShapeStyle.fromJsonObject(jsonObject.getAsJsonObject("shapeStyle")) : null;
            ShapeBorder border = jsonObject.has("border") ? ShapeBorder.fromJsonObject(jsonObject.getAsJsonObject("border")) : null;
            String base64 = jsonObject.has("base64") ? jsonObject.get("base64").getAsString() : null;
            String resourceId = jsonObject.has("resourceId") ? jsonObject.get("resourceId").getAsString() : null;
            String mimeType = jsonObject.has("mimeType") ? jsonObject.get("mimeType").getAsString() : null;
            Boolean isEmpty = jsonObject.has("isEmpty") ? jsonObject.get("isEmpty").getAsBoolean() : null;
            return new ImageWireframeUpdate(id, x, y, width, height, clip, shapeStyle, border, base64, resourceId, mimeType, isEmpty);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type ImageWireframeUpdate", e);
        }
    }

    public ImageWireframeUpdate(long id, Long x, Long y, Long width, Long height, WireframeClip clip, ShapeStyle shapeStyle, ShapeBorder border, String base64, String resourceId, String mimeType, Boolean isEmpty) {
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

    public ImageWireframeUpdate setId(long id) {
        this.id = id;
        return this;
    }

    public ImageWireframeUpdate setX(Long x) {
        this.x = x;
        return this;
    }

    public ImageWireframeUpdate setY(Long y) {
        this.y = y;
        return this;
    }

    public ImageWireframeUpdate setWidth(Long width) {
        this.width = width;
        return this;
    }

    public ImageWireframeUpdate setHeight(Long height) {
        this.height = height;
        return this;
    }

    public ImageWireframeUpdate setClip(WireframeClip clip) {
        this.clip = clip;
        return this;
    }

    public ImageWireframeUpdate setShapeStyle(ShapeStyle shapeStyle) {
        this.shapeStyle = shapeStyle;
        return this;
    }

    public ImageWireframeUpdate setBorder(ShapeBorder border) {
        this.border = border;
        return this;
    }


    public ImageWireframeUpdate setType(String type) {
        this.type = type;
        return this;
    }
}
