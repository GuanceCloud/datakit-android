package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class PlaceholderWireframeUpdate extends WireframeUpdateMutation {
    public long id;
    public Long x;
    public Long y;
    public Long width;
    public Long height;
    public WireframeClip clip;
    public String label;
    public String type = "placeholder";

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
        json.addProperty("type", type);
        if (label != null) {
            json.addProperty("label", label);
        }
        return json;
    }

    public static PlaceholderWireframeUpdate fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type PlaceholderWireframeUpdate", e);
        }
    }

    public static PlaceholderWireframeUpdate fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            long id = jsonObject.get("id").getAsLong();
            Long x = jsonObject.has("x") ? jsonObject.get("x").getAsLong() : null;
            Long y = jsonObject.has("y") ? jsonObject.get("y").getAsLong() : null;
            Long width = jsonObject.has("width") ? jsonObject.get("width").getAsLong() : null;
            Long height = jsonObject.has("height") ? jsonObject.get("height").getAsLong() : null;
            WireframeClip clip = jsonObject.has("clip") ? WireframeClip.fromJsonObject(jsonObject.getAsJsonObject("clip")) : null;
            String label = jsonObject.has("label") ? jsonObject.get("label").getAsString() : null;
            return new PlaceholderWireframeUpdate(id, x, y, width, height, clip, label);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type PlaceholderWireframeUpdate", e);
        }
    }

    public PlaceholderWireframeUpdate(long id, Long x, Long y, Long width, Long height, WireframeClip clip, String label) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.clip = clip;
        this.label = label;
    }


    public PlaceholderWireframeUpdate setId(long id) {
        this.id = id;
        return this;
    }

    public PlaceholderWireframeUpdate setX(Long x) {
        this.x = x;
        return this;
    }

    public PlaceholderWireframeUpdate setY(Long y) {
        this.y = y;
        return this;
    }

    public PlaceholderWireframeUpdate setWidth(Long width) {
        this.width = width;
        return this;
    }

    public PlaceholderWireframeUpdate setHeight(Long height) {
        this.height = height;
        return this;
    }

    public PlaceholderWireframeUpdate setClip(WireframeClip clip) {
        this.clip = clip;
        return this;
    }

    public PlaceholderWireframeUpdate setType(String type) {
        this.type = type;
        return this;
    }

    public PlaceholderWireframeUpdate setLabel(String label) {
        this.label = label;
        return this;
    }
}
