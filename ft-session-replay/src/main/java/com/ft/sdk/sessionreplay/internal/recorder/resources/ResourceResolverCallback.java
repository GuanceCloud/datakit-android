package com.ft.sdk.sessionreplay.internal.recorder.resources;

public interface ResourceResolverCallback {
    void onSuccess(String resourceId);
    
    void onFailure();
}
