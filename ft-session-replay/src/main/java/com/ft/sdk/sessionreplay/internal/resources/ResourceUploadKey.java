package com.ft.sdk.sessionreplay.internal.resources;

import com.ft.sdk.sessionreplay.utils.SessionReplayRumContext;

import java.util.Map;
import java.util.Objects;

/**
 * Identifies a replay resource within one upload routing target.
 */
public final class ResourceUploadKey {
    public static final String WGT_ID_KEY = "wgt_id";
    private static final String STORAGE_VERSION = "v2";

    private final String resourceHash;
    private final String wgtId;

    private ResourceUploadKey(String resourceHash, String wgtId) {
        this.resourceHash = resourceHash;
        this.wgtId = wgtId == null ? "" : wgtId;
    }

    public static ResourceUploadKey from(String resourceHash, SessionReplayRumContext rumContext) {
        Map<String, Object> globalContext = rumContext == null ? null : rumContext.getGlobalContext();
        return from(resourceHash, globalContext);
    }

    public static ResourceUploadKey from(String resourceHash, Map<String, Object> globalContext) {
        return new ResourceUploadKey(resourceHash, extractWgtId(globalContext));
    }

    public static ResourceUploadKey main(String resourceHash) {
        return new ResourceUploadKey(resourceHash, "");
    }

    public static String extractWgtId(Map<String, Object> globalContext) {
        if (globalContext == null) {
            return "";
        }
        Object value = globalContext.get(WGT_ID_KEY);
        return value == null ? "" : String.valueOf(value);
    }

    public boolean isRouted() {
        return !wgtId.isEmpty();
    }

    public String toStorageKey() {
        return STORAGE_VERSION + "|" + wgtId.length() + "|" + wgtId + "|" + resourceHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceUploadKey that = (ResourceUploadKey) o;
        return Objects.equals(resourceHash, that.resourceHash) && Objects.equals(wgtId, that.wgtId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceHash, wgtId);
    }
}
