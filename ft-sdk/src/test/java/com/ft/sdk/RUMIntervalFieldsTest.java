package com.ft.sdk;

import com.ft.sdk.garble.bean.*;
import com.ft.sdk.garble.threadpool.*;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;
import org.junit.*;
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
public class RUMIntervalFieldsTest {
    private FTRUMInnerManager rum;
    private AppStateHistoryTest.TestClock clock;
    private AppStateHistory history;
    private Object oldActivityManager;
    private Object oldApplication;
    private Object oldHelper;
    private Object oldEventExecutor;
    private Object oldDataExecutor;
    private ThreadPoolExecutor eventExecutor;
    private ThreadPoolExecutor dataExecutor;
    private final Map<String, HashMap<String, Object>> fields = new HashMap<>();
    private final Map<String, HashMap<String, Object>> tags = new HashMap<>();
    private final Map<String, String> lines = new HashMap<>();

    @Before public void setup() throws Exception {
        LogUtils.setDebug(false);
        oldApplication = replace(FTApplication.class, null, "instance", RuntimeEnvironment.getApplication());
        oldActivityManager = replace(FTActivityManager.class, null, "instance", null);
        clock = new AppStateHistoryTest.TestClock();
        clock.now = clock.mono = 1;
        history = new AppStateHistory(clock);
        history.record("foreground");
        replace(FTActivityManager.class, FTActivityManager.get(), "appStateHistory", history);
        // Hold cache work without starting threads; serialization runs synchronously below.
        eventExecutor = executor(false);
        dataExecutor = executor(true);
        oldEventExecutor = replace(BaseThreadPool.class, EventConsumerThreadPool.get(), "executor", eventExecutor);
        oldDataExecutor = replace(BaseThreadPool.class, DataProcessThreadPool.get(), "executor", dataExecutor);
        SyncDataHelper helper = new SyncDataHelper() {
            @Override public String getBodyContent(String measurement, HashMap<String, Object> eventTags,
                    HashMap<String, Object> eventFields, long time, DataType type, String uuid, String visitor) {
                fields.put(measurement, new HashMap<>(eventFields));
                tags.put(measurement, new HashMap<>(eventTags));
                lines.put(measurement, super.getBodyContent(measurement, eventTags, eventFields,
                        time, type, uuid, visitor));
                return "";
            }
        };
        helper.initBaseConfig(FTSDKConfig.builder().setEnableDataFilter(false));
        helper.initRUMConfig(new FTRUMConfig());
        oldHelper = replace(FTTrackInner.class, FTTrackInner.getInstance(), "dataHelper", helper);
        Constructor<FTRUMInnerManager> constructor = FTRUMInnerManager.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        rum = constructor.newInstance();
        rum.initParams(new FTRUMConfig());
    }

    @After public void cleanup() throws Exception {
        rum.release();
        eventExecutor.shutdownNow();
        dataExecutor.shutdownNow();
        replace(BaseThreadPool.class, EventConsumerThreadPool.get(), "executor", oldEventExecutor);
        replace(BaseThreadPool.class, DataProcessThreadPool.get(), "executor", oldDataExecutor);
        replace(FTTrackInner.class, FTTrackInner.getInstance(), "dataHelper", oldHelper);
        replace(FTActivityManager.class, null, "instance", oldActivityManager);
        replace(FTApplication.class, null, "instance", oldApplication);
        FTMonitorManager.release();
    }

    @Test public void automaticViewEndsExactlyAtBackgroundAndOldOwnerCannotCloseNewView() throws Exception {
        Object first = new Object();
        rum.startAutomaticView(first, "first", null);
        ActiveViewBean view = active();
        long boundary = view.getStartTime() + 100;
        advanceTo(boundary);
        history.record("background");
        rum.onAppBackground(boundary);
        assertTrue(view.isClose());
        assertEquals(100, view.getTimeSpent());
        assertEquals("foreground", view.closedAppState);
        rum.stopAutomaticView(first);
        assertEquals(100, view.getTimeSpent());
        rum.forceRefreshSessionId();
        assertSame(view, active());
        advanceTo(Utils.getCurrentNanoTime() + 1_000_000);
        history.record("foreground");
        rum.startAutomaticView(new Object(), "second", null);
        ActiveViewBean next = active();
        assertNotEquals(view.getId(), next.getId());
        rum.stopAutomaticView(first);
        assertFalse(next.isClose());
    }

    @Test public void manualViewSurvivesBackgroundAndLoadingOriginIsConsumedOnce() throws Exception {
        long origin = 100;
        rum.onCreateView("manual", 50, origin);
        rum.startView("manual");
        ActiveViewBean view = active();
        assertEquals(origin, view.appStateStart);
        view.setStartTime(100);
        advanceTo(200);
        history.record("background");
        rum.onAppBackground(clock.now);
        assertFalse(view.isClose());
        advanceTo(Utils.getCurrentNanoTime() + 1_000_000);
        rum.stopView();
        assertEquals("foreground_and_background", view.closedAppState);
        rum.startView("manual");
        assertEquals(-1, active().getLoadTime());
        assertEquals(active().getStartTime(), active().appStateStart);
    }

    @Test public void durationOnlyLoadDoesNotInventOriginAndBackgroundExpiresPendingLoad() throws Exception {
        rum.onCreateView("legacy", 1_000_000);
        rum.startView("legacy");
        assertEquals(active().getStartTime(), active().appStateStart);
        rum.onCreateView("later", 999, 10);
        advanceTo(Utils.getCurrentNanoTime());
        rum.onAppBackground(clock.now);
        rum.startView("later");
        assertEquals(-1, active().getLoadTime());
    }

    @Test public void invalidAndExpiredLoadOriginsFallBackToViewStart() throws Exception {
        rum.onCreateView("future", 99, Long.MAX_VALUE);
        rum.startView("future");
        assertEquals(active().getStartTime(), active().appStateStart);
        rum.onCreateView("stale", 99, 100);
        Field cacheField = FTRUMInnerManager.class.getDeclaredField("preActivityDuration");
        cacheField.setAccessible(true);
        Object load = ((Map<?, ?>) cacheField.get(rum)).get("stale");
        replace(load.getClass(), load, "recordedAt",
                System.nanoTime() - FTRUMInnerManager.MAX_RESTING_TIME);
        rum.startView("stale");
        assertEquals(-1, active().getLoadTime());
        assertEquals(active().getStartTime(), active().appStateStart);
    }

    @Test public void loadOriginRequiresAPositiveDurationAndTimestamp() throws Exception {
        for (long duration : new long[]{0, -1}) {
            rum.onCreateView("invalid-duration", duration, 100);
            rum.startView("invalid-duration");
            assertEquals(active().getStartTime(), active().appStateStart);
        }
        for (long origin : new long[]{0, -1, Long.MAX_VALUE}) {
            rum.onCreateView("invalid-origin", 100, origin);
            rum.startView("invalid-origin");
            assertEquals(active().getStartTime(), active().appStateStart);
        }
    }

    @Test public void frameworkAutomaticViewClosesActionAndIgnoresOldIdsWhileResourceFinishes() throws Exception {
        FTRUMGlobalManager global = FTRUMGlobalManager.get();
        Object previous = replace(FTRUMGlobalManager.class, global, "innerManager", rum);
        try {
            global.onCreateView("framework", 50, 100);
            String firstId = global.startView("framework", null, ViewTrackingType.AUTOMATIC);
            ActiveViewBean first = active();
            assertEquals(firstId, first.getId());
            assertEquals(100, first.appStateStart);
            rum.startAction("wait", "custom", true);
            ActiveActionBean firstAction = action();
            firstAction.setStartTime(first.getStartTime() + 20);
            global.startResource("pending");
            ResourceBean resource = rum.onCreateResource("pending");
            long boundary = first.getStartTime() + 100;
            advanceTo(boundary);
            history.record("background");
            rum.onAppBackground(boundary);
            assertTrue(first.isClose());
            assertEquals(100, first.getTimeSpent());
            assertTrue(firstAction.isClose());
            assertEquals(80, firstAction.getDuration());
            rum.stopAction();
            global.stopView(firstId, null);
            assertEquals(80, firstAction.getDuration());

            advanceTo(Utils.getCurrentNanoTime() + 1_000_000);
            history.record("foreground");
            String secondId = global.startView("framework", null, ViewTrackingType.AUTOMATIC);
            assertNotEquals(firstId, secondId);
            assertEquals(-1, active().getLoadTime());
            rum.startAction("new", "custom", true);
            ActiveActionBean secondAction = action();
            HashMap<String, Object> staleProperty = new HashMap<>();
            staleProperty.put("stale", true);
            global.stopView(firstId, staleProperty);
            global.stopView(null, staleProperty);
            assertFalse(active().isClose());
            assertFalse(secondAction.isClose());
            assertFalse(active().getProperty().containsKey("stale"));

            NetStatusBean network = new NetStatusBean();
            network.callStartEpoch = first.getStartTime();
            network.callStartTime = 1000;
            network.bodyEndTime = 1050;
            rum.setNetState("pending", network);
            resource.url = "https://example.com/pending";
            resource.resourceStatus = 200;
            global.stopResource("pending");
            rum.putRUMResourcePerformance("pending");
            assertEquals(firstId, tags.get("resource").get("view_id"));
            assertEquals("foreground", fields.get("resource").get("app_state"));
            assertEquals(100, first.getTimeSpent());
            assertEquals("foreground", first.closedAppState);
            assertFalse(secondAction.isClose());

            global.stopView(secondId, null);
            assertTrue(active().isClose());
            assertTrue(secondAction.isClose());
        } finally {
            replace(FTRUMGlobalManager.class, global, "innerManager", previous);
        }
    }

    @Test public void frameworkManualAndLegacyViewsKeepCallerControlledLifecycles() throws Exception {
        FTRUMGlobalManager global = FTRUMGlobalManager.get();
        Object previous = replace(FTRUMGlobalManager.class, global, "innerManager", rum);
        try {
            global.startView("automatic", null, ViewTrackingType.AUTOMATIC);
            for (int mode = 0; mode < 4; mode++) {
                if (mode == 0) global.startView("manual");
                if (mode == 1) global.startView("manual", null);
                if (mode == 2) global.startView("manual", null, ViewTrackingType.MANUAL);
                if (mode == 3) global.startView("manual", null, null);
                rum.startAction("manual-action", "custom", true);
                advanceTo(Utils.getCurrentNanoTime() + 1_000_000);
                history.record("background");
                rum.onAppBackground(clock.now);
                assertFalse(active().isClose());
                assertFalse(action().isClose());
            }
        } finally {
            replace(FTRUMGlobalManager.class, global, "innerManager", previous);
        }
    }

    @Test public void typedFrameworkApisRemainSafeWhenRumIsDisabled() throws Exception {
        FTRUMGlobalManager global = FTRUMGlobalManager.get();
        Object previous = replace(FTRUMGlobalManager.class, global, "innerManager", null);
        try {
            assertNull(global.startView("disabled", null, ViewTrackingType.AUTOMATIC));
            global.onCreateView("disabled", 100, 100);
            global.stopView("old-id", null);
        } finally {
            replace(FTRUMGlobalManager.class, global, "innerManager", previous);
        }
    }

    @Test public void sessionReasonsCoverTimeoutSamplingAndUnknownWithoutRelabellingOldEvents() throws Exception {
        rum.startView("first");
        ActiveViewBean first = active();
        assertEquals("sdk_init", first.getSessionStartReason());
        replace(FTRUMInnerManager.class, rum, "lastUserActiveTime",
                System.nanoTime() - FTRUMInnerManager.MAX_RESTING_TIME);
        rum.startView("idle");
        assertEquals("inactivity_timeout", active().getSessionStartReason());
        replace(FTRUMInnerManager.class, rum, "lastSessionTime",
                System.nanoTime() - FTRUMInnerManager.SESSION_EXPIRE_TIME);
        rum.startView("max");
        assertEquals("max_duration", active().getSessionStartReason());
        assertTrue(rum.hotUpdate(0f, 0f));
        assertEquals("sampling_changed", active().getSessionStartReason());
        String id = rum.getSessionId();
        assertFalse(rum.hotUpdate(0.5f, null));
        assertEquals(id, rum.getSessionId());
        rum.forceRefreshSessionId();
        assertEquals("unknown", active().getSessionStartReason());
        assertEquals("sdk_init", first.getSessionStartReason());
        assertEquals(-1, active().getLoadTime());
    }

    @Test public void resourceUsesOriginalNetworkIntervalAndKeepsOldSessionAfterRenewal() throws Exception {
        rum.startView("screen");
        rum.startResource("request");
        ResourceBean resource = rum.onCreateResource("request");
        String sessionId = resource.sessionId;
        resource.startTime = 200;
        resource.startTimeNanoForDuration = 1000;
        NetStatusBean network = new NetStatusBean();
        network.callStartTime = 900;
        network.callStartEpoch = 100;
        network.bodyEndTime = 1100;
        advanceTo(250);
        history.record("background");
        advanceTo(Utils.getCurrentNanoTime() + 1_000_000);
        rum.setNetState("request", network);
        resource.url = "https://example.com/path";
        resource.resourceStatus = 500;
        rum.forceRefreshSessionId();
        rum.putRUMResourcePerformance("request");
        assertEquals("foreground_and_background", fields.get("resource").get("app_state"));
        assertEquals("sdk_init", fields.get("resource").get("session_start_reason"));
        assertEquals(sessionId, tags.get("resource").get("session_id"));
        assertEquals(sessionId, tags.get("error").get("session_id"));
        assertEquals("sdk_init", fields.get("error").get("session_start_reason"));
        assertFalse(fields.get("error").containsKey("app_state"));
        assertTrue(tags.get("error").containsKey("error_situation"));
        assertTrue(lines.get("resource").contains("app_state=\"foreground_and_background\""));
        assertTrue(lines.get("resource").contains("session_start_reason=\"sdk_init\""));
    }

    @Test public void delayedResourceCompletionDoesNotExtendTheNetworkInterval() {
        rum.startView("screen");
        rum.startResource("request");
        ResourceBean resource = rum.onCreateResource("request");
        NetStatusBean network = new NetStatusBean();
        network.callStartTime = 1000;
        network.callStartEpoch = 100;
        network.bodyEndTime = 1100;
        advanceTo(200);
        history.record("background");
        advanceTo(Utils.getCurrentNanoTime() + 1_000_000);
        rum.setNetState("request", network);
        resource.url = "https://example.com/path";
        resource.resourceStatus = 200;
        rum.putRUMResourcePerformance("request");
        assertEquals("foreground", fields.get("resource").get("app_state"));
        assertEquals(100L, fields.get("resource").get("duration"));
    }

    @Test public void longTasksUseOriginalIntervalAndHistoricalTasksAreUnknown() {
        advanceTo(200);
        history.record("background");
        advanceTo(Utils.getCurrentNanoTime() + 1_000_000);
        rum.addLongTask("stack", 100, 100, false, null);
        assertEquals("foreground", fields.get("long_task").get("app_state"));
        rum.addLongTask("stack", 101, 100, false, null);
        assertEquals("foreground_and_background", fields.get("long_task").get("app_state"));
        rum.addLongTask("old", 100, 100, true, null);
        assertEquals("unknown", fields.get("long_task").get("app_state"));
        assertEquals("sdk_init", fields.get("long_task").get("session_start_reason"));
    }

    @Test public void viewAndActionMetadataSurviveExistingAttributeJsonAndOldDataDefaults() throws Exception {
        rum.startView("persisted");
        ActiveViewBean view = active();
        view.appStateStart = 100;
        view.closedAppState = "foreground";
        ViewBean decoded = new ViewBean();
        decoded.setFromAttrJsonString(view.convertToViewBean().getAttrJsonString());
        assertEquals("sdk_init", decoded.getSessionStartReason());
        assertEquals(100, decoded.appStateStart);
        assertEquals(history.processToken, decoded.appStateProcess);
        assertEquals("foreground", decoded.closedAppState);
        ActiveActionBean action = new ActiveActionBean("tap", "click", view.getSessionId(),
                view.getId(), "persisted", "root", false);
        action.setSessionStartReason("inactivity_timeout");
        ActionBean decodedAction = new ActionBean();
        decodedAction.setFromAttrJsonString(action.convertToActionBean().getAttrJsonString());
        assertEquals("inactivity_timeout", decodedAction.getSessionStartReason());
        org.json.JSONObject old = new org.json.JSONObject(view.getAttrJsonString());
        old.remove("session_start_reason"); old.remove("app_state_process");
        old.remove("closed_app_state"); old.remove("app_state_start");
        decoded.setFromAttrJsonString(old.toString());
        assertEquals("unknown", decoded.getSessionStartReason());
        assertEquals("unknown", decoded.closedAppState);
        assertNull(decoded.appStateProcess);
    }

    @Test public void viewObservationsGrowAcrossTransitionsAndDoNotReuseAnotherProcessHistory() throws Exception {
        rum.startView("screen");
        ViewBean view = active().convertToViewBean();
        view.appStateStart = 100;
        advanceTo(200);
        history.record("background");
        advanceTo(300);
        assertEquals("foreground", rum.viewAppState(view, 200));
        assertEquals("foreground_and_background", rum.viewAppState(view, 300));
        view.appStateProcess = "previous-process";
        assertEquals("unknown", rum.viewAppState(view, 300));
        view.setClose(true);
        view.closedAppState = "foreground";
        assertEquals("foreground", rum.viewAppState(view, 300));
    }

    @Test public void queuedActionKeepsItsSessionReasonAndDoesNotAcquireIntervalState() throws Exception {
        rum.startView("screen");
        rum.startAction("tap", "click");
        Field activeAction = FTRUMInnerManager.class.getDeclaredField("activeAction");
        activeAction.setAccessible(true);
        ActionBean old = ((ActiveActionBean) activeAction.get(rum)).convertToActionBean();
        String originalSession = old.getSessionId();
        advanceTo(Utils.getCurrentNanoTime() + 1_000_000);
        rum.forceRefreshSessionId();
        Method insert = FTRUMInnerManager.class.getDeclaredMethod("insertAction", ActionBean.class);
        insert.setAccessible(true);
        insert.invoke(rum, old);
        assertEquals(originalSession, tags.get("action").get("session_id"));
        assertEquals("sdk_init", fields.get("action").get("session_start_reason"));
        assertFalse(fields.get("action").containsKey("app_state"));
    }

    private ActiveViewBean active() throws Exception {
        Field f = FTRUMInnerManager.class.getDeclaredField("activeView");
        f.setAccessible(true); return (ActiveViewBean) f.get(rum);
    }
    private ActiveActionBean action() throws Exception {
        Field f = FTRUMInnerManager.class.getDeclaredField("activeAction");
        f.setAccessible(true); return (ActiveActionBean) f.get(rum);
    }
    private void advanceTo(long time) { clock.advance(time - clock.now); }
    private ThreadPoolExecutor executor(boolean run) {
        return new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>()) {
            @Override public void execute(Runnable task) { if (run) task.run(); }
        };
    }
    private static Object replace(Class<?> type, Object owner, String name, Object value) throws Exception {
        Field field = type.getDeclaredField(name); field.setAccessible(true);
        Object previous = field.get(owner); field.set(owner, value); return previous;
    }
}
