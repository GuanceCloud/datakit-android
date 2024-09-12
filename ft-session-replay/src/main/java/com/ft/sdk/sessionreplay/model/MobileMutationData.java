package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class MobileMutationData extends MobileIncrementalData {
    public final List<Add> adds;
    public final List<Remove> removes;
    public final List<WireframeUpdateMutation> updates;
    public final long source = 0L;

    public MobileMutationData(List<Add> adds, List<Remove> removes, List<WireframeUpdateMutation> updates) {
        this.adds = adds;
        this.removes = removes;
        this.updates = updates;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("source", source);

        if (adds != null) {
            JsonArray addsArray = new JsonArray();
            for (Add add : adds) {
                addsArray.add(add.toJson());
            }
            json.add("adds", addsArray);
            if(addsArray.size()>0){
                System.out.println("MobileMutationData add:"+addsArray);
            }
        }

        if (removes != null) {
            JsonArray removesArray = new JsonArray();
            for (Remove remove : removes) {
                removesArray.add(remove.toJson());
            }
            json.add("removes", removesArray);
            if(removesArray.size()>0){
                System.out.println("MobileMutationData removes:"+removesArray);
            }
        }

        if (updates != null) {
            JsonArray updatesArray = new JsonArray();
            for (WireframeUpdateMutation update : updates) {
                updatesArray.add(update.toJson());
            }
            json.add("updates", updatesArray);
            if(updatesArray.size()>0){
                System.out.println("MobileMutationData updates:"+updatesArray);
            }
        }

        return json;
    }

    public static MobileMutationData fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type MobileMutationData", e);
        }
    }

    public static MobileMutationData fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            List<Add> adds = null;
            List<Remove> removes = null;
            List<WireframeUpdateMutation> updates = null;

            if (jsonObject.has("adds")) {
                adds = new ArrayList<>();
                JsonArray addsArray = jsonObject.getAsJsonArray("adds");
                for (JsonElement element : addsArray) {
                    adds.add(Add.fromJsonObject(element.getAsJsonObject()));
                }
            }

            if (jsonObject.has("removes")) {
                removes = new ArrayList<>();
                JsonArray removesArray = jsonObject.getAsJsonArray("removes");
                for (JsonElement element : removesArray) {
                    removes.add(Remove.fromJsonObject(element.getAsJsonObject()));
                }
            }

            if (jsonObject.has("updates")) {
                updates = new ArrayList<>();
                JsonArray updatesArray = jsonObject.getAsJsonArray("updates");
                for (JsonElement element : updatesArray) {
                    updates.add(WireframeUpdateMutation.fromJsonObject(element.getAsJsonObject()));
                }
            }

            return new MobileMutationData(adds, removes, updates);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type MobileMutationData", e);
        }
    }
}
