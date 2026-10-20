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

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class RUMViewTimingIntegrationTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();
    private FTRUMInnerManager rum;
    private FTFileDataStore store;
    private final Deque<Runnable> events = new ArrayDeque<>();
    private final Deque<Runnable> data = new ArrayDeque<>();
    private final Map<String, HashMap<String, Object>> views = new HashMap<>();
    private final Set<String> dropped = new HashSet<>();
    private final List<Runnable> restore = new ArrayList<>();
    private boolean failWrite;
    private boolean throwWrite;
    private ThreadPoolExecutor eventExecutor, dataExecutor;

    @Before public void setup() throws Exception {
        LogUtils.setDebug(false);
        swap(FTApplication.class, null, "instance", RuntimeEnvironment.getApplication());
        swap(FTActivityManager.class, null, "instance", null);
        swap(SyncTaskManager.class, SyncTaskManager.get(), "autoSync", false);
        store = new FTFileDataStore(new FTFileStorePaths(folder.newFolder())) {
            @Override public boolean insertFtOperation(SyncData record, boolean reInsert) {
                if (throwWrite) throw new IllegalStateException("write failure");
                return !failWrite && super.insertFtOperation(record, reInsert);
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
                return dropped.contains(measurement) ? "" : super.getBodyContent(measurement, tags, fields,
                        time, type, uuid, visitor);
            }
        };
        helper.initBaseConfig(FTSDKConfig.builder().setEnableDataFilter(false));
        helper.initRUMConfig(new FTRUMConfig());
        swap(FTTrackInner.class, FTTrackInner.getInstance(), "dataHelper", helper);
        Constructor<FTRUMInnerManager> constructor = FTRUMInnerManager.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        rum = constructor.newInstance();
        rum.initParams(new FTRUMConfig());
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

    @Test public void networkWaitsForAcceptedWritesAndSurvivesAttributeRoundTrip() throws Exception {
        rum.startView("first");
        ActiveViewBean view = active();
        long start = view.getStartTimeNanoForDuration();
        resource("a", start + 20_000_000);
        resource("b", start + 80_000_000);
        resource("late", start + 100_000_000);
        finish("b", start + 400_000_000, 200);
        drain();
        assertNull(view.networkSettledMetric.resolve());
        finish("a", start + 150_000_000, 200);
        assertNull(view.networkSettledMetric.resolve());
        drain();
        assertEquals(400_000_000L, views.get(view.getId()).get("network_settled_time"));
        finish("late", start + 800_000_000, 200);
        drain();
        assertEquals(Long.valueOf(400_000_000), store.querySumView(0, true).get(0).networkSettledTime);
        rum.stopView();
        drain();
        assertEquals(400_000_000L, views.get(view.getId()).get("network_settled_time"));
    }

    @Test public void errorCanCompleteResourceWhenResourceEventIsFiltered() throws Exception {
        rum.startView("first");
        ActiveViewBean view = active();
        resource("error", view.getStartTimeNanoForDuration());
        dropped.add("resource");
        finish("error", view.getStartTimeNanoForDuration() + 200, 500);
        drain();
        assertEquals(Long.valueOf(200), view.networkSettledMetric.resolve());
    }

    @Test public void acceptedResourceSettlesWithoutWaitingForItsCompanionErrorWrite() throws Exception {
        rum.startView("first");
        ActiveViewBean view = active();
        resource("error", view.getStartTimeNanoForDuration());
        finish("error", view.getStartTimeNanoForDuration() + 200, 500);
        data.removeFirst().run();
        assertEquals(Long.valueOf(200), view.networkSettledMetric.resolve());
        rum.stopView();
        drain();
        assertEquals(Long.valueOf(200), view.networkSettledMetric.resolve());
    }

    @Test public void droppedFailedAndAbandonedResourcesDoNotInventDurations() throws Exception {
        for (int scenario = 0; scenario < 4; scenario++) {
            rum.startView("case-" + scenario);
            ActiveViewBean view = active();
            resource("r", view.getStartTimeNanoForDuration());
            if (scenario == 0) dropped.add("resource");
            if (scenario == 1) failWrite = true;
            if (scenario == 2) throwWrite = true;
            if (scenario == 3) rum.discardResource("r");
            else finish("r", view.getStartTimeNanoForDuration() + 100, 200);
            drain();
            assertNull(view.networkSettledMetric.resolve());
            // Removal, not an indefinitely pending request: a new successful initial request can settle.
            dropped.clear(); failWrite = false; throwWrite = false;
            resource("valid", view.getStartTimeNanoForDuration());
            finish("valid", view.getStartTimeNanoForDuration() + 300, 200);
            drain();
            assertEquals(Long.valueOf(300), view.networkSettledMetric.resolve());
        }
    }

    @Test public void closingViewBeforeWriteFreezesNullAndDoesNotAffectNextView() throws Exception {
        rum.startView("one");
        ActiveViewBean first = active();
        resource("r", first.getStartTimeNanoForDuration());
        finish("r", first.getStartTimeNanoForDuration() + 100, 200);
        rum.startView("two");
        ActiveViewBean second = active();
        drain();
        assertNull(first.networkSettledMetric.resolve());
        assertFalse(views.get(first.getId()).containsKey("network_settled_time"));
        assertFalse(views.get(second.getId()).containsKey("network_settled_time"));
    }

    @Test public void automaticActionUsesStartAndExplicitActionUsesStop() throws Exception {
        for (boolean explicit : new boolean[]{false, true}) {
            rum.startView("source", null, ViewTrackingType.AUTOMATIC);
            rum.startAction("navigate", "click", explicit);
            ActiveActionBean action = action();
            action.setStartTimeNanoForDuration(System.nanoTime() - 50_000_000);
            // Persist the test clock change before the close operation.
            if (explicit) rum.stopAction();
            long interaction = action.getInteractionTimeNano();
            rum.startView("target");
            ActiveViewBean target = active();
            drain();
            assertEquals(target.getStartTimeNanoForDuration() - interaction,
                    views.get(target.getId()).get("interaction_to_next_view_time"));
            assertEquals(Long.valueOf(target.getStartTimeNanoForDuration() - interaction),
                    store.querySumView(0, true).get(0).interactionToNextViewTime);
        }
    }

    @Test public void droppedActionIsNotAnInteractionAndCustomActionDoesNotReplaceClick() throws Exception {
        rum.startView("one");
        dropped.add("action");
        rum.addAction("filtered", "click", 0, Utils.getCurrentNanoTime() - 10_000_000);
        drain();
        rum.startView("two");
        ActiveViewBean second = active();
        drain();
        assertFalse(views.get(second.getId()).containsKey("interaction_to_next_view_time"));
        dropped.clear();
        rum.addAction("accepted", "click", 0, Utils.getCurrentNanoTime() - 10_000_000);
        drain();
        rum.addAction("custom", "custom", 0, Utils.getCurrentNanoTime());
        drain();
        rum.startView("three");
        drain();
        assertNotNull(views.get(active().getId()).get("interaction_to_next_view_time"));
    }

    @Test public void lateActionWriteUpdatesAnAlreadyClosedNextView() throws Exception {
        rum.startView("source", null, ViewTrackingType.AUTOMATIC);
        rum.startAction("navigate", "click");
        ActiveActionBean action = action();
        action.setStartTimeNanoForDuration(System.nanoTime() - 50_000_000);
        rum.startView("target");
        ActiveViewBean target = active();
        rum.startView("third");
        drain();
        assertEquals(target.getStartTimeNanoForDuration() - action.getInteractionTimeNano(),
                views.get(target.getId()).get("interaction_to_next_view_time"));
        assertEquals(false, views.get(target.getId()).get("is_active"));
        assertEquals(target.getTimeSpent(), views.get(target.getId()).get("time_spent"));
    }

    @Test public void explicitStopWithPendingResourceIsPersistedWhenTheResourceCompletes() throws Exception {
        rum.startView("source");
        rum.startAction("navigate", "click", true);
        resource("pending", active().getStartTimeNanoForDuration());
        rum.stopAction();
        long stop = action().getInteractionTimeNano();
        drain();
        assertTrue(store.querySumAction(0).isEmpty());
        finish("pending", stop + 100, 200);
        drain();
        rum.startView("target");
        ActiveViewBean target = active();
        drain();
        assertEquals(target.getStartTimeNanoForDuration() - stop,
                views.get(target.getId()).get("interaction_to_next_view_time"));
    }

    @Test public void writeOutcomesDistinguishAcceptedFilteredUnsampledFailedAndCacheLimitedEvents() throws Exception {
        for (int scenario = 0; scenario < 6; scenario++) {
            List<Boolean> outcomes = new ArrayList<>();
            dropped.clear(); failWrite = false; throwWrite = false;
            if (scenario == 1) dropped.add("action");
            if (scenario == 2) failWrite = true;
            if (scenario == 3) throwWrite = true;
            if (scenario == 5) {
                com.ft.sdk.garble.db.FTDBCachePolicy policy = com.ft.sdk.garble.db.FTDBCachePolicy.get();
                swap(policy.getClass(), policy, "rumLimitCount", 0);
                swap(policy.getClass(), policy, "rumCacheDiscardStrategy", RUMCacheDiscard.DISCARD);
            }
            FTTrackInner.getInstance().rum(Utils.getCurrentNanoTime(), "action", new HashMap<>(),
                    new HashMap<>(), (RUMWriteResultCallback) outcomes::add,
                    scenario == 4 ? CollectType.NOT_COLLECT : CollectType.COLLECT_BY_SAMPLE);
            drain();
            assertEquals(Collections.singletonList(scenario == 0), outcomes);
        }
    }

    @Test public void networkStrategyIsSnapshottedAndDoesNotDiscardExcludedResourceEvents() throws Exception {
        List<FTNetworkSettledResourceContext> contexts = new ArrayList<>();
        FTRUMConfig config = new FTRUMConfig().setInitialResourceIdentifier(context -> {
            contexts.add(context);
            return "selected".equals(context.getResourceId());
        });
        rum.initParams(config);
        config.setInitialResourceIdentifier(context -> false);
        for (String name : new String[]{"first", "second"}) {
            rum.startView(name);
            ActiveViewBean view = active();
            long start = view.getStartTimeNanoForDuration();
            resource("excluded", start);
            resource("selected", start + 500_000_000);
            finish("excluded", start + 900_000_000, 200);
            finish("selected", start + 800_000_000, 200);
            drain();
            assertEquals(800_000_000L, views.get(view.getId()).get("network_settled_time"));
            assertEquals(2, ((Number) views.get(view.getId()).get(Constants.KEY_RUM_VIEW_RESOURCE_COUNT)).intValue());
            FTNetworkSettledResourceContext context = contexts.get(contexts.size() - 1);
            assertEquals(start, context.getViewStartTimeNanos());
            assertEquals(start + 500_000_000, context.getResourceStartTimeNanos());
        }
        assertEquals(4, contexts.size());
    }

    @Test public void invStrategyRunsOnQueuedInputsAndIsNotRepeatedForViewUpdates() throws Exception {
        List<FTPreviousViewLastInteractionContext> contexts = new ArrayList<>();
        FTRUMConfig config = new FTRUMConfig().setLastInteractionIdentifier(context -> {
            contexts.add(context);
            return true;
        });
        rum.initParams(config);
        config.setLastInteractionIdentifier(null);
        rum.startView("source");
        rum.addAction("navigate", "click", 0, Utils.getCurrentNanoTime());
        drain();
        assertTrue(contexts.isEmpty());
        rum.startView("target");
        ActiveViewBean target = active();
        assertTrue(contexts.isEmpty());
        drain();
        assertEquals(1, contexts.size());
        FTPreviousViewLastInteractionContext context = contexts.get(0);
        assertEquals("click", context.getActionType());
        assertEquals(target.getStartTimeNanoForDuration(), context.getNextViewStartTimeNanos());
        assertEquals(target.getStartTimeNanoForDuration() - context.getInteractionTimeNanos(),
                views.get(target.getId()).get("interaction_to_next_view_time"));
        drain(); drain();
        rum.stopView();
        drain();
        assertEquals(1, contexts.size());
    }

    @Test public void invHistoryEvictionAndUnrelatedActionPreserveClosedTargetValue() throws Exception {
        rum.startView("source");
        rum.addAction("navigate", "click", 0, Utils.getCurrentNanoTime());
        drain();
        rum.startView("target");
        ActiveViewBean target = active();
        drain();
        Object original = views.get(target.getId()).get("interaction_to_next_view_time");
        assertNotNull(original);
        for (String name : new String[]{"third", "fourth", "fifth"}) {
            rum.startView(name);
            drain();
        }
        rum.addAction("unrelated", "click", 0, Utils.getCurrentNanoTime());
        drain();
        assertEquals(original, views.get(target.getId()).get("interaction_to_next_view_time"));
        assertEquals(false, views.get(target.getId()).get("is_active"));
    }

    @Test public void rejectedLateCandidateRemovesInvFromAlreadyClosedTarget() throws Exception {
        rum.initParams(new FTRUMConfig().setLastInteractionIdentifier(
                context -> "click".equals(context.getActionType())));
        rum.startView("source");
        rum.addAction("first", "click", 0, Utils.getCurrentNanoTime());
        drain();
        rum.addAction("later", "swipe", 0, Utils.getCurrentNanoTime());
        rum.startView("target");
        ActiveViewBean target = active();
        rum.startView("third");
        drain();
        assertFalse(views.get(target.getId()).containsKey("interaction_to_next_view_time"));
        assertEquals(false, views.get(target.getId()).get("is_active"));
    }

    @Test public void disabledInvProducesNoMetricEvenWithAcceptedClick() throws Exception {
        rum.initParams(new FTRUMConfig().setLastInteractionIdentifier(null));
        rum.startView("source");
        rum.addAction("navigate", "click", 0, Utils.getCurrentNanoTime());
        drain();
        rum.startView("target");
        drain();
        assertFalse(views.get(active().getId()).containsKey("interaction_to_next_view_time"));
    }

    @Test public void callbackFailureAffectsMetricOnlyAndDoesNotLeavePendingResource() throws Exception {
        rum.initParams(new FTRUMConfig().setInitialResourceIdentifier(context -> {
            if ("failed".equals(context.getResourceId())) throw new IllegalStateException("callback");
            return true;
        }).setLastInteractionIdentifier(context -> { throw new IllegalStateException("callback"); }));
        rum.startView("source");
        ActiveViewBean source = active();
        long start = source.getStartTimeNanoForDuration();
        resource("failed", start);
        resource("good", start);
        finish("failed", start + 500, 200);
        finish("good", start + 300, 200);
        rum.addAction("navigate", "click", 0, Utils.getCurrentNanoTime());
        drain();
        assertEquals(300L, views.get(source.getId()).get("network_settled_time"));
        assertEquals(2, ((Number) views.get(source.getId()).get(Constants.KEY_RUM_VIEW_RESOURCE_COUNT)).intValue());
        assertEquals(1, ((Number) views.get(source.getId()).get(Constants.KEY_RUM_VIEW_ACTION_COUNT)).intValue());
        rum.startView("target");
        drain();
        assertFalse(views.get(active().getId()).containsKey("interaction_to_next_view_time"));
    }

    @Test public void reinitializingDuringInvCallbackCannotRestoreOldResult() throws Exception {
        int[] calls = {0};
        rum.initParams(new FTRUMConfig().setLastInteractionIdentifier(context -> {
            calls[0]++;
            rum.initParams(new FTRUMConfig());
            return true;
        }));
        rum.startView("source");
        rum.addAction("navigate", "click", 0, Utils.getCurrentNanoTime());
        drain();
        rum.startView("target");
        ActiveViewBean target = active();
        drain();
        assertEquals(1, calls[0]);
        assertNull(target.interactionToNextViewTime);
        assertFalse(views.get(target.getId()).containsKey("interaction_to_next_view_time"));
    }

    @Test public void restoredViewAttributesDoNotInvokeNewStrategiesOrRecomputeMonotonicTime() throws Exception {
        rum.startView("source");
        rum.addAction("navigate", "click", 0, Utils.getCurrentNanoTime());
        drain();
        rum.startView("target");
        ActiveViewBean target = active();
        drain();
        Object original = views.get(target.getId()).get("interaction_to_next_view_time");
        assertNotNull(original);
        int[] calls = {0};
        rum.initParams(new FTRUMConfig().setInitialResourceIdentifier(context -> {
            calls[0]++;
            return false;
        }).setLastInteractionIdentifier(context -> {
            calls[0]++;
            return false;
        }));
        drain();
        assertEquals(0, calls[0]);
        assertEquals(original, views.get(target.getId()).get("interaction_to_next_view_time"));
    }

    private void resource(String id, long start) {
        rum.onCreateResource(id).startTimeNanoForDuration = start;
        rum.startResource(id);
    }

    private void finish(String id, long end, int status) {
        rum.stopResource(id);
        ResourceBean resource = rum.onCreateResource(id);
        resource.endTimeNanoForDuration = end;
        resource.resourceStatus = status;
        resource.url = "https://example.com/" + id;
        rum.putRUMResourcePerformance(id);
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
    private ActiveActionBean action() throws Exception { return (ActiveActionBean) field(FTRUMInnerManager.class, "activeAction").get(rum); }
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
