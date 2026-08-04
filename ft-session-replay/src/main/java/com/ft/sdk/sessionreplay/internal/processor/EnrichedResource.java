/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.ft.sdk.sessionreplay.internal.processor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class EnrichedResource {
    private final byte[] resource;
    private final String filename;
    private final Map<String, Object> globalContext;

    public EnrichedResource(byte[] resource, String filename) {
        this(resource, filename, null);
    }

    public EnrichedResource(byte[] resource, String filename, Map<String, Object> globalContext) {
        this.resource = resource;
        this.filename = filename;
        this.globalContext = globalContext == null
                ? Collections.<String, Object>emptyMap()
                : Collections.unmodifiableMap(new HashMap<>(globalContext));
    }

    public byte[] getResource() {
        return resource;
    }


    public String getFilename() {
        return filename;
    }

    public Map<String, Object> getGlobalContext() {
        return globalContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnrichedResource that = (EnrichedResource) o;
        return Arrays.equals(resource, that.resource)  &&
               Objects.equals(filename, that.filename);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(resource);
        result = 31 * result + Objects.hashCode(filename);
        return result;
    }

    public static final String APPLICATION_ID_KEY = "applicationId";
    public static final String FILENAME_KEY = "filename";
    public static final String GLOBAL_CONTEXT_KEY = "globalContext";

    public static String extractFileName(byte[] metadata) {
        if (metadata == null || metadata.length == 0) {
            return null;
        }
        try {
            JsonObject json = new Gson().fromJson(new String(metadata), JsonObject.class);
            if (json != null && json.has(FILENAME_KEY)) {
                return json.get(FILENAME_KEY).getAsString();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    public static String extractApplicationId(byte[] metadata) {
        if (metadata == null || metadata.length == 0) {
            return null;
        }
        try {
            JsonObject json = new Gson().fromJson(new String(metadata), JsonObject.class);
            if (json != null && json.has(APPLICATION_ID_KEY)) {
                return json.get(APPLICATION_ID_KEY).getAsString();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    /**
     * Resource metadata stores the replay link context captured when the resource entered the
     * queue so upload requests can forward the same filtered tags later.
     */
    public static Map<String, Object> extractGlobalContext(byte[] metadata) {
        Map<String, Object> globalContext = new ConcurrentHashMap<>();
        if (metadata == null || metadata.length == 0) {
            return globalContext;
        }
        try {
            JsonObject json = new Gson().fromJson(new String(metadata), JsonObject.class);
            if (json != null && json.has(GLOBAL_CONTEXT_KEY) && json.get(GLOBAL_CONTEXT_KEY).isJsonObject()) {
                JsonObject contextJson = json.getAsJsonObject(GLOBAL_CONTEXT_KEY);
                for (String key : contextJson.keySet()) {
                    JsonElement value = contextJson.get(key);
                    if (value == null || value.isJsonNull()) {
                        globalContext.put(key, null);
                    } else if (value.isJsonPrimitive()) {
                        if (value.getAsJsonPrimitive().isBoolean()) {
                            globalContext.put(key, value.getAsBoolean());
                        } else if (value.getAsJsonPrimitive().isNumber()) {
                            globalContext.put(key, value.getAsNumber());
                        } else if (value.getAsJsonPrimitive().isString()) {
                            globalContext.put(key, value.getAsString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return globalContext;
    }

    public byte[] asBinaryMetadata(String applicationId) {
        return asBinaryMetadata(applicationId, null);
    }

    /**
     * Persist the resource filename together with the captured replay link context. The context
     * is intentionally serialized into metadata instead of re-reading the current RUM state at
     * upload time, so view-scoped properties are preserved.
     */
    public byte[] asBinaryMetadata(String applicationId, Map<String, Object> globalContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(APPLICATION_ID_KEY, applicationId);
        jsonObject.addProperty(FILENAME_KEY, filename);
        if (globalContext != null && !globalContext.isEmpty()) {
            JsonObject contextJson = new JsonObject();
            for (Map.Entry<String, Object> entry : globalContext.entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    contextJson.add(entry.getKey(), JsonNull.INSTANCE);
                } else if (value instanceof Boolean) {
                    contextJson.addProperty(entry.getKey(), (Boolean) value);
                } else if (value instanceof Number) {
                    contextJson.addProperty(entry.getKey(), (Number) value);
                } else {
                    contextJson.addProperty(entry.getKey(), String.valueOf(value));
                }
            }
            jsonObject.add(GLOBAL_CONTEXT_KEY, contextJson);
        }
        return jsonObject.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
