package com.ft.sdk.sessionreplay.internal.resources;

import com.ft.sdk.sessionreplay.model.ResourceHashesEntry;
import com.ft.sdk.storage.Serializer;

public class ResourceHashesEntrySerializer implements Serializer<ResourceHashesEntry> {

    @Override
    public String serialize(ResourceHashesEntry model) {
        return model.toJson().toString();
    }
}