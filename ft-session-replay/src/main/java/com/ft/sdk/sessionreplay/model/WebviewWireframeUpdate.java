package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class WebviewWireframeUpdate extends WireframeUpdateMutation {
    public long id;
    public Long x;
    public Long y;
    public Long width;
    public Long height;
    public WireframeClip clip;
    public ShapeStyle shapeStyle;
    public ShapeBorder border;
    public String slotId;
    public Boolean isVisible;
    public String type = "webview";

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
        json.addProperty("slotId", slotId);
        if (isVisible != null) {
            json.addProperty("isVisible", isVisible);
        }
        return json;
    }

    public static WebviewWireframeUpdate fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type WebviewWireframeUpdate", e);
        }
    }

    public static WebviewWireframeUpdate fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            long id = jsonObject.get("id").getAsLong();
            Long x = jsonObject.has("x") ? jsonObject.get("x").getAsLong() : null;
            Long y = jsonObject.has("y") ? jsonObject.get("y").getAsLong() : null;
            Long width = jsonObject.has("width") ? jsonObject.get("width").getAsLong() : null;
            Long height = jsonObject.has("height") ? jsonObject.get("height").getAsLong() : null;
            WireframeClip clip = jsonObject.has("clip") ? WireframeClip.fromJsonObject(jsonObject.getAsJsonObject("clip")) : null;
            ShapeStyle shapeStyle = jsonObject.has("shapeStyle") ? ShapeStyle.fromJsonObject(jsonObject.getAsJsonObject("shapeStyle")) : null;
            ShapeBorder border = jsonObject.has("border") ? ShapeBorder.fromJsonObject(jsonObject.getAsJsonObject("border")) : null;
            String slotId = jsonObject.get("slotId").getAsString();
            Boolean isVisible = jsonObject.has("isVisible") ? jsonObject.get("isVisible").getAsBoolean() : null;
            return new WebviewWireframeUpdate(id, x, y, width, height, clip, shapeStyle, border, slotId, isVisible);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type WebviewWireframeUpdate", e);
        }
    }

    public WebviewWireframeUpdate(long id, Long x, Long y, Long width, Long height, WireframeClip clip, ShapeStyle shapeStyle, ShapeBorder border, String slotId, Boolean isVisible) {
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


    public WebviewWireframeUpdate setId(long id) {
        this.id = id;
        return this;
    }

    public WebviewWireframeUpdate setX(Long x) {
        this.x = x;
        return this;
    }

    public WebviewWireframeUpdate setY(Long y) {
        this.y = y;
        return this;
    }

    public WebviewWireframeUpdate setWidth(Long width) {
        this.width = width;
        return this;
    }

    public WebviewWireframeUpdate setHeight(Long height) {
        this.height = height;
        return this;
    }

    public WebviewWireframeUpdate setClip(WireframeClip clip) {
        this.clip = clip;
        return this;
    }

    public WebviewWireframeUpdate setShapeStyle(ShapeStyle shapeStyle) {
        this.shapeStyle = shapeStyle;
        return this;
    }

    public WebviewWireframeUpdate setBorder(ShapeBorder border) {
        this.border = border;
        return this;
    }


    public WebviewWireframeUpdate setType(String type) {
        this.type = type;
        return this;
    }
}
