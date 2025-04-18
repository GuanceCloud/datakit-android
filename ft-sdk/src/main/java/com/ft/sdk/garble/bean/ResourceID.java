package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Utils;

/**
 * 作为请求过程中生成唯一的qi
 */
public class ResourceID {
    private final String uuid = Utils.randomUUID();

    public String getUuid() {
        return uuid;
    }
}
