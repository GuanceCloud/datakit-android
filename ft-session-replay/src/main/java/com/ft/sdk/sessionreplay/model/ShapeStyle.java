package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.Locale;

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
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
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
        return (float) (this.opacity != null ? this.opacity : 1f) >= FULL_OPACITY_ALPHA;
    }


}
