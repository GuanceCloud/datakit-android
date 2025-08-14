package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class Data3 {
    public Number height;
    public Number offsetLeft;
    public Number offsetTop;
    public Number pageLeft;
    public Number pageTop;
    public Number scale;
    public Number width;

    public Data3(Number height, Number offsetLeft, Number offsetTop, Number pageLeft, Number pageTop, Number scale, Number width) {
        this.height = height;
        this.offsetLeft = offsetLeft;
        this.offsetTop = offsetTop;
        this.pageLeft = pageLeft;
        this.pageTop = pageTop;
        this.scale = scale;
        this.width = width;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("height", height);
        json.addProperty("offsetLeft", offsetLeft);
        json.addProperty("offsetTop", offsetTop);
        json.addProperty("pageLeft", pageLeft);
        json.addProperty("pageTop", pageTop);
        json.addProperty("scale", scale);
        json.addProperty("width", width);
        return json;
    }

    public static Data3 fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type Data3", e);
        }
    }

    public static Data3 fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            Number height = jsonObject.get("height").getAsNumber();
            Number offsetLeft = jsonObject.get("offsetLeft").getAsNumber();
            Number offsetTop = jsonObject.get("offsetTop").getAsNumber();
            Number pageLeft = jsonObject.get("pageLeft").getAsNumber();
            Number pageTop = jsonObject.get("pageTop").getAsNumber();
            Number scale = jsonObject.get("scale").getAsNumber();
            Number width = jsonObject.get("width").getAsNumber();
            return new Data3(height, offsetLeft, offsetTop, pageLeft, pageTop, scale, width);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type Data3", e);
        }
    }
}
