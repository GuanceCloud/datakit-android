/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.ft.sdk.sessionreplay.internal.processor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Objects;

public class EnrichedResource {
    private final byte[] resource;
    private final String filename;

    public EnrichedResource(byte[] resource, String filename) {
        this.resource = resource;
        this.filename = filename;
    }

    public byte[] getResource() {
        return resource;
    }


    public String getFilename() {
        return filename;
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

    public byte[] asBinaryMetadata(String applicationId) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(APPLICATION_ID_KEY, applicationId);
        jsonObject.addProperty(FILENAME_KEY, filename);
        return jsonObject.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
