package com.ft.sdk;

/**
 * Marks a request initiated by OkHttp's WebSocket API so it is not also reported as a generic
 * HTTP resource.
 */
final class FTWebSocketRequestTag {
    static final FTWebSocketRequestTag INSTANCE = new FTWebSocketRequestTag();

    private FTWebSocketRequestTag() {
    }
}
