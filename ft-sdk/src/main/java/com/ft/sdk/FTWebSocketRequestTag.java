package com.ft.sdk;

import com.ft.sdk.garble.bean.ResourceID;

import okhttp3.Request;

/**
 * Marks a request initiated by OkHttp's WebSocket API so it is not also reported as a generic
 * HTTP resource.
 */
final class FTWebSocketRequestTag {
    static final FTWebSocketRequestTag INSTANCE = new FTWebSocketRequestTag();

    private FTWebSocketRequestTag() {
    }

    static boolean isTracked(Request request) {
        return request.tag(FTWebSocketRequestTag.class) != null;
    }

    static Request prepare(Request request) {
        Request.Builder builder = request.newBuilder()
                .tag(FTWebSocketRequestTag.class, INSTANCE);
        if (request.tag(ResourceID.class) == null) {
            builder.tag(ResourceID.class, new ResourceID());
        }
        return builder.build();
    }
}
