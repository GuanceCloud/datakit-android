package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public abstract class Wireframe {
    public abstract JsonElement toJson();

    public static Wireframe fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException(
                    "Unable to parse json into one of type Wireframe",
                    e
            );
        }
    }

    public static Wireframe fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        List<Throwable> errors = new ArrayList<>();
        Wireframe asShapeWireframe = null;
        try {
            asShapeWireframe = ShapeWireframe.fromJsonObject(jsonObject);
        } catch (JsonParseException e) {
            errors.add(e);
        }

        Wireframe asTextWireframe = null;
        try {
            asTextWireframe = TextWireframe.fromJsonObject(jsonObject);
        } catch (JsonParseException e) {
            errors.add(e);
        }

        Wireframe asImageWireframe = null;
        try {
            asImageWireframe = ImageWireframe.fromJsonObject(jsonObject);
        } catch (JsonParseException e) {
            errors.add(e);
        }

        Wireframe asPlaceholderWireframe = null;
        try {
            asPlaceholderWireframe = PlaceholderWireframe.fromJsonObject(jsonObject);
        } catch (JsonParseException e) {
            errors.add(e);
        }

        Wireframe asWebviewWireframe = null;
        try {
            asWebviewWireframe = WebviewWireframe.fromJsonObject(jsonObject);
        } catch (JsonParseException e) {
            errors.add(e);
        }

        Wireframe result = null;
        for (Wireframe wf : new Wireframe[]{
                asShapeWireframe,
                asTextWireframe,
                asImageWireframe,
                asPlaceholderWireframe,
                asWebviewWireframe
        }) {
            if (wf != null) {
                result = wf;
                break;
            }
        }

        if (result == null) {
            StringBuilder message = new StringBuilder("Unable to parse json into one of type \nWireframe\n");
            for (Throwable error : errors) {
                message.append(error.getMessage()).append("\n");
            }
            throw new JsonParseException(message.toString());
        }
        return result;
    }

    public abstract Wireframe setClip(WireframeClip clip);

    public abstract boolean hasOpaqueBackground();

    public abstract Long getId();


}
