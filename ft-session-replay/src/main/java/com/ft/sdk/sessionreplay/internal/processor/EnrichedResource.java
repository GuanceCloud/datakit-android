/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.ft.sdk.sessionreplay.internal.processor;

import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Objects;

public class EnrichedResource {
    private final byte[] resource;
    private final String applicationId;
    private final String filename;

    public EnrichedResource(byte[] resource, String applicationId, String filename) {
        this.resource = resource;
        this.applicationId = applicationId;
        this.filename = filename;
    }

    public byte[] getResource() {
        return resource;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnrichedResource that = (EnrichedResource) o;
        return Arrays.equals(resource, that.resource) &&
               Objects.equals(applicationId, that.applicationId) &&
               Objects.equals(filename, that.filename);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(resource);
        result = 31 * result + Objects.hashCode(applicationId);
        result = 31 * result + Objects.hashCode(filename);
        return result;
    }

    public static final String APPLICATION_ID_KEY = "applicationId";
    public static final String FILENAME_KEY = "filename";

    public byte[] asBinaryMetadata() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(APPLICATION_ID_KEY, applicationId);
        jsonObject.addProperty(FILENAME_KEY, filename);
        return jsonObject.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
