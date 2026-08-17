package com.ft.sdk;

import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Explicit OkHttp WebSocket integration for RUM handshake Resource collection.
 *
 * <p>Use this API when the Guance Gradle Plugin does not instrument the WebSocket call site.
 * Collection follows {@link FTRUMConfig#setEnableTraceUserResource(boolean)}, RUM sampling, and
 * Resource URL filtering. Only the opening handshake is collected; connection lifetime and
 * message contents are not collected.</p>
 */
public final class FTWebSocket {

    private FTWebSocket() {
    }

    /**
     * Starts an OkHttp WebSocket with handshake Resource collection when enabled.
     *
     * @param factory OkHttp WebSocket factory, commonly an {@code OkHttpClient}
     * @param request WebSocket handshake request
     * @param listener application listener that receives all WebSocket callbacks
     * @return WebSocket returned by the supplied factory
     */
    public static WebSocket newWebSocket(WebSocket.Factory factory,
                                         Request request,
                                         WebSocketListener listener) {
        return FTWebSocketInstrumentation.newWebSocket(factory, request, listener);
    }

    /**
     * Wraps an OkHttp WebSocket factory so all calls use handshake Resource collection when
     * enabled. Wrapping an already wrapped factory returns the same instance.
     *
     * @param factory factory to wrap, commonly an {@code OkHttpClient}
     * @return an idempotently wrapped WebSocket factory
     */
    public static WebSocket.Factory wrap(WebSocket.Factory factory) {
        if (factory instanceof FTWebSocketFactory) {
            return factory;
        }
        return new FTWebSocketFactory(factory);
    }

    private static final class FTWebSocketFactory implements WebSocket.Factory {
        private final WebSocket.Factory delegate;

        private FTWebSocketFactory(WebSocket.Factory delegate) {
            this.delegate = delegate;
        }

        @Override
        public WebSocket newWebSocket(Request request, WebSocketListener listener) {
            return FTWebSocketInstrumentation.newWebSocket(delegate, request, listener);
        }
    }
}
