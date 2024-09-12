package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.Objects;

public class PlaceholderWireframe extends Wireframe {
    private Long id;
    private Long x;
    private Long y;
    private Long width;
    private Long height;
    private WireframeClip clip;
    private String label;
    private String type = "placeholder";

    public PlaceholderWireframe(long id, long x, long y, long width, long height,
                                WireframeClip clip, String label) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.clip = clip;
        this.label = label;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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
        json.addProperty("type", type);
        if (label != null) {
            json.addProperty("label", label);
        }
        return json;
    }

    public static PlaceholderWireframe fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException(
                    "Unable to parse json into type PlaceholderWireframe",
                    e
            );
        }
    }

    public static PlaceholderWireframe fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            long id = jsonObject.get("id").getAsLong();
            long x = jsonObject.get("x").getAsLong();
            long y = jsonObject.get("y").getAsLong();
            long width = jsonObject.get("width").getAsLong();
            long height = jsonObject.get("height").getAsLong();
            WireframeClip clip = jsonObject.get("clip") != null ? WireframeClip.fromJsonObject(jsonObject.get("clip").getAsJsonObject()) : null;
            String label = jsonObject.get("label") != null ? jsonObject.get("label").getAsString() : null;
            return new PlaceholderWireframe(id, x, y, width, height, clip, label);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException(
                    "Unable to parse json into type PlaceholderWireframe",
                    e
            );
        }
    }

    @Override
    public boolean hasOpaqueBackground() {
        return true;
    }


    public PlaceholderWireframe setId(Long id) {
        this.id = id;
        return this;
    }

    public PlaceholderWireframe setX(Long x) {
        this.x = x;
        return this;
    }

    public PlaceholderWireframe setY(Long y) {
        this.y = y;
        return this;
    }

    public PlaceholderWireframe setWidth(Long width) {
        this.width = width;
        return this;
    }

    public PlaceholderWireframe setHeight(Long height) {
        this.height = height;
        return this;
    }

    public PlaceholderWireframe setClip(WireframeClip clip) {
        this.clip = clip;
        return this;
    }

    public PlaceholderWireframe setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaceholderWireframe that = (PlaceholderWireframe) o;
        return Objects.equals(id, that.id) && Objects.equals(x, that.x) && Objects.equals(y, that.y) && Objects.equals(width, that.width) && Objects.equals(height, that.height) && Objects.equals(clip, that.clip) && Objects.equals(label, that.label) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, x, y, width, height, clip, label, type);
    }
}
