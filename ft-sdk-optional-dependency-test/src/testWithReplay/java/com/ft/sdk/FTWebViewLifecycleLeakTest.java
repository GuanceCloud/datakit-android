package com.ft.sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.webkit.WebView;

import com.ft.sdk.optionaldependency.OptionalDependencyApplication;
import com.ft.sdk.sessionreplay.FTSessionReplayConfig;
import com.ft.sdk.sessionreplay.SlotIdWebviewBinder;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RunWith(RobolectricTestRunner.class)
@Config(application = OptionalDependencyApplication.class, sdk = 28)
public class FTWebViewLifecycleLeakTest {

    private static final long SLOT_ID = 4242L;
    private static final String INITIAL_VIEW_ID = "native-view-1";

    private SessionReplayManager manager;
    private SlotIdWebviewBinder binder;

    @Before
    public void setUp() {
        FTSdk.install(FTSDKConfig.builder()
                .setOnlySupportMainProcess(false)
                .setAutoSync(false)
                .setEnableAccessAndroidID(false));
        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setRumAppId("rum-app-id")
                .setEnableTraceWebView(true));
        FTSdk.initSessionReplayConfig(new FTSessionReplayConfig()
                .setSampleRate(1f)
                .enableLinkRUMKeys(new String[]{"wgt_id"}));

        assertTrue(FTSdk.isSessionReplaySupport());
        manager = SessionReplayManager.get();
        binder = manager.getSlotIdWebviewBinder();
        assertNotNull(binder);
        binder.clear();
        manager.getFieldLinkMap().clear();
        manager.getTagLinkMap().clear();
    }

    @After
    public void tearDown() {
        if (binder != null) {
            binder.clear();
        }
        FTSdk.shutDown();
    }

    @Test
    public void slotCallbacksDoNotRetainReleasedWebViewHandler() throws Exception {
        ReferenceQueue<FTWebViewHandler> collectedHandlers = new ReferenceQueue<>();
        WeakReference<FTWebViewHandler> handlerReference =
                registerAndReleaseHandler(collectedHandlers);

        awaitCollection(handlerReference, collectedHandlers);

        assertNull("Slot callbacks retained FTWebViewHandler", handlerReference.get());
        assertEquals(INITIAL_VIEW_ID, binder.getViewId(SLOT_ID));
    }

    @Test
    public void liveHandlerStillReceivesNativeViewRebind() throws Exception {
        FTWebViewHandler handler = new FTWebViewHandler();
        setField(handler, "slotID", SLOT_ID);
        setField(handler, "webViewId", "web-view-1");

        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, Map<String, Object>> webViewLinkMap =
                (ConcurrentHashMap<String, Map<String, Object>>) getField(
                        handler, "webViewLinkMap");
        Map<String, Object> rumLinkData = new ConcurrentHashMap<>();
        rumLinkData.put("wgt_id", "widget-1");
        webViewLinkMap.put("web-view-1", rumLinkData);

        binder.bind(SLOT_ID, INITIAL_VIEW_ID);
        registerViewChangeCallback(handler, SLOT_ID, INITIAL_VIEW_ID);

        binder.bind(SLOT_ID, "native-view-2");

        assertEquals("widget-1",
                manager.getFieldLinkMap().get("native-view-2").get("wgt_id"));
    }

    @Test
    public void repeatedDestroyedActivitiesAndWebViewsAreCollectibleWhileMappingsRemain()
            throws Exception {
        List<ActivityScenarioReferences> destroyedScenarios = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            destroyedScenarios.add(createAndDestroyWebViewActivity());
        }

        for (ActivityScenarioReferences references : destroyedScenarios) {
            awaitCollection(references.handlerReference, references.handlerQueue);
            awaitCollection(references.webViewReference, references.webViewQueue);
            awaitCollection(references.activityReference, references.activityQueue);

            assertNull("Destroyed FTWebViewHandler was retained",
                    references.handlerReference.get());
            assertNull("Destroyed WebView was retained", references.webViewReference.get());
            assertNull("Destroyed Activity was retained", references.activityReference.get());
        }

        ActivityScenarioReferences latestScenario =
                destroyedScenarios.get(destroyedScenarios.size() - 1);
        assertEquals(latestScenario.reboundViewId,
                binder.getViewId(latestScenario.slotId));
    }

    @Test
    public void unbindRemovesOnlyRequestedSlot() {
        long remainingSlotId = SLOT_ID + 1;
        binder.bind(SLOT_ID, INITIAL_VIEW_ID);
        binder.bind(remainingSlotId, "remaining-view");

        SessionReplayBridge.unbindSlot(SLOT_ID);

        assertNull(binder.getViewId(SLOT_ID));
        assertEquals("remaining-view", binder.getViewId(remainingSlotId));
    }

    @Test
    public void sdkShutdownClearsBindingsAndFeatureReference() {
        binder.bind(SLOT_ID, INITIAL_VIEW_ID);

        FTSdk.shutDown();

        assertNull(binder.getViewId(SLOT_ID));
        assertNull(manager.getSlotIdWebviewBinder());
    }

    private ActivityScenarioReferences createAndDestroyWebViewActivity() throws Exception {
        ActivityController<WebViewHostActivity> controller =
                Robolectric.buildActivity(WebViewHostActivity.class).setup();
        WebViewHostActivity activity = controller.get();
        WebView webView = new WebView(activity);
        activity.setContentView(webView);

        FTWebViewHandler handler = new FTWebViewHandler();
        handler.setWebView(webView);
        long slotId = System.identityHashCode(webView);
        String linkedValue = "widget-" + slotId;
        String reboundViewId = "native-view-" + slotId;
        binder.bind(slotId, INITIAL_VIEW_ID);

        JSONObject eventData = new JSONObject()
                .put("measurement", "view")
                .put("tags", new JSONObject()
                        .put("view_id", "web-view-" + slotId)
                        .put("wgt_id", linkedValue))
                .put("fields", new JSONObject())
                .put("time", System.currentTimeMillis());
        handler.sendEvent(new JSONObject()
                .put("name", "rum")
                .put("data", eventData)
                .toString());

        binder.bind(slotId, reboundViewId);
        assertEquals(linkedValue,
                manager.getFieldLinkMap().get(reboundViewId).get("wgt_id"));

        ActivityScenarioReferences references = new ActivityScenarioReferences(
                handler, webView, activity, slotId, reboundViewId);
        controller.pause().stop().destroy();
        webView.destroy();
        return references;
    }

    private WeakReference<FTWebViewHandler> registerAndReleaseHandler(
            ReferenceQueue<FTWebViewHandler> collectedHandlers) throws Exception {
        FTWebViewHandler handler = new FTWebViewHandler();
        setField(handler, "slotID", SLOT_ID);
        binder.bind(SLOT_ID, INITIAL_VIEW_ID);
        registerViewChangeCallback(handler, SLOT_ID, INITIAL_VIEW_ID);
        return new WeakReference<>(handler, collectedHandlers);
    }

    private static void registerViewChangeCallback(FTWebViewHandler handler, long slotId,
                                                   String viewId) throws Exception {
        Method register = FTWebViewHandler.class.getDeclaredMethod(
                "registerViewChangeCallback", long.class, String.class);
        register.setAccessible(true);
        register.invoke(handler, slotId, viewId);
    }

    private static Object getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static <T> void awaitCollection(WeakReference<T> reference,
                                            ReferenceQueue<T> referenceQueue)
            throws InterruptedException {
        for (int i = 0; i < 30; i++) {
            System.gc();
            System.runFinalization();
            if (referenceQueue.remove(50) != null || reference.get() == null) {
                return;
            }
        }
    }

    public static class WebViewHostActivity extends Activity {
    }

    private static final class ActivityScenarioReferences {
        private final ReferenceQueue<FTWebViewHandler> handlerQueue = new ReferenceQueue<>();
        private final ReferenceQueue<WebView> webViewQueue = new ReferenceQueue<>();
        private final ReferenceQueue<Activity> activityQueue = new ReferenceQueue<>();
        private final WeakReference<FTWebViewHandler> handlerReference;
        private final WeakReference<WebView> webViewReference;
        private final WeakReference<Activity> activityReference;
        private final long slotId;
        private final String reboundViewId;

        private ActivityScenarioReferences(FTWebViewHandler handler, WebView webView,
                                           Activity activity, long slotId,
                                           String reboundViewId) {
            handlerReference = new WeakReference<>(handler, handlerQueue);
            webViewReference = new WeakReference<>(webView, webViewQueue);
            activityReference = new WeakReference<>(activity, activityQueue);
            this.slotId = slotId;
            this.reboundViewId = reboundViewId;
        }
    }
}
