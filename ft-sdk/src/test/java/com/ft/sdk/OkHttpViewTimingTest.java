package com.ft.sdk;

import com.ft.sdk.garble.bean.*;
import com.ft.sdk.garble.db.FTDataStoreManager;
import com.ft.sdk.garble.db.file.FTFileDataStore;
import com.ft.sdk.garble.db.file.FTFileStorePaths;
import com.ft.sdk.garble.threadpool.*;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;
import java.io.IOException;
import okhttp3.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class OkHttpViewTimingTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();
    private FTRUMInnerManager rum;
    private FTFileDataStore store;
    private final Deque<Runnable> events = new ArrayDeque<>();
    private final Deque<Runnable> data = new ArrayDeque<>();
    private final Map<String, HashMap<String, Object>> views = new HashMap<>();
    private final Set<String> dropped = new HashSet<>();
    private final List<Runnable> restore = new ArrayList<>();
    private final List<HashMap<String, Object>> resources = new ArrayList<>();
    private final Map<String, Integer> pending = new HashMap<>();
    private ThreadPoolExecutor eventExecutor, dataExecutor;

    @Before public void setup() throws Exception {
        LogUtils.setDebug(false);
        swap(FTApplication.class, null, "instance", RuntimeEnvironment.getApplication());
        swap(FTActivityManager.class, null, "instance", null);
        swap(SyncTaskManager.class, SyncTaskManager.get(), "autoSync", false);
        store = new FTFileDataStore(new FTFileStorePaths(folder.newFolder())) {
            @Override public void increaseViewPendingResource(String id) {
                super.increaseViewPendingResource(id);
                pending.put(id, pending.getOrDefault(id, 0) + 1);
            }
            @Override public void reduceViewPendingResource(String id) {
                super.reduceViewPendingResource(id);
                pending.put(id, pending.getOrDefault(id, 0) - 1);
            }
        };
        swap(FTDataStoreManager.class, null, "dataStore", store);
        eventExecutor = queueExecutor(events);
        dataExecutor = queueExecutor(data);
        swap(BaseThreadPool.class, EventConsumerThreadPool.get(), "executor", eventExecutor);
        swap(BaseThreadPool.class, DataProcessThreadPool.get(), "executor", dataExecutor);
        swap(com.ft.sdk.garble.db.FTDBCachePolicy.class, null, "instance", null);
        com.ft.sdk.garble.db.FTDBCachePolicy.get().initRUMParam(new FTRUMConfig());
        SyncDataHelper helper = new SyncDataHelper() {
            @Override public String getBodyContent(String measurement, HashMap<String, Object> tags,
                    HashMap<String, Object> fields, long time, DataType type, String uuid, String visitor) {
                if ("view".equals(measurement)) views.put((String) tags.get("view_id"), new HashMap<>(fields));
                if ("resource".equals(measurement)) {
                    HashMap<String, Object> record = new HashMap<>(fields);
                    record.putAll(tags);
                    resources.add(record);
                }
                return dropped.contains(measurement) ? "" : super.getBodyContent(measurement, tags, fields,
                        time, type, uuid, visitor);
            }
        };
        helper.initBaseConfig(FTSDKConfig.builder().setEnableDataFilter(false));
        helper.initRUMConfig(new FTRUMConfig());
        swap(FTTrackInner.class, FTTrackInner.getInstance(), "dataHelper", helper);
        rum = FTRUMInnerManager.get();
        rum.release();
        drainQueues();
        FTRUMConfig config = new FTRUMConfig().setRumAppId("okhttp-timing")
                .setInitialResourceIdentifier(new FTTimeBasedInitialResourceIdentifier(5000));
        swap(FTRUMConfigManager.class, FTRUMConfigManager.get(), "config", config);
        rum.initParams(config);
        drainQueues();
    }

    @After public void cleanup() {
        rum.release();
        eventExecutor.shutdownNow();
        dataExecutor.shutdownNow();
        Collections.reverse(restore);
        for (Runnable task : restore) task.run();
        FTMonitorManager.release();
    }

    @Test public void callCreationDoesNotRegisterUntilTheInterceptorDispatches() throws Exception {
        Exchange queued = new Exchange("queued");
        assertTrue(resourceMap().isEmpty());
        rum.startView("dispatch-owner");
        ActiveViewBean view = active();
        long beforeDispatch = System.nanoTime();
        queued.proceed = () -> {
            ResourceBean bean = resourceMap().get(queued.id);
            assertNotNull(bean);
            assertEquals(view.getId(), bean.viewId);
            assertTrue(bean.startTimeNanoForDuration >= beforeDispatch);
            drainQueues();
            assertEquals(Integer.valueOf(1), pending.get(view.getId()));
            return queued.response(200);
        };
        queued.execute(new FTResourceInterceptor());
        queued.end();
        drain();
        assertEquals(Integer.valueOf(0), pending.get(view.getId()));
        assertNotNull(view.networkSettledMetric.resolve());
    }

    @Test public void fastResponseCannotSettleWhileAnotherInitialRequestIsWaiting() throws Exception {
        rum.startView("parallel");
        ActiveViewBean view = active();
        Exchange slow = new Exchange("slow");
        slow.proceed = () -> {
            Exchange fast = new Exchange("fast");
            fast.execute(new FTResourceInterceptor());
            fast.end();
            drain();
            assertEquals(Integer.valueOf(1), pending.get(view.getId()));
            assertNull(view.networkSettledMetric.resolve());
            assertFalse(views.get(view.getId()).containsKey("network_settled_time"));
            return slow.response(200);
        };
        slow.execute(new FTResourceInterceptor());
        slow.end();
        drain();
        assertEquals(2, resources.size());
        assertNotNull(view.networkSettledMetric.resolve());
        assertEquals(Integer.valueOf(0), pending.get(view.getId()));
    }

    @Test public void leavingWithSlowRequestFreezesNullAndPreservesOriginalViewAndSession() throws Exception {
        rum.startView("first");
        ActiveViewBean first = active();
        String session = rum.getSessionId();
        Exchange slow = new Exchange("cross-view");
        slow.proceed = () -> {
            assertEquals(first.getId(), resourceMap().get(slow.id).viewId);
            rum.startView("second");
            rum.forceRefreshSessionId();
            return slow.response(200);
        };
        slow.execute(new FTResourceInterceptor());
        slow.end();
        drain();
        assertNull(first.networkSettledMetric.resolve());
        assertNull(active().networkSettledMetric.resolve());
        assertEquals(first.getId(), resources.get(0).get("view_id"));
        assertEquals(session, resources.get(0).get("session_id"));
        assertEquals(Integer.valueOf(0), pending.get(first.getId()));
    }

    @Test public void responseBodyCompletionSuppliesTheEndInsteadOfResponseHeaders() throws Exception {
        rum.startView("body");
        ActiveViewBean view = active();
        Exchange request = new Exchange("body");
        request.execute(new FTResourceInterceptor());
        long headersEnd = resourceMap().get(request.id).endTimeNanoForDuration;
        drain();
        assertNull(view.networkSettledMetric.resolve());
        long bodyEnd = headersEnd + 2_000_000_000L;
        NetStatusBean timing = new NetStatusBean();
        timing.callStartTime = headersEnd - 10_000_000;
        timing.bodyEndTime = bodyEnd;
        rum.setOkHttpResourceNetState(request.id, timing, bodyEnd);
        drain();
        assertEquals(Long.valueOf(bodyEnd - view.getStartTimeNanoForDuration()),
                view.networkSettledMetric.resolve());
    }

    @Test public void alreadyAvailableTimingCannotWriteAnUnsetStopTimestamp() throws Exception {
        rum.startView("early-timing");
        ActiveViewBean view = active();
        Exchange request = new Exchange("empty");
        request.proceed = () -> {
            request.end();
            return request.response(204);
        };
        request.execute(new FTResourceInterceptor());
        drain();
        assertNotNull(view.networkSettledMetric.resolve());
        assertTrue(view.networkSettledMetric.resolve() > 0);
        assertEquals(1, resources.size());
        assertEquals(Integer.valueOf(0), pending.get(view.getId()));
    }

    @Test public void networkFailureAndCancellationBalancePendingAndPreserveTheIOException() throws Exception {
        for (String message : new String[]{"timeout", "Canceled"}) {
            rum.startView(message);
            ActiveViewBean view = active();
            Exchange request = new Exchange(message);
            IOException failure = new IOException(message);
            request.proceed = () -> { throw failure; };
            try {
                request.execute(new FTResourceInterceptor());
                fail("must propagate original failure");
            } catch (IOException actual) {
                assertSame(failure, actual);
            }
            request.listener.callFailed(request.call, failure);
            drain();
            assertEquals(Integer.valueOf(0), pending.get(view.getId()));
            assertNotNull(view.networkSettledMetric.resolve());
            assertFalse(resourceMap().containsKey(request.id));
        }
    }

    @Test public void runtimeFailureAbandonsPendingWithoutContributingAnEnd() throws Exception {
        for (int stage = 0; stage < 3; stage++) {
            final int failureStage = stage;
            rum.startView("callback-failure-" + stage);
            ActiveViewBean view = active();
            Exchange abandoned = new Exchange("abandoned-" + stage);
            IllegalStateException failure = new IllegalStateException("application callback");
            if (stage == 2) abandoned.proceed = () -> { throw failure; };
            FTResourceInterceptor interceptor = new FTResourceInterceptor(new FTResourceInterceptor.ContentHandlerHelper() {
                @Override public void onRequest(Request request, HashMap<String, Object> extra) {
                    if (failureStage == 0) throw failure;
                }
                @Override public void onResponse(Response response, HashMap<String, Object> extra) {
                    if (failureStage == 1) throw failure;
                }
            });
            try {
                abandoned.execute(interceptor);
                fail("must preserve callback failure");
            } catch (IllegalStateException actual) {
                assertSame(failure, actual);
            }
            drain();
            assertEquals(Integer.valueOf(0), pending.get(view.getId()));
            assertTrue(resourceMap().isEmpty());
            assertNull(view.networkSettledMetric.resolve());
            Exchange next = new Exchange("next-" + stage);
            next.execute(new FTResourceInterceptor()); next.end(); drain();
            assertNotNull("abandonment cannot leave network permanently pending", view.networkSettledMetric.resolve());
        }
    }

    @Test public void intakeExclusionNeverRegistersOrSettlesAnInitialRequest() throws Exception {
        FTRUMConfigManager.get().getConfig().setResourceUrlHandler(url -> true);
        rum.startView("excluded");
        Exchange request = new Exchange("intake");
        request.proceed = () -> {
            assertTrue(resourceMap().isEmpty());
            return request.response(200);
        };
        request.execute(new FTResourceInterceptor()); request.end(); drain();
        assertTrue(pending.isEmpty());
        assertTrue(resources.isEmpty());
        assertNull(active().networkSettledMetric.resolve());
    }

    @Test public void discardedWritesReleasePendingWithoutInventingNetworkDuration() throws Exception {
        rum.startView("filtered");
        ActiveViewBean view = active();
        dropped.add("resource"); dropped.add("error");
        Exchange filtered = new Exchange("filtered");
        filtered.proceed = () -> filtered.response(503);
        filtered.execute(new FTResourceInterceptor()); filtered.end(); drain();
        assertNull(view.networkSettledMetric.resolve());
        assertEquals(Integer.valueOf(0), pending.get(view.getId()));
        dropped.clear();
        Exchange next = new Exchange("next");
        next.execute(new FTResourceInterceptor()); next.end(); drain();
        assertNotNull(view.networkSettledMetric.resolve());
    }

    private Map<String, ResourceBean> resourceMap() {
        try {
            return (Map<String, ResourceBean>) field(FTRUMInnerManager.class, "resourceBeanMap").get(rum);
        } catch (Exception e) { throw new AssertionError(e); }
    }

    private interface Proceed { Response run() throws IOException; }
    private class Exchange {
        final Request request;
        final String id;
        final Call call;
        final FTResourceEventListener listener;
        Proceed proceed;
        Exchange(String path) {
            request = new Request.Builder().url("https://example.com/" + path)
                    .tag(ResourceID.class, new ResourceID()).build();
            id = Utils.identifyRequest(request);
            call = new OkHttpClient().newCall(request);
            listener = new FTResourceEventListener(id, false, new FTResourceEventListener.NoOPEventListener());
            listener.callStart(call);
            proceed = () -> response(200);
        }
        Response response(int status) {
            return new Response.Builder().request(request).protocol(Protocol.HTTP_1_1)
                    .code(status).message("test").body(ResponseBody.create(MediaType.parse("text/plain"), "ok")).build();
        }
        void execute(FTResourceInterceptor interceptor) throws IOException {
            Interceptor.Chain chain = (Interceptor.Chain) Proxy.newProxyInstance(
                    Interceptor.Chain.class.getClassLoader(), new Class<?>[]{Interceptor.Chain.class},
                    (proxy, method, args) -> {
                        if ("request".equals(method.getName())) return request;
                        if ("proceed".equals(method.getName())) return proceed.run();
                        if ("call".equals(method.getName())) return call;
                        throw new AssertionError(method.getName());
                    });
            try (Response ignored = interceptor.intercept(chain)) { }
        }
        void end() { listener.callEnd(call); }
    }

    private void drain() {
        drainQueues();
        // Rapid test navigation stays inside the SDK's 200 ms emission debounce.
        // Request a View update explicitly, without sleeps or changing lifecycle times.
        try {
            Method generate = FTRUMInnerManager.class.getDeclaredMethod("generateViewSum");
            generate.setAccessible(true);
            generate.invoke(rum);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        drainQueues();
    }

    private void drainQueues() {
        int count = 0;
        while (!events.isEmpty() || !data.isEmpty()) {
            while (!events.isEmpty()) {
                assertTrue("event loop", ++count < 1000);
                events.removeFirst().run();
            }
            while (!data.isEmpty()) {
                assertTrue("data loop", ++count < 1000);
                data.removeFirst().run();
            }
        }
    }

    private ActiveViewBean active() throws Exception { return (ActiveViewBean) field(FTRUMInnerManager.class, "activeView").get(rum); }
    private static Field field(Class<?> cls, String name) throws Exception {
        Field f = cls.getDeclaredField(name); f.setAccessible(true); return f;
    }
    private void swap(Class<?> cls, Object object, String name, Object value) throws Exception {
        Field f = field(cls, name); Object old = f.get(object); f.set(object, value);
        restore.add(() -> { try { f.set(object, old); } catch (Exception e) { throw new AssertionError(e); } });
    }
    private ThreadPoolExecutor queueExecutor(Deque<Runnable> queue) {
        return new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>()) {
            @Override public void execute(Runnable task) { queue.addLast(task); }
        };
    }
}
