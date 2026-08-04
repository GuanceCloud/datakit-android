package com.ft.sdk;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Reports the WebSocket handshake terminal state while preserving application callbacks.
 */
final class FTWebSocketHandshakeListener extends WebSocketListener {
    private final WebSocketListener delegate;
    private final FTWebSocketHandshakeResource resource;

    FTWebSocketHandshakeListener(WebSocketListener delegate, FTWebSocketHandshakeResource resource) {
        this.delegate = delegate;
        this.resource = resource;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        resource.onOpen(response);
        if (delegate != null) {
            delegate.onOpen(webSocket, response);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        if (delegate != null) {
            delegate.onMessage(webSocket, text);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        if (delegate != null) {
            delegate.onMessage(webSocket, bytes);
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        if (delegate != null) {
            delegate.onClosing(webSocket, code, reason);
        }
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        if (delegate != null) {
            delegate.onClosed(webSocket, code, reason);
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        resource.onFailure(t, response);
        if (delegate != null) {
            delegate.onFailure(webSocket, t, response);
        }
    }
}
