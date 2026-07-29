package com.ft;

import com.ft.sdk.FTResourceEventListener;
import com.ft.sdk.FTResourceInterceptor;
import com.ft.sdk.FTRUMInnerManager;
import com.ft.sdk.garble.bean.ResourceBean;
import com.ft.sdk.garble.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.EventListener;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Reproduces the timing problem caused by identical queued OkHttp requests sharing one resource id.
 *
 * <p>The scenario uses a real loopback HTTP server and OkHttp's real Dispatcher. Reflection is used
 * only to observe the ResourceBean selected by the SDK; it does not create, remove, or modify beans.
 */
final class ResourceQueueTimingRepro implements Closeable {

    private static final String REPRO_API_PATH = "/api/v1/test/resource-queue";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final long RESOURCE_REMOVAL_TIMEOUT_MS = 10_000L;
    private static final long OBSERVED_TIMESTAMP_NEAR_END_NS = TimeUnit.SECONDS.toNanos(1);

    interface Listener {
        void onProgress(String message);

        void onComplete(String result);
    }

    private final AtomicBoolean finished = new AtomicBoolean();
    private final AtomicReference<ResourceBean> secondResourceBean = new AtomicReference<>();
    private final CallTiming secondTiming = new CallTiming();

    private volatile LoopbackHttpServer server;
    private volatile OkHttpClient client;
    private volatile Call firstCall;
    private volatile Call secondCall;

    void start(int queueHoldSeconds, Listener listener) {
        long queueHoldMillis = TimeUnit.SECONDS.toMillis(queueHoldSeconds);
        ResourceBeanObserver beanObserver;
        try {
            beanObserver = new ResourceBeanObserver();
            server = new LoopbackHttpServer(queueHoldMillis);
            server.start();
        } catch (Exception e) {
            complete(listener, "复现场景启动失败：\n" + stackMessage(e), true);
            return;
        }

        listener.onProgress(String.format(
                Locale.US,
                "本地 HTTP 服务已启动，第一条请求将占用 Dispatcher %d 秒。",
                queueHoldSeconds
        ));

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(1);
        dispatcher.setMaxRequestsPerHost(1);

        AtomicInteger listenerSequence = new AtomicInteger();
        EventListener.Factory timingFactory = call ->
                listenerSequence.incrementAndGet() == 2
                        ? new TimingEventListener(secondTiming)
                        : new EventListener() {
                        };

        AtomicInteger interceptorSequence = new AtomicInteger();
        Interceptor beanCaptureInterceptor = chain -> {
            int callSequence = interceptorSequence.incrementAndGet();
            Response response = chain.proceed(chain.request());
            if (callSequence == 2) {
                secondResourceBean.compareAndSet(
                        null,
                        beanObserver.get(Utils.identifyRequest(chain.request()))
                );
            }
            return response;
        };

        client = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .addInterceptor(beanCaptureInterceptor)
                .addInterceptor(new FTResourceInterceptor())
                .eventListenerFactory(new FTResourceEventListener.FTFactory(
                        false,
                        null,
                        timingFactory
                ))
                // A 600-second reproduction must not be converted into a read-timeout test.
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        String url = "http://localhost:" + server.getPort() + REPRO_API_PATH;
        Request firstRequest = createRequest(url);
        Request secondRequest = createRequest(url);
        String resourceId = Utils.identifyRequest(firstRequest);

        firstCall = client.newCall(firstRequest);
        secondCall = client.newCall(secondRequest);
        ResourceBean sharedResourceBean = beanObserver.get(resourceId);
        if (sharedResourceBean == null) {
            complete(listener, "复现场景启动失败：创建 Call 后未找到 ResourceBean。", true);
            return;
        }

        listener.onProgress("准备连续 enqueue 两条 method、URL、body 完全相同的请求。");
        listener.onProgress("两条请求的内部 resourceId 相同，第二条会在 OkHttp Dispatcher 中排队。");

        firstCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                complete(listener, "第一条请求失败：\n" + stackMessage(e), true);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (Response ignored = response) {
                    consumeBody(response);
                    listener.onProgress("第一条慢响应读取完成，等待 SDK 自然移除共享 ResourceBean。");
                    if (!waitUntilResourceRemoved(beanObserver, resourceId, sharedResourceBean)) {
                        complete(
                                listener,
                                "未在限定时间内观察到 SDK 移除第一条 ResourceBean，无法继续确定性复现。",
                                true
                        );
                    }
                } catch (Exception e) {
                    complete(listener, "读取第一条响应失败：\n" + stackMessage(e), true);
                }
            }
        });

        secondCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                complete(listener, "第二条请求失败：\n" + stackMessage(e), true);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (Response ignored = response) {
                    consumeBody(response);
                    long completionTimestamp = Utils.getCurrentNanoTime();
                    ResourceBean bean = secondResourceBean.get();
                    if (bean == null) {
                        bean = beanObserver.get(resourceId);
                    }
                    if (bean == null) {
                        complete(listener, "第二条请求完成，但未观察到对应 ResourceBean。", true);
                        return;
                    }
                    complete(
                            listener,
                            buildResult(
                                    queueHoldMillis,
                                    resourceId,
                                    sharedResourceBean,
                                    bean,
                                    completionTimestamp
                            ),
                            false
                    );
                } catch (Exception e) {
                    complete(listener, "读取第二条响应失败：\n" + stackMessage(e), true);
                }
            }
        });
    }

    private Request createRequest(String url) {
        RequestBody body = RequestBody.create(JSON, "{\"token\":\"same-request\"}");
        return new Request.Builder()
                .url(url)
                .post(body)
                .build();
    }

    private boolean waitUntilResourceRemoved(
            ResourceBeanObserver observer,
            String resourceId,
            ResourceBean expected
    ) throws InterruptedException {
        long deadline = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(RESOURCE_REMOVAL_TIMEOUT_MS);
        while (!finished.get() && System.nanoTime() < deadline) {
            ResourceBean current = observer.get(resourceId);
            if (current == null || current != expected) {
                return true;
            }
            Thread.sleep(10L);
        }
        return false;
    }

    private String buildResult(
            long queueHoldMillis,
            String resourceId,
            ResourceBean firstBean,
            ResourceBean secondBean,
            long completionTimestamp
    ) {
        long measuredDuration = secondBean.resourceLoad;
        if (measuredDuration <= 0
                && secondTiming.callEndNano > secondTiming.callStartNano) {
            measuredDuration = secondTiming.callEndNano - secondTiming.callStartNano;
        }

        long beanTimestampAge = completionTimestamp - secondBean.startTime;
        long beanCreatedAfterCallStart =
                secondBean.startTime - secondTiming.callStartTimestamp;
        long knownDuration = positive(secondBean.resourceDNS)
                + positive(secondBean.resourceTCP)
                + positive(secondBean.resourceSSL)
                + positive(secondBean.resourceFirstByte)
                + positive(secondBean.resourceDownloadTime);
        long estimatedUnknown = Math.max(0L, measuredDuration - knownDuration);
        long firstNetworkOffset = firstPositive(
                secondBean.resourceDNSStart,
                secondBean.resourceTCPStart,
                secondBean.resourceFirstByteStart
        );

        boolean beanWasRecreated = firstBean != secondBean;
        long expectedLongPhase = TimeUnit.MILLISECONDS.toNanos(
                Math.max(1L, queueHoldMillis * 8L / 10L)
        );
        boolean queueWasMeasured = measuredDuration >= expectedLongPhase;
        boolean networkStartedAfterQueue = firstNetworkOffset >= expectedLongPhase;
        boolean unknownWasLong = estimatedUnknown >= expectedLongPhase;
        boolean timestampNearCompletion = beanTimestampAge >= 0
                && beanTimestampAge < OBSERVED_TIMESTAMP_NEAR_END_NS;
        boolean reproduced = beanWasRecreated
                && queueWasMeasured
                && networkStartedAfterQueue
                && unknownWasLong
                && timestampNearCompletion;

        StringBuilder result = new StringBuilder();
        result.append(reproduced ? "结论：已复现\n\n" : "结论：本次未满足全部复现条件\n\n");
        result.append("场景\n");
        result.append("• Dispatcher maxRequests/maxRequestsPerHost = 1\n");
        result.append("• 两条 POST 的 method、URL、body、Content-Type 相同\n");
        result.append("• 第一条真实响应占用队列 ")
                .append(formatMillis(queueHoldMillis))
                .append("\n\n");

        result.append("第二条请求观测值\n");
        result.append("• Call 总时长：").append(formatNanos(measuredDuration)).append("\n");
        result.append("• 首个网络阶段开始偏移：")
                .append(formatOptionalNanos(firstNetworkOffset))
                .append("\n");
        result.append("• DNS start：")
                .append(formatOptionalNanos(secondBean.resourceDNSStart))
                .append("\n");
        result.append("• Connect start：")
                .append(formatOptionalNanos(secondBean.resourceTCPStart))
                .append("\n");
        result.append("• First byte start：")
                .append(formatOptionalNanos(secondBean.resourceFirstByteStart))
                .append("\n");
        result.append("• Download start：")
                .append(formatOptionalNanos(secondBean.resourceDownloadTimeStart))
                .append("\n");
        result.append("• 推算 Unknown：").append(formatNanos(estimatedUnknown)).append("\n\n");

        result.append("ResourceBean 观测值\n");
        result.append("• 两条 Call 最初共享同一 resourceId：是\n");
        result.append("• 第一条结束后 ResourceBean 被重建：")
                .append(beanWasRecreated ? "是" : "否")
                .append("\n");
        result.append("• data_ns（ResourceBean.startTime）距第二条完成：")
                .append(formatNanos(beanTimestampAge))
                .append("\n");
        result.append("• ResourceBean.startTime 距 callStart：")
                .append(formatNanos(beanCreatedAfterCallStart))
                .append("\n");
        result.append("• resourceId：").append(resourceId).append("\n\n");

        result.append("解释\n");
        result.append("第二条 Call 的计时从 enqueue/callStart 开始，因此包含 Dispatcher 排队；")
                .append("但它开始执行时，共享 Bean 已被第一条请求移除，FTResourceInterceptor ")
                .append("在收到响应后重新创建 Bean，所以 data_ns 接近完成时间，")
                .append("而排队时间落入 Unknown。反射只读取 Bean，没有改动 SDK 状态。");
        return result.toString();
    }

    private static void consumeBody(Response response) throws IOException {
        ResponseBody body = response.body();
        if (body != null) {
            body.string();
        }
    }

    private void complete(Listener listener, String result, boolean cancelCalls) {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        listener.onComplete(result);
        cleanup(cancelCalls);
    }

    private void cleanup(boolean cancelCalls) {
        Call currentFirstCall = firstCall;
        Call currentSecondCall = secondCall;
        if (cancelCalls) {
            if (currentFirstCall != null) {
                currentFirstCall.cancel();
            }
            if (currentSecondCall != null) {
                currentSecondCall.cancel();
            }
        }

        LoopbackHttpServer currentServer = server;
        if (currentServer != null) {
            currentServer.close();
        }

        OkHttpClient currentClient = client;
        if (currentClient != null) {
            currentClient.connectionPool().evictAll();
            currentClient.dispatcher().executorService().shutdown();
        }
    }

    @Override
    public void close() {
        if (finished.compareAndSet(false, true)) {
            cleanup(true);
        }
    }

    private static long positive(long value) {
        return Math.max(0L, value);
    }

    private static long firstPositive(long... values) {
        for (long value : values) {
            if (value >= 0) {
                return value;
            }
        }
        return -1L;
    }

    private static String formatOptionalNanos(long nanoseconds) {
        return nanoseconds < 0 ? "未记录" : formatNanos(nanoseconds);
    }

    private static String formatNanos(long nanoseconds) {
        if (nanoseconds < 0) {
            return "未记录";
        }
        return String.format(Locale.US, "%.3f s", nanoseconds / 1_000_000_000d);
    }

    private static String formatMillis(long milliseconds) {
        return String.format(Locale.US, "%.3f s", milliseconds / 1_000d);
    }

    private static String stackMessage(Exception exception) {
        String message = exception.getMessage();
        return exception.getClass().getSimpleName()
                + (message == null ? "" : ": " + message);
    }

    private static final class CallTiming {
        volatile long callStartNano = -1L;
        volatile long callStartTimestamp = -1L;
        volatile long callEndNano = -1L;
    }

    private static final class TimingEventListener extends EventListener {
        private final CallTiming timing;

        private TimingEventListener(CallTiming timing) {
            this.timing = timing;
        }

        @Override
        public void callStart(Call call) {
            timing.callStartNano = System.nanoTime();
            timing.callStartTimestamp = Utils.getCurrentNanoTime();
        }

        @Override
        public void callEnd(Call call) {
            timing.callEndNano = System.nanoTime();
        }
    }

    /**
     * Read-only view of the SDK's resource map for displaying the exact Bean timestamps.
     */
    private static final class ResourceBeanObserver {
        private final Field resourceBeanMapField;

        private ResourceBeanObserver() throws NoSuchFieldException {
            resourceBeanMapField = FTRUMInnerManager.class.getDeclaredField("resourceBeanMap");
            resourceBeanMapField.setAccessible(true);
        }

        @SuppressWarnings("unchecked")
        private ResourceBean get(String resourceId) {
            try {
                Map<String, ResourceBean> resourceBeanMap =
                        (Map<String, ResourceBean>) resourceBeanMapField.get(FTRUMInnerManager.get());
                return resourceBeanMap.get(resourceId);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot inspect resourceBeanMap", e);
            }
        }
    }

    /**
     * Minimal real HTTP/1.1 server. The first response holds its body for the configured duration,
     * and both responses close the connection so the second call records fresh DNS/connect phases.
     */
    private static final class LoopbackHttpServer implements Closeable {
        private static final byte[] RESPONSE_BODY = "ok".getBytes(StandardCharsets.UTF_8);

        private final long firstResponseHoldMillis;
        private final AtomicBoolean closed = new AtomicBoolean();
        private ServerSocket serverSocket;
        private Thread serverThread;

        private LoopbackHttpServer(long firstResponseHoldMillis) {
            this.firstResponseHoldMillis = firstResponseHoldMillis;
        }

        private void start() throws IOException {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(
                    InetAddress.getByName("127.0.0.1"),
                    0
            ));
            serverThread = new Thread(this::serve, "ft-resource-queue-repro-server");
            serverThread.start();
        }

        private int getPort() {
            return serverSocket.getLocalPort();
        }

        private void serve() {
            for (int requestIndex = 0; requestIndex < 2 && !closed.get(); requestIndex++) {
                try (Socket socket = serverSocket.accept()) {
                    socket.setSoTimeout(15_000);
                    readRequest(socket.getInputStream());
                    writeResponse(
                            socket.getOutputStream(),
                            requestIndex == 0 ? firstResponseHoldMillis : 0L
                    );
                } catch (IOException e) {
                    if (!closed.get()) {
                        close();
                    }
                    return;
                }
            }
        }

        private static void readRequest(InputStream rawInput) throws IOException {
            BufferedInputStream input = new BufferedInputStream(rawInput);
            int contentLength = 0;
            String line = readAsciiLine(input);
            if (line == null) {
                throw new IOException("Request ended before request line");
            }
            while ((line = readAsciiLine(input)) != null && !line.isEmpty()) {
                int separator = line.indexOf(':');
                if (separator > 0
                        && "content-length".equalsIgnoreCase(line.substring(0, separator).trim())) {
                    contentLength = Integer.parseInt(line.substring(separator + 1).trim());
                }
            }
            for (int remaining = contentLength; remaining > 0; ) {
                int read = input.read(new byte[Math.min(remaining, 8_192)]);
                if (read < 0) {
                    throw new IOException("Request body ended early");
                }
                remaining -= read;
            }
        }

        private static String readAsciiLine(InputStream input) throws IOException {
            ByteArrayOutputStream line = new ByteArrayOutputStream();
            int value;
            while ((value = input.read()) != -1) {
                if (value == '\n') {
                    break;
                }
                if (value != '\r') {
                    line.write(value);
                }
            }
            if (value == -1 && line.size() == 0) {
                return null;
            }
            return line.toString(StandardCharsets.US_ASCII.name());
        }

        private static void writeResponse(OutputStream rawOutput, long holdMillis)
                throws IOException {
            BufferedOutputStream output = new BufferedOutputStream(rawOutput);
            String headers = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/plain; charset=utf-8\r\n"
                    + "Content-Length: " + RESPONSE_BODY.length + "\r\n"
                    + "Connection: close\r\n"
                    + "\r\n";
            output.write(headers.getBytes(StandardCharsets.US_ASCII));
            output.flush();
            if (holdMillis > 0) {
                try {
                    Thread.sleep(holdMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Local response interrupted", e);
                }
            }
            output.write(RESPONSE_BODY);
            output.flush();
        }

        @Override
        public void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            if (serverThread != null) {
                serverThread.interrupt();
            }
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ignored) {
                    // Best-effort cleanup for a sample-only local server.
                }
            }
        }
    }
}
