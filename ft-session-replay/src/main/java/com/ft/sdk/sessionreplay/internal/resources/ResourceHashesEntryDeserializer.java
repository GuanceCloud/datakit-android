package com.ft.sdk.sessionreplay.internal.resources;

import com.ft.sdk.sessionreplay.model.ResourceHashesEntry;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.storage.Deserializer;
import com.google.gson.JsonParseException;

import java.util.Locale;

public class ResourceHashesEntryDeserializer implements Deserializer<String, ResourceHashesEntry> {

    private static final String TAG = "ResourceHashesEntryDese";
    private final InternalLogger internalLogger;

    public ResourceHashesEntryDeserializer(InternalLogger internalLogger) {
        this.internalLogger = internalLogger;
    }

    @Override
    public ResourceHashesEntry deserialize(String model) {
        try {
            return ResourceHashesEntry.fromJson(model);
        } catch (JsonParseException e) {
            internalLogger.e(TAG, String.format(DESERIALIZE_ERROR_MESSAGE_FORMAT, model));
            return null;
        }
    }

    public static final String DESERIALIZE_ERROR_MESSAGE_FORMAT =
            "Error while trying to deserialize the ResourceHashesEntry: %s";
}
