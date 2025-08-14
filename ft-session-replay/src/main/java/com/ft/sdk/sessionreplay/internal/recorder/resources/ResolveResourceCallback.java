package com.ft.sdk.sessionreplay.internal.recorder.resources;

public interface ResolveResourceCallback {
    void onResolved(String resourceId, byte[] resourceData);
    void onFailed();
}
