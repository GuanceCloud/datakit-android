package com.ft.sdk;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.TouchPrivacy;
import com.ft.sdk.sessionreplay.internal.TouchPrivacyManager;
import com.ft.sdk.sessionreplay.internal.recorder.Node;
import com.ft.sdk.sessionreplay.internal.recorder.SnapshotProducer;
import com.ft.sdk.sessionreplay.internal.recorder.TreeViewTraversal;
import com.ft.sdk.sessionreplay.internal.recorder.ViewUtilsInternal;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.ViewWireframeMapper;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;
import com.ft.sdk.sessionreplay.utils.DefaultColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DefaultViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.DefaultViewIdentifierResolver;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.NoOpInternalLogger;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class FTHeatMapSwitchTest {
    @After
    public void tearDown() {
        FTRUMConfigManager.get().release();
    }

    @Test
    public void defaultsOffAndDoesNotEnableOtherCollection() {
        FTRUMConfig config = new FTRUMConfig();
        assertFalse(config.isEnableHeatMap());
        assertTrue(config.toString().contains("enableHeatMap=false"));
        assertSame(config, config.setEnableHeatMap(true));
        assertFalse(config.isEnableTraceUserAction());
        assertTrue(config.toString().contains("enableHeatMap=true"));
    }

    @Test
    public void disabledActionSkipsTargetResolutionButStillCallsActionHandler() throws Exception {
        AtomicReference<ActionEventWrapper> received = new AtomicReference<>();
        HeatMapTestConfig.set(new FTRUMConfig().setRumAppId("test-app")
                .setEnableTraceUserAction(true).setActionTrackingHandler(event -> {
                    received.set(event);
                    return null;
                }));
        CountingResources resources = new CountingResources();
        View view = target(resources, 0x7f010123);
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("business", "value");

        assertTrue(RUMHeatMapActionPropertyBuilder.build(view, extra).isEmpty());
        FTAutoTrack.clickView(view, ActionSourceType.CLICK_VIEW, extra);

        assertNotNull(received.get());
        assertSame(view, received.get().getSource());
        assertEquals(extra, received.get().getExtra());
        assertEquals(0, resources.queries);
    }

    @Test
    public void disabledTouchSkipsViewTreeAndReleaseClearsCachedTarget() throws Exception {
        View view = new View(RuntimeEnvironment.application) {
            @Override
            public int getVisibility() {
                throw new AssertionError("Heatmap touch traversal must not start");
            }
        };
        long now = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(now, now, MotionEvent.ACTION_UP, 10f, 10f, 0);
        try {
            HeatMapTestConfig.set(new FTRUMConfig().setRumAppId("test-app"));
            RUMTouchPositionTracker.record(event, view);
            FTAutoTrack.trackWindowTouch(event);
            assertNull(RUMTouchPositionTracker.resolveLastTouchTarget());

            HeatMapTestConfig.set(new FTRUMConfig().setRumAppId("test-app").setEnableHeatMap(true));
            View clickable = target(new CountingResources(), 1);
            RUMTouchPositionTracker.record(event, clickable);
            assertSame(clickable, RUMTouchPositionTracker.resolveLastTouchTarget());
            FTRUMConfigManager.get().release();
            assertFalse(SessionReplayManager.get().isHeatMapEnabled());
            assertNull(RUMTouchPositionTracker.resolveLastTouchTarget());
        } finally {
            event.recycle();
        }
    }

    @Test
    public void disabledLegacyReplayEntrySkipsAllResourceIds() throws Exception {
        HeatMapTestConfig.set(new FTRUMConfig().setRumAppId("test-app"));
        CountingResources resources = new CountingResources();
        for (int id : new int[]{1, View.NO_ID, android.R.id.text1, 0x7f010123}) {
            assertNull(SessionReplayManager.get().resolvePermanentId(target(resources, id)));
        }
        assertEquals(0, resources.queries);
    }

    @Test
    public void snapshotOffPreservesRecordingAndOnUsesSameIdAsAction() throws Exception {
        CountingResources resources = new CountingResources();
        FrameLayout root = new FrameLayout(contextWith(resources));
        root.setId(0x7f010001);
        root.layout(0, 0, 200, 200);
        View visible = target(resources, 0x7f010123);
        visible.setBackgroundColor(Color.RED);
        View transparent = target(resources, 2);
        root.addView(visible);
        root.addView(transparent);
        SnapshotProducer producer = producer();
        SystemInformation system = new SystemInformation(new GlobalBounds(0, 0, 200, 200),
                0, 1f, null);

        HeatMapTestConfig.set(new FTRUMConfig().setRumAppId("test-app"));
        Node disabled = producer.produce(root, system, TextAndInputPrivacy.MASK_SENSITIVE_INPUTS,
                ImagePrivacy.MASK_NONE, null);
        assertEquals(2, disabled.getChildren().size());
        ShapeWireframe ordinary = (ShapeWireframe) disabled.getChildren().get(0).getWireframes().get(0);
        assertNull(ordinary.getPermanentId());
        assertEquals(100L, ordinary.getWidth());
        assertEquals(50L, ordinary.getHeight());
        assertTrue(disabled.getChildren().get(1).getWireframes().isEmpty());
        assertEquals(0, resources.queries);

        HeatMapTestConfig.set(new FTRUMConfig().setRumAppId("test-app").setEnableHeatMap(true));
        assertFalse(FTRUMConfigManager.get().getConfig().isEnableTraceUserAction());
        Node enabled = producer.produce(root, system, TextAndInputPrivacy.MASK_SENSITIVE_INPUTS,
                ImagePrivacy.MASK_NONE, null);
        String replayId = enabled.getChildren().get(0).getWireframes().get(0).getPermanentId();
        assertNotNull(replayId);
        assertTrue(replayId.matches("[0-9a-f]{32}"));
        assertEquals(1, enabled.getChildren().get(1).getWireframes().size());
        assertTrue(resources.queries > 0);
        HashMap<String, Object> action = RUMHeatMapActionPropertyBuilder.build(visible, null);
        assertEquals(replayId, new JSONObject((String) action.get(Constants.KEY_RUM_ACTION_TARGET))
                .getString("permanent_id"));
    }

    @Test
    public void disabledRumSerializationPreservesOrdinaryAndCallerDisplayFields() throws Exception {
        HeatMapTestConfig.set(new FTRUMConfig().setRumAppId("test-app"));
        SyncDataHelper helper = new SyncDataHelper();
        HashMap<String, Object> fields = new HashMap<>();
        fields.put("custom", "value");
        helper.getBodyContent(Constants.FT_MEASUREMENT_RUM_VIEW, new HashMap<>(), fields,
                1L, DataType.RUM_APP, "uuid");
        assertFalse(fields.containsKey(Constants.KEY_RUM_DISPLAY));
        assertEquals("value", fields.get("custom"));
        fields.put(Constants.KEY_RUM_DISPLAY, "caller-display");
        helper.getBodyContent(Constants.FT_MEASUREMENT_RUM_VIEW, new HashMap<>(), fields,
                1L, DataType.RUM_APP, "uuid");
        assertEquals("caller-display", fields.get(Constants.KEY_RUM_DISPLAY));
    }

    private SnapshotProducer producer() {
        NoOpInternalLogger logger = new NoOpInternalLogger();
        ViewWireframeMapper mapper = new ViewWireframeMapper(DefaultViewIdentifierResolver.get(),
                DefaultColorStringFormatter.get(), DefaultViewBoundsResolver.get(),
                (drawable, internalLogger) -> drawable instanceof ColorDrawable
                        ? ((ColorDrawable) drawable).getColor() : null);
        ViewUtilsInternal viewUtils = new ViewUtilsInternal() {
            @Override
            public boolean isNotVisible(View view) {
                return false;
            }
        };
        TreeViewTraversal traversal = new TreeViewTraversal(Collections.emptyList(), mapper, mapper,
                viewUtils, null, logger);
        return new SnapshotProducer(null, traversal, parent -> false,
                new TouchPrivacyManager(TouchPrivacy.SHOW), logger, SessionReplayManager.get());
    }

    private View target(CountingResources resources, int id) {
        View view = new View(contextWith(resources));
        view.setId(id);
        view.setClickable(true);
        view.layout(0, 0, 100, 50);
        return view;
    }

    private Context contextWith(Resources resources) {
        return new ContextWrapper(RuntimeEnvironment.application) {
            @Override
            public Resources getResources() {
                return resources;
            }
        };
    }

    private static class CountingResources extends Resources {
        int queries;

        CountingResources() {
            super(RuntimeEnvironment.application.getAssets(),
                    RuntimeEnvironment.application.getResources().getDisplayMetrics(),
                    RuntimeEnvironment.application.getResources().getConfiguration());
        }

        @Override
        public String getResourceName(int id) throws NotFoundException {
            queries++;
            // Valid-format missing IDs still query Resources when Heatmap is enabled.
            throw new NotFoundException("missing test resource");
        }
    }
}
