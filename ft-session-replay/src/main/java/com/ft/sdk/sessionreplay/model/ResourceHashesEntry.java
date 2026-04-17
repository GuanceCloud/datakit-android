package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResourceHashesEntry {

    public final long lastUpdateDateNs;
    public final List<String> resourceHashes;

    public ResourceHashesEntry(long lastUpdateDateNs, List<String> resourceHashes) {
        this.lastUpdateDateNs = lastUpdateDateNs;
        this.resourceHashes = Collections.unmodifiableList(new ArrayList<>(resourceHashes));
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("last_update_date_ns", lastUpdateDateNs);
        JsonArray resourceHashesArray = new JsonArray();
        for (String resourceHash : resourceHashes) {
            resourceHashesArray.add(new JsonPrimitive(resourceHash));
        }
        json.add("resource_hashes", resourceHashesArray);
        return json;
    }

    public static ResourceHashesEntry fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (JsonSyntaxException e) {
            throw new JsonParseException("Unable to parse json into type ResourceHashesEntry", e);
        }
    }

    public static ResourceHashesEntry fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            long lastUpdateDateNs = jsonObject.get("last_update_date_ns").getAsLong();
            List<String> resourceHashes = new ArrayList<>();
            JsonArray jsonArray = jsonObject.get("resource_hashes").getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                resourceHashes.add(jsonArray.get(i).getAsString());
            }
            return new ResourceHashesEntry(lastUpdateDateNs, resourceHashes);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type ResourceHashesEntry", e);
        } catch (NumberFormatException e) {
            throw new JsonParseException("Unable to parse json into type ResourceHashesEntry", e);
        }
    }

    public long getLastUpdateDateNs() {
        return lastUpdateDateNs;
    }

    public List<String> getResourceHashes() {
        return resourceHashes;
    }
}
