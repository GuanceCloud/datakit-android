package com.ft.sdk.sessionreplay.internal.processor;

import com.ft.sdk.sessionreplay.SlotIdWebviewBinder;
import com.ft.sdk.sessionreplay.model.MobileMutationData;
import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.TextStyle;
import com.ft.sdk.sessionreplay.model.TextWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.model.WireframeClip;
import com.ft.sdk.sessionreplay.model.WireframeUpdateMutation;
import com.ft.sdk.sessionreplay.model.WebviewWireframe;
import com.ft.sdk.sessionreplay.model.WebviewWireframeUpdate;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MutationResolverTest {

    @Test
    public void resolveMutations_shouldReturnNullWhenSnapshotsAreEqual() {
        MutationResolver resolver = new MutationResolver(new TestLogger(), null);
        Wireframe wireframe = shapeWireframe(1L, 10L);

        MobileMutationData mutationData = resolver.resolveMutations(
                Collections.singletonList(wireframe),
                Collections.singletonList(wireframe)
        );

        assertNull(mutationData);
    }

    @Test
    public void resolveMutations_shouldReturnAddsAndRemovesForReorderedItems() {
        MutationResolver resolver = new MutationResolver(new TestLogger(), null);
        Wireframe first = shapeWireframe(1L, 10L);
        Wireframe second = shapeWireframe(2L, 20L);

        MobileMutationData mutationData = resolver.resolveMutations(
                Arrays.asList(first, second),
                Arrays.asList(second, first)
        );

        assertNotNull(mutationData);
        // A reordering is represented as remove+add pairs because the replay payload models
        // insertion order explicitly.
        assertEquals(2, mutationData.adds.size());
        assertEquals(2, mutationData.removes.size());
        assertEquals(Long.valueOf(2L), mutationData.removes.get(0).id);
        assertEquals(Long.valueOf(1L), mutationData.removes.get(1).id);
        assertEquals(Long.valueOf(2L), mutationData.adds.get(0).wireframe.getId());
        assertNull(mutationData.adds.get(0).previousId);
        assertEquals(Long.valueOf(1L), mutationData.adds.get(1).wireframe.getId());
        assertEquals(Long.valueOf(2L), mutationData.adds.get(1).previousId);
    }

    @Test
    public void resolveUpdateMutation_shouldCreateTextUpdateAndResetMissingClip() {
        MutationResolver resolver = new MutationResolver(new TestLogger(), null);
        TextWireframe previous = textWireframe(
                1L,
                10L,
                "before",
                new WireframeClip(1L, 2L, 3L, 4L)
        );
        TextWireframe current = textWireframe(1L, 15L, "after", null);

        WireframeUpdateMutation update = resolver.resolveUpdateMutation(previous, current);

        assertNotNull(update);
        String asJson = update.toJson().toString();
        assertTrue(asJson.contains("\"x\":15"));
        assertTrue(asJson.contains("\"text\":\"after\""));
        // A removed clip is serialized as a zeroed clip to make the reset explicit downstream.
        assertTrue(asJson.contains("\"clip\":{\"top\":0,\"bottom\":0,\"left\":0,\"right\":0}"));
    }

    @Test
    public void resolveMutations_shouldMarkRemovedWebviewAsInvisibleAndDeactivateBinding() {
        SlotIdWebviewBinder binder = new SlotIdWebviewBinder(new TestLogger());
        binder.bind(42L, "view-42");
        final AtomicBoolean reboundTriggered = new AtomicBoolean(false);
        binder.setSlotRebindCallback(42L, new SlotIdWebviewBinder.SlotRebindCallBack() {
            @Override
            public void onSlotRebound(long slotId) {
                reboundTriggered.set(true);
            }
        });

        MutationResolver resolver = new MutationResolver(new TestLogger(), binder);
        WebviewWireframe webview = new WebviewWireframe(
                10L, 0L, 0L, 100L, 50L, null, null, null, "42", true
        );

        MobileMutationData mutationData = resolver.resolveMutations(
                Collections.<Wireframe>singletonList(webview),
                Collections.<Wireframe>emptyList()
        );

        assertNotNull(mutationData);
        assertEquals(1, mutationData.updates.size());
        assertTrue(mutationData.updates.get(0) instanceof WebviewWireframeUpdate);
        WebviewWireframeUpdate update = (WebviewWireframeUpdate) mutationData.updates.get(0);
        assertEquals("42", update.slotId);
        assertFalse(update.isVisible);
        assertTrue(mutationData.removes.isEmpty());

        // Rebinding the same slot verifies the resolver marked the previous binding inactive.
        binder.bind(42L, "view-42");
        assertTrue(reboundTriggered.get());
    }

    private static ShapeWireframe shapeWireframe(long id, long x) {
        return new ShapeWireframe(id, x, 0L, 20L, 20L, null,
                new ShapeStyle("#112233ff", 1f, null), null);
    }

    private static TextWireframe textWireframe(long id, long x, String text, WireframeClip clip) {
        return new TextWireframe(
                id,
                x,
                5L,
                50L,
                12L,
                clip,
                new ShapeStyle("#ffffff", 1f, null),
                null,
                text,
                new TextStyle("sans", 12L, "#000000"),
                null
        );
    }

    private static class TestLogger implements InternalLogger {
        @Override public void i(String tag, String message) { }
        @Override public void i(String tag, String message, boolean onlyOnce) { }
        @Override public void d(String tag, String message) { }
        @Override public void d(String tag, String message, boolean onlyOnce) { }
        @Override public void e(String tag, String message) { }
        @Override public void e(String tag, String message, boolean onlyOnce) { }
        @Override public void e(String tag, String message, Throwable e) { }
        @Override public void e(String tag, String message, Throwable e, boolean onlyOnce) { }
        @Override public void v(String tag, String message) { }
        @Override public void v(String tag, String message, boolean onlyOnce) { }
        @Override public void w(String tag, String message) { }
        @Override public void w(String tag, String message, boolean onlyOnce) { }
        @Override public void w(String tag, String message, Throwable e) { }
        @Override public void w(String tag, String message, Throwable e, boolean onlyOnce) { }
    }
}
