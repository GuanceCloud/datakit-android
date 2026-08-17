package com.ft.sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.ft.sdk.garble.bean.ResourceID;
import com.ft.sdk.garble.utils.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class FTWebSocketIntegrationTest {
    private Field sdkInstanceField;
    private Field rumConfigField;

    @Before
    public void setUp() throws Exception {
        sdkInstanceField = FTSdk.class.getDeclaredField("mFtSdk");
        sdkInstanceField.setAccessible(true);
        Constructor<FTSdk> constructor = FTSdk.class.getDeclaredConstructor(FTSDKConfig.class);
        constructor.setAccessible(true);
        sdkInstanceField.set(null, constructor.newInstance(FTSDKConfig.builder()));

        rumConfigField = FTRUMConfigManager.class.getDeclaredField("config");
        rumConfigField.setAccessible(true);
        setRumConfig(enabledConfig());
    }

    @After
    public void tearDown() throws Exception {
        sdkInstanceField.set(null, null);
        rumConfigField.set(FTRUMConfigManager.get(), null);
        FTRUMGlobalManager.get().release();
    }

    @Test
    public void automaticExplicitAndWrappedEntriesUseEquivalentInstrumentation() {
        assertCollectedEntry(new EntryCall() {
            @Override
            public void invoke(WebSocket.Factory factory, Request request,
                               WebSocketListener listener) {
                FTAutoTrack.trackOkHttpNewWebSocket(factory, request, listener);
            }
        });
        assertCollectedEntry(new EntryCall() {
            @Override
            public void invoke(WebSocket.Factory factory, Request request,
                               WebSocketListener listener) {
                FTWebSocket.newWebSocket(factory, request, listener);
            }
        });
        assertCollectedEntry(new EntryCall() {
            @Override
            public void invoke(WebSocket.Factory factory, Request request,
                               WebSocketListener listener) {
                FTWebSocket.wrap(factory).newWebSocket(request, listener);
            }
        });
    }

    @Test
    public void pluginBridgeAndWrappedFactoryCreateOnlyOneListenerLayer() throws Exception {
        RecordingFactory factory = new RecordingFactory();
        WebSocket.Factory wrapped = FTWebSocket.wrap(factory);
        CountingListener listener = new CountingListener();

        FTAutoTrack.trackOkHttpNewWebSocket(wrapped, request("mixed"), listener);

        assertEquals(1, factory.requests.size());
        WebSocketListener captured = factory.listeners.get(0);
        assertTrue(captured instanceof FTWebSocketHandshakeListener);
        assertSame(listener, listenerDelegate(captured));
    }

    @Test
    public void wrappingIsIdempotent() {
        WebSocket.Factory wrapped = FTWebSocket.wrap(new RecordingFactory());

        assertSame(wrapped, FTWebSocket.wrap(wrapped));
    }

    @Test
    public void trackedRequestFallsThroughWithoutAnotherListenerOrIdentity() {
        RecordingFactory factory = new RecordingFactory();
        CountingListener listener = new CountingListener();
        Request tracked = FTWebSocketRequestTag.prepare(request("already-tracked"));
        ResourceID resourceID = tracked.tag(ResourceID.class);

        FTWebSocket.newWebSocket(factory, tracked, listener);

        assertSame(tracked, factory.requests.get(0));
        assertSame(resourceID, factory.requests.get(0).tag(ResourceID.class));
        assertSame(listener, factory.listeners.get(0));
    }

    @Test
    public void urlFilterForwardsOriginalListenerWithTaggedRequest() throws Exception {
        setRumConfig(enabledConfig().setResourceUrlHandler(new FTInTakeUrlHandler() {
            @Override
            public boolean isInTakeUrl(String url) {
                return true;
            }
        }));
        assertForwardedWithoutCollection();
    }

    @Test
    public void rumDisabledForwardsOriginalListenerWithTaggedRequest() throws Exception {
        setRumConfig(new FTRUMConfig().setRumAppId(null).setEnableTraceUserResource(true));
        assertForwardedWithoutCollection();
    }

    @Test
    public void automaticResourceDisabledForwardsOriginalListenerWithTaggedRequest()
            throws Exception {
        setRumConfig(new FTRUMConfig().setRumAppId("app-id")
                .setEnableTraceUserResource(false));
        assertForwardedWithoutCollection();
    }

    @Test
    public void synchronousFactoryExceptionCompletesHandshakeAndIsRethrown() throws Exception {
        final IllegalStateException expected = new IllegalStateException("factory failed");
        ThrowingFactory factory = new ThrowingFactory(expected);

        try {
            FTWebSocket.newWebSocket(factory, request("throw"), new CountingListener());
            fail("Expected the factory exception");
        } catch (IllegalStateException actual) {
            assertSame(expected, actual);
        }

        assertTrue(factory.listener instanceof FTWebSocketHandshakeListener);
        assertTrue(resourceFinished(factory.listener));
    }

    @Test
    public void listenerForwardsEveryCallbackOnceAndFinishesOnlyHandshake() throws Exception {
        RecordingFactory factory = new RecordingFactory();
        CountingListener listener = new CountingListener();
        FTWebSocket.newWebSocket(factory, request("callbacks"), listener);
        WebSocketListener captured = factory.listeners.get(0);
        Response switchingProtocols = response(factory.requests.get(0), 101,
                "Switching Protocols");

        captured.onOpen(null, switchingProtocols);
        captured.onMessage(null, "text");
        captured.onMessage(null, ByteString.encodeUtf8("bytes"));
        captured.onClosing(null, 1000, "closing");
        captured.onClosed(null, 1000, "closed");
        captured.onFailure(null, new IOException("after open"), null);

        assertEquals(1, listener.openCount.get());
        assertEquals(1, listener.textMessageCount.get());
        assertEquals(1, listener.byteMessageCount.get());
        assertEquals(1, listener.closingCount.get());
        assertEquals(1, listener.closedCount.get());
        assertEquals(1, listener.failureCount.get());
        assertTrue(resourceFinished(captured));
    }

    @Test
    public void rejectedAndTransportFailureCallbacksCompleteTheirIndependentHandshakes()
            throws Exception {
        RecordingFactory factory = new RecordingFactory();

        FTWebSocket.newWebSocket(factory, request("rejected"), new CountingListener());
        WebSocketListener rejected = factory.listeners.get(0);
        rejected.onFailure(null, new IOException("forbidden"),
                response(factory.requests.get(0), 403, "Forbidden"));

        FTWebSocket.newWebSocket(factory, request("failed-with-response"), new CountingListener());
        WebSocketListener failedWithResponse = factory.listeners.get(1);
        failedWithResponse.onFailure(null, new IOException("bad upgrade"),
                response(factory.requests.get(1), 101, "Switching Protocols"));

        FTWebSocket.newWebSocket(factory, request("failed-without-response"),
                new CountingListener());
        WebSocketListener failedWithoutResponse = factory.listeners.get(2);
        failedWithoutResponse.onFailure(null, new IOException("network failure"), null);

        assertTrue(resourceFinished(rejected));
        assertTrue(resourceFinished(failedWithResponse));
        assertTrue(resourceFinished(failedWithoutResponse));
        assertEquals(FTWebSocketHandshakeResource.HANDSHAKE_STATE_REJECTED,
                FTWebSocketHandshakeResource.getFailureState(
                        response(factory.requests.get(0), 403, "Forbidden")));
        assertEquals(FTWebSocketHandshakeResource.HANDSHAKE_STATE_FAILED,
                FTWebSocketHandshakeResource.getFailureState(
                        response(factory.requests.get(1), 101, "Switching Protocols")));
        assertEquals(FTWebSocketHandshakeResource.HANDSHAKE_STATE_FAILED,
                FTWebSocketHandshakeResource.getFailureState(null));
    }

    @Test
    public void concurrentHandshakesKeepIndependentResourceIds() throws Exception {
        final int handshakeCount = 20;
        final RecordingFactory factory = new RecordingFactory();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        final CountDownLatch done = new CountDownLatch(handshakeCount);

        for (int i = 0; i < handshakeCount; i++) {
            final int index = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        FTWebSocket.newWebSocket(factory, request("concurrent-" + index),
                                new CountingListener());
                    } finally {
                        done.countDown();
                    }
                }
            });
        }

        assertTrue(done.await(5, TimeUnit.SECONDS));
        executor.shutdownNow();
        Set<String> resourceIds = new HashSet<>();
        for (Request request : factory.requests) {
            resourceIds.add(Utils.identifyRequest(request));
        }
        assertEquals(handshakeCount, factory.requests.size());
        assertEquals(handshakeCount, resourceIds.size());
    }

    @Test
    public void taggedWebSocketRequestSkipsGenericInterceptorAndEventListener() throws Exception {
        Request tagged = FTWebSocketRequestTag.prepare(request("generic-suppression"));
        RecordingChain chain = new RecordingChain(tagged);

        Response response = new FTResourceInterceptor().intercept(chain);

        assertSame(chain.response, response);
        assertEquals(1, chain.proceedCount);

        final EventListener original = new EventListener() {
        };
        EventListener.Factory originalFactory = new EventListener.Factory() {
            @Override
            public EventListener create(Call call) {
                return original;
            }
        };
        FTResourceEventListener.FTFactory factory = new FTResourceEventListener.FTFactory(
                false, null, originalFactory);
        Call call = new OkHttpClient().newCall(tagged);
        assertSame(original, factory.create(call));
    }

    private void assertCollectedEntry(EntryCall entryCall) {
        RecordingFactory factory = new RecordingFactory();
        CountingListener listener = new CountingListener();
        Request original = request("entry");

        entryCall.invoke(factory, original, listener);

        assertEquals(1, factory.requests.size());
        Request capturedRequest = factory.requests.get(0);
        assertFalse(capturedRequest == original);
        assertTrue(FTWebSocketRequestTag.isTracked(capturedRequest));
        assertNotNull(capturedRequest.tag(ResourceID.class));
        assertTrue(factory.listeners.get(0) instanceof FTWebSocketHandshakeListener);
    }

    private void assertForwardedWithoutCollection() {
        RecordingFactory factory = new RecordingFactory();
        CountingListener listener = new CountingListener();

        FTWebSocket.newWebSocket(factory, request("not-collected"), listener);

        assertTrue(FTWebSocketRequestTag.isTracked(factory.requests.get(0)));
        assertSame(listener, factory.listeners.get(0));
    }

    private void setRumConfig(FTRUMConfig config) throws IllegalAccessException {
        rumConfigField.set(FTRUMConfigManager.get(), config);
    }

    private static FTRUMConfig enabledConfig() {
        return new FTRUMConfig().setRumAppId("app-id").setEnableTraceUserResource(true);
    }

    private static Request request(String path) {
        return new Request.Builder().url("wss://example.com/" + path).build();
    }

    private static Response response(Request request, int code, String message) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(message)
                .build();
    }

    private static Object listenerDelegate(WebSocketListener listener) throws Exception {
        Field delegate = FTWebSocketHandshakeListener.class.getDeclaredField("delegate");
        delegate.setAccessible(true);
        return delegate.get(listener);
    }

    private static boolean resourceFinished(WebSocketListener listener) throws Exception {
        Field resourceField = FTWebSocketHandshakeListener.class.getDeclaredField("resource");
        resourceField.setAccessible(true);
        Object resource = resourceField.get(listener);
        Field finishedField = FTWebSocketHandshakeResource.class.getDeclaredField("finished");
        finishedField.setAccessible(true);
        return ((AtomicBoolean) finishedField.get(resource)).get();
    }

    private interface EntryCall {
        void invoke(WebSocket.Factory factory, Request request, WebSocketListener listener);
    }

    private static final class RecordingFactory implements WebSocket.Factory {
        private final List<Request> requests = new CopyOnWriteArrayList<>();
        private final List<WebSocketListener> listeners = new CopyOnWriteArrayList<>();

        @Override
        public WebSocket newWebSocket(Request request, WebSocketListener listener) {
            requests.add(request);
            listeners.add(listener);
            return null;
        }
    }

    private static final class ThrowingFactory implements WebSocket.Factory {
        private final RuntimeException exception;
        private WebSocketListener listener;

        private ThrowingFactory(RuntimeException exception) {
            this.exception = exception;
        }

        @Override
        public WebSocket newWebSocket(Request request, WebSocketListener listener) {
            this.listener = listener;
            throw exception;
        }
    }

    private static final class CountingListener extends WebSocketListener {
        private final AtomicInteger openCount = new AtomicInteger();
        private final AtomicInteger textMessageCount = new AtomicInteger();
        private final AtomicInteger byteMessageCount = new AtomicInteger();
        private final AtomicInteger closingCount = new AtomicInteger();
        private final AtomicInteger closedCount = new AtomicInteger();
        private final AtomicInteger failureCount = new AtomicInteger();

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            openCount.incrementAndGet();
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            textMessageCount.incrementAndGet();
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            byteMessageCount.incrementAndGet();
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            closingCount.incrementAndGet();
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            closedCount.incrementAndGet();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable throwable, Response response) {
            failureCount.incrementAndGet();
        }
    }

    private static final class RecordingChain implements Interceptor.Chain {
        private final Request request;
        private final Response response;
        private int proceedCount;

        private RecordingChain(Request request) {
            this.request = request;
            this.response = response(request, 200, "OK");
        }

        @Override
        public Request request() {
            return request;
        }

        @Override
        public Response proceed(Request request) {
            proceedCount++;
            return response;
        }

        @Override
        public Connection connection() {
            return null;
        }

        @Override
        public Call call() {
            return null;
        }

        @Override
        public int connectTimeoutMillis() {
            return 0;
        }

        @Override
        public Interceptor.Chain withConnectTimeout(int timeout, TimeUnit unit) {
            return this;
        }

        @Override
        public int readTimeoutMillis() {
            return 0;
        }

        @Override
        public Interceptor.Chain withReadTimeout(int timeout, TimeUnit unit) {
            return this;
        }

        @Override
        public int writeTimeoutMillis() {
            return 0;
        }

        @Override
        public Interceptor.Chain withWriteTimeout(int timeout, TimeUnit unit) {
            return this;
        }
    }
}
