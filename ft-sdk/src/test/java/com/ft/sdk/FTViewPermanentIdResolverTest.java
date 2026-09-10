package com.ft.sdk;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.view.View;
import android.widget.LinearLayout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 28)
public class FTViewPermanentIdResolverTest {

    @Test
    public void nonResourceIdsKeepFallbackWithoutQueryingResources() {
        RecordingResources resources = new RecordingResources();
        View view = new View(contextWith(resources));
        String fallbackId = FTViewPermanentIdResolver.resolve(view);
        int[] ids = {View.NO_ID, 0, 1, 2, 3, 4, View.generateViewId(),
                0x00ffffff, 0x00010001, 0x7f000001, 0x01000001};

        for (int id : ids) {
            view.setId(id);
            assertEquals("Fallback changed for ID " + Integer.toHexString(id),
                    fallbackId, FTViewPermanentIdResolver.resolve(view));
        }

        assertTrue("Non-resource IDs must not reach Resources", resources.queriedIds.isEmpty());
    }

    @Test
    public void resourceIdsKeepNamesAcrossPackageIdsIncludingSignBit() {
        RecordingResources resources = new RecordingResources();
        View view = new View(contextWith(resources));
        int[] ids = {android.R.id.text1, 0x7f010001, 0x80010001, 0xff010001};

        for (int id : ids) {
            String name = "package" + Integer.toHexString(id) + ":id/target";
            resources.names.put(id, name);
            view.setId(id);
            assertTrue(FTViewPermanentIdResolver.resolvePath(view)
                    .endsWith("/" + name.replace("/", "%2F") + "#0"));
        }

        assertEquals(ids.length, resources.queriedIds.size());
    }

    @Test
    public void missingResourceEntryStillUsesExistingFallback() {
        RecordingResources resources = new RecordingResources();
        View view = new View(contextWith(resources));
        String fallbackId = FTViewPermanentIdResolver.resolve(view);
        view.setId(0x7f010123);

        assertEquals(fallbackId, FTViewPermanentIdResolver.resolve(view));
        assertEquals(1, resources.queriedIds.size());
        assertEquals(Integer.valueOf(0x7f010123), resources.queriedIds.get(0));
    }

    @Test
    public void dynamicAncestorsPreservePathsAndSiblingIdentityWithoutQueries() {
        RecordingResources resources = new RecordingResources();
        Context context = contextWith(resources);
        LinearLayout root = new LinearLayout(context);
        LinearLayout parent = new LinearLayout(context);
        View first = new View(context);
        View second = new View(context);
        root.addView(parent);
        parent.addView(first);
        parent.addView(second);
        String firstFallback = FTViewPermanentIdResolver.resolve(first);
        String secondFallback = FTViewPermanentIdResolver.resolve(second);

        root.setId(1);
        parent.setId(2);
        first.setId(3);
        second.setId(4);
        assertEquals(firstFallback, FTViewPermanentIdResolver.resolve(first));
        assertEquals(secondFallback, FTViewPermanentIdResolver.resolve(second));
        assertNotEquals(firstFallback, secondFallback);

        parent.removeView(first);
        parent.addView(first);
        assertEquals(secondFallback, FTViewPermanentIdResolver.resolve(first));
        assertEquals(firstFallback, FTViewPermanentIdResolver.resolve(second));
        assertTrue(resources.queriedIds.isEmpty());
    }

    private Context contextWith(Resources resources) {
        return new ContextWrapper(RuntimeEnvironment.application) {
            @Override
            public Resources getResources() {
                return resources;
            }
        };
    }

    private static class RecordingResources extends Resources {
        final List<Integer> queriedIds = new ArrayList<>();
        final Map<Integer, String> names = new HashMap<>();

        RecordingResources() {
            super(RuntimeEnvironment.application.getAssets(),
                    RuntimeEnvironment.application.getResources().getDisplayMetrics(),
                    RuntimeEnvironment.application.getResources().getConfiguration());
        }

        @Override
        public String getResourceName(int id) throws NotFoundException {
            queriedIds.add(id);
            if (names.containsKey(id)) {
                return names.get(id);
            }
            throw new NotFoundException("No resource for " + Integer.toHexString(id));
        }
    }
}
