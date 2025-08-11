package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Utils;

/**
 * Used to generate a unique id during the request process
 */
public class ResourceID {
    private final String uuid = Utils.randomUUID();

    public String getUuid() {
        return uuid;
    }
}
