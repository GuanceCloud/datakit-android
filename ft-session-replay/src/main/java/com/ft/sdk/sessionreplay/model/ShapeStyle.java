package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.Locale;
import java.util.Objects;

public class ShapeStyle {
    private final String backgroundColor;
    private final Number opacity;
    private final Number cornerRadius;

    public ShapeStyle(String backgroundColor, Number opacity, Number cornerRadius) {
        this.backgroundColor = backgroundColor;
        this.opacity = opacity;
        this.cornerRadius = cornerRadius;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public Number getOpacity() {
        return opacity;
    }

    public Number getCornerRadius() {
        return cornerRadius;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        if (backgroundColor != null) {
            json.addProperty("backgroundColor", backgroundColor);
        }
        if (opacity != null) {
            json.addProperty("opacity", opacity);
        }
        if (cornerRadius != null) {
            json.addProperty("cornerRadius", cornerRadius);
        }
        return json;
    }

    public static ShapeStyle fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException(
                    "Unable to parse json into type ShapeStyle",
                    e
            );
        }
    }

    public static ShapeStyle fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            String backgroundColor = jsonObject.get("backgroundColor") != null ? jsonObject.get("backgroundColor").getAsString() : null;
            Number opacity = jsonObject.get("opacity") != null ? jsonObject.get("opacity").getAsNumber() : null;
            Number cornerRadius = jsonObject.get("cornerRadius") != null ? jsonObject.get("cornerRadius").getAsNumber() : null;
            return new ShapeStyle(backgroundColor, opacity, cornerRadius);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException(
                    "Unable to parse json into type ShapeStyle",
                    e
            );
        }
    }


    private static final String FULL_OPACITY_STRING_HEXA = "ff";
    private static final float FULL_OPACITY_ALPHA = 1f;

    public boolean hasNonTranslucentColor() {
        if (backgroundColor == null || backgroundColor.length() < 2) {
            return false;
        }
        String alphaComponent = backgroundColor.substring(backgroundColor.length() - 2)
                .toLowerCase(Locale.US);
        return FULL_OPACITY_STRING_HEXA.equals(alphaComponent);
    }

    public boolean isFullyOpaque() {
        if (this.opacity == null) {
            return true; // 默认不透明
        }
        // 安全地转换Number为float，处理Double到Float的转换
        float opacityValue;
        if (this.opacity instanceof Double) {
            opacityValue = ((Double) this.opacity).floatValue();
        } else if (this.opacity instanceof Float) {
            opacityValue = (Float) this.opacity;
        } else {
            opacityValue = this.opacity.floatValue();
        }
        return opacityValue >= FULL_OPACITY_ALPHA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShapeStyle that = (ShapeStyle) o;
        return Objects.equals(backgroundColor, that.backgroundColor) && Objects.equals(opacity, that.opacity) && Objects.equals(cornerRadius, that.cornerRadius);
    }

    @Override
    public int hashCode() {
        return Objects.hash(backgroundColor, opacity, cornerRadius);
    }
}
