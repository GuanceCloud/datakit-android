package com.ft.sdk;

import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Shared OkHttp WebSocket handshake Resource instrumentation used by automatic and explicit
 * integration entry points.
 */
final class FTWebSocketInstrumentation {

    private FTWebSocketInstrumentation() {
    }

    static WebSocket newWebSocket(WebSocket.Factory factory,
                                  Request request,
                                  WebSocketListener listener) {
        if (FTWebSocketRequestTag.isTracked(request)) {
            return factory.newWebSocket(request, listener);
        }

        Request webSocketRequest = FTWebSocketRequestTag.prepare(request);
        if (!FTSdk.checkInstallState() || !FTRUMConfigManager.get().isRumEnable()) {
            return factory.newWebSocket(webSocketRequest, listener);
        }

        FTRUMConfig config = FTRUMConfigManager.get().getConfig();
        if (!config.isEnableTraceUserResource()
                || config.getResourceUrlHandler().isInTakeUrl(request.url().toString())) {
            return factory.newWebSocket(webSocketRequest, listener);
        }

        FTWebSocketHandshakeResource resource = new FTWebSocketHandshakeResource(webSocketRequest);
        try {
            return factory.newWebSocket(webSocketRequest,
                    new FTWebSocketHandshakeListener(listener, resource));
        } catch (RuntimeException e) {
            resource.onFailure(e, null);
            throw e;
        }
    }
}
