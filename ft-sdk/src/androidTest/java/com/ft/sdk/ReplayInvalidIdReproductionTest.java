package com.ft.sdk;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.sessionreplay.internal.recorder.TreeViewTraversal;
import com.ft.sdk.sessionreplay.internal.recorder.SnapshotProducer;
import com.ft.sdk.sessionreplay.internal.recorder.Node;
import com.ft.sdk.sessionreplay.internal.recorder.ViewUtilsInternal;
import com.ft.sdk.sessionreplay.internal.recorder.mapper.ViewWireframeMapper;
import com.ft.sdk.sessionreplay.internal.TouchPrivacyManager;
import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.TouchPrivacy;
import com.ft.sdk.sessionreplay.recorder.SystemInformation;
import com.ft.sdk.sessionreplay.utils.DefaultColorStringFormatter;
import com.ft.sdk.sessionreplay.utils.DefaultViewBoundsResolver;
import com.ft.sdk.sessionreplay.utils.DefaultViewIdentifierResolver;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapperFactory;
import com.ft.sdk.sessionreplay.utils.GlobalBounds;
import com.ft.sdk.sessionreplay.utils.NoOpInternalLogger;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Device-only diagnostic: markers delimit framework errors from each replay phase. */
@RunWith(AndroidJUnit4.class)
public class ReplayInvalidIdReproductionTest {
    private static final String TAG = "FTInvalidIdRepro";

    @Before
    public void installSdkWithoutUpload() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FTSdk.install(FTSDKConfig.builder().setAutoSync(false).setDebug(true));
            assertNotNull(FTSdk.get());
        });
    }

    @After
    public void shutdownSdk() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(FTSdk::shutDown);
    }

    private void configureHeatMap(boolean enabled) {
        FTRUMConfig config = new FTRUMConfig().setRumAppId("heatmap-device-test")
                .setEnableTraceUserAction(false).setEnableHeatMap(enabled);
        FTSdk.initRUMWithConfig(config);
        assertEquals(enabled, FTRUMConfigManager.get().isHeatMapEnabled());
        assertEquals(enabled, SessionReplayManager.get().isHeatMapEnabled());
        assertFalse(config.isEnableTraceUserAction());
        // Mutating the builder after initialization must not change the active value.
        config.setEnableHeatMap(!enabled);
        assertEquals(enabled, SessionReplayManager.get().isHeatMapEnabled());
        config.setEnableHeatMap(enabled);
    }

    @Test
    public void compareRealViewSnapshotsWithHeatMapOffAndOn() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, ReplayHeatMapTestActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ReplayHeatMapTestActivity activity = (ReplayHeatMapTestActivity)
                InstrumentationRegistry.getInstrumentation().startActivitySync(intent);
        try {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                for (boolean enabled : new boolean[]{false, true}) {
                    configureHeatMap(enabled);
                    runSnapshotPhase(activity, "snapshot_" + (enabled ? "enabled" : "disabled")
                            + "_dynamic", new int[]{1, 2, 3, 4}, enabled);
                    runSnapshotPhase(activity, "snapshot_" + (enabled ? "enabled" : "disabled")
                            + "_resource", new int[]{android.R.id.content, android.R.id.widget_frame,
                            android.R.id.background, android.R.id.text1}, enabled);
                }
            });
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(activity::finish);
        }
    }

    private void runSnapshotPhase(ReplayHeatMapTestActivity activity, String name, int[] ids,
                                  boolean enabled) {
        CountingResources resources = new CountingResources(activity.getResources());
        Context context = new ContextWrapper(activity) {
            @Override
            public Resources getResources() {
                return resources;
            }
        };
        LinearLayout root = new LinearLayout(context);
        root.setId(ids[0]);
        LinearLayout parent = new LinearLayout(context);
        parent.setId(ids[1]);
        LinearLayout inner = new LinearLayout(context);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setId(ids[2]);
        root.addView(parent);
        parent.addView(inner);
        for (int i = 0; i < 13; i++) {
            View child = new View(context);
            child.setId(ids[3]);
            child.setClickable(true);
            if (i < 12) {
                child.setBackgroundColor(Color.RED);
            }
            inner.addView(child, new LinearLayout.LayoutParams(100, 30));
        }
        activity.setContentView(root);
        int exactSize = View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY);
        root.measure(exactSize, exactSize);
        root.layout(0, 0, 600, 600);
        assertTrue("The fixture must be visible in an actual Activity", root.isShown());
        assertTrue(inner.getChildAt(0).isShown());
        SnapshotProducer producer = createSnapshotProducer();
        SystemInformation system = new SystemInformation(new GlobalBounds(0, 0, 600, 600),
                0, 1f, null);
        resources.nameQueries = 0;
        int ordinaryWireframes = 0;
        int bindingWireframes = 0;
        Log.i(TAG, "BEGIN " + name + " heatMap=" + enabled + " autoAction=false");
        for (int pass = 0; pass < 3; pass++) {
            Node snapshot = producer.produce(root, system, TextAndInputPrivacy.MASK_SENSITIVE_INPUTS,
                    ImagePrivacy.MASK_NONE, null);
            Node innerNode = snapshot.getChildren().get(0).getChildren().get(0);
            assertEquals(13, innerNode.getChildren().size());
            for (int i = 0; i < 12; i++) {
                List<Wireframe> wireframes = innerNode.getChildren().get(i).getWireframes();
                assertEquals(1, wireframes.size());
                ShapeWireframe wireframe = (ShapeWireframe) wireframes.get(0);
                assertEquals(100L, wireframe.getWidth());
                assertEquals(30L, wireframe.getHeight());
                assertNotNull(wireframe.getShapeStyle());
                if (enabled) {
                    assertNotNull(wireframe.getPermanentId());
                    assertTrue(wireframe.getPermanentId().matches("[0-9a-f]{32}"));
                } else {
                    assertNull(wireframe.getPermanentId());
                }
                ordinaryWireframes++;
            }
            List<Wireframe> binding = innerNode.getChildren().get(12).getWireframes();
            assertEquals(enabled ? 1 : 0, binding.size());
            bindingWireframes += binding.size();
        }
        if (!enabled || name.endsWith("_dynamic")) {
            assertEquals(0, resources.nameQueries);
        } else {
            assertTrue("Enabled legal IDs must exercise real resource lookup", resources.nameQueries >= 156);
        }
        Log.i(TAG, "END " + name + " snapshots=3 ordinaryWireframes=" + ordinaryWireframes
                + " transparentBindings=" + bindingWireframes
                + " resourceQueries=" + resources.nameQueries);
    }

    private SnapshotProducer createSnapshotProducer() {
        NoOpInternalLogger logger = new NoOpInternalLogger();
        ViewWireframeMapper mapper = new ViewWireframeMapper(DefaultViewIdentifierResolver.get(),
                DefaultColorStringFormatter.get(), DefaultViewBoundsResolver.get(),
                DrawableToColorMapperFactory.getDefault());
        TreeViewTraversal traversal = new TreeViewTraversal(Collections.emptyList(), mapper, mapper,
                new ViewUtilsInternal(), null, logger);
        return new SnapshotProducer(null, traversal, view -> false,
                new TouchPrivacyManager(TouchPrivacy.SHOW), logger, SessionReplayManager.get());
    }

    @Test
    public void compareReplayPermanentIdWithDynamicAndResourceIds() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            assertEquals(Looper.getMainLooper(), Looper.myLooper());
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            Log.i(TAG, "BEGIN framework_probe");
            int failures = 0;
            for (int id = 1; id <= 4; id++) {
                try {
                    context.getResources().getResourceName(id);
                } catch (Resources.NotFoundException expected) {
                    failures++;
                }
            }
            assertEquals(4, failures);
            Log.i(TAG, "END framework_probe notFound=" + failures);

            for (boolean enabled : new boolean[]{false, true}) {
                configureHeatMap(enabled);
                String prefix = enabled ? "enabled_" : "disabled_";
                runReplayPhase(context, prefix + "dynamic_ids", new int[]{1, 2, 3, 4}, true, enabled);
                runReplayPhase(context, prefix + "resource_ids", new int[]{android.R.id.content,
                        android.R.id.widget_frame, android.R.id.background, android.R.id.text1}, true, enabled);
                runReplayPhase(context, prefix + "no_ids", new int[]{View.NO_ID, View.NO_ID,
                        View.NO_ID, View.NO_ID}, true, enabled);
            }
            runReplayPhase(context, "dynamic_ids_skip_attachment", new int[]{1, 2, 3, 4}, false, true);
        });
    }

    private void runReplayPhase(Context baseContext, String name, int[] ids, boolean attach, boolean enabled) {
        CountingResources resources = new CountingResources(baseContext.getResources());
        Context context = new ContextWrapper(baseContext) {
            @Override
            public Resources getResources() {
                return resources;
            }
        };
        LinearLayout root = new LinearLayout(context);
        root.setId(ids[0]);
        LinearLayout parent = new LinearLayout(context);
        parent.setId(ids[1]);
        root.addView(parent);
        LinearLayout inner = new LinearLayout(context);
        inner.setId(ids[2]);
        parent.addView(inner);
        for (int i = 0; i < 12; i++) {
            View child = new View(context);
            child.setId(ids[3]);
            inner.addView(child);
        }
        // Exercise the actual replay attachment and its reflective Agent bridge without
        // starting recording or uploading any data; RUM uses its public initialization API.
        TreeViewTraversal traversal = new TreeViewTraversal(null, null, null, null, null, null);
        try {
            Method method = TreeViewTraversal.class.getDeclaredMethod(
                    "attachPermanentId", View.class, List.class);
            method.setAccessible(true);
            resources.nameQueries = 0;
            Log.i(TAG, "BEGIN " + name + " main=" + (Looper.myLooper() == Looper.getMainLooper()));
            long start = System.nanoTime();
            for (int pass = 0; pass < 3; pass++) {
                for (int i = 0; i < inner.getChildCount(); i++) {
                    Wireframe wireframe = new ShapeWireframe(i, 0L, 0L, 10L, 10L,
                            null, null, null);
                    if (attach) {
                        method.invoke(traversal, inner.getChildAt(i), Collections.singletonList(wireframe));
                        if (enabled) {
                            assertNotNull(wireframe.getPermanentId());
                            assertTrue(wireframe.getPermanentId().matches("[0-9a-f]{32}"));
                        } else {
                            assertNull(wireframe.getPermanentId());
                        }
                    }
                }
            }
            assertEquals("Unexpected resource queries in " + name,
                    enabled && name.endsWith("resource_ids") ? 144 : 0, resources.nameQueries);
            Log.i(TAG, "END " + name + " calls=" + (attach ? 36 : 0)
                    + " resourceQueries=" + resources.nameQueries
                    + " elapsedMs=" + ((System.nanoTime() - start) / 1000000.0));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Replay attachment failed", e);
        }
    }

    private static class CountingResources extends Resources {
        private final Resources delegate;
        int nameQueries;

        CountingResources(Resources delegate) {
            super(delegate.getAssets(), delegate.getDisplayMetrics(), delegate.getConfiguration());
            this.delegate = delegate;
        }

        @Override
        public String getResourceName(int id) throws NotFoundException {
            nameQueries++;
            return delegate.getResourceName(id);
        }
    }
}
