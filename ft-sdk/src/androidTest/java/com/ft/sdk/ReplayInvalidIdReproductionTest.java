package com.ft.sdk;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.sdk.sessionreplay.internal.recorder.TreeViewTraversal;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/** Device-only diagnostic: markers delimit framework errors from each replay phase. */
@RunWith(AndroidJUnit4.class)
public class ReplayInvalidIdReproductionTest {
    private static final String TAG = "FTInvalidIdRepro";

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

            runReplayPhase(context, "dynamic_ids", new int[]{1, 2, 3, 4}, true);
            runReplayPhase(context, "resource_ids", new int[]{android.R.id.content,
                    android.R.id.widget_frame, android.R.id.background, android.R.id.text1}, true);
            runReplayPhase(context, "no_ids", new int[]{View.NO_ID, View.NO_ID,
                    View.NO_ID, View.NO_ID}, true);
            runReplayPhase(context, "dynamic_ids_skip_attachment", new int[]{1, 2, 3, 4}, false);
        });
    }

    private void runReplayPhase(Context baseContext, String name, int[] ids, boolean attach) {
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
        // starting recording, installing the SDK, or uploading any data.
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
                        assertNotNull(wireframe.getPermanentId());
                        assertTrue(wireframe.getPermanentId().matches("[0-9a-f]{32}"));
                    }
                }
            }
            assertEquals("Unexpected resource queries in " + name,
                    name.equals("resource_ids") ? 144 : 0, resources.nameQueries);
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
