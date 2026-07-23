package com.ft.sdk.sessionreplay.internal.processor;

import com.ft.sdk.sessionreplay.internal.recorder.Node;
import com.ft.sdk.sessionreplay.model.ShapeStyle;
import com.ft.sdk.sessionreplay.model.ShapeWireframe;
import com.ft.sdk.sessionreplay.model.Wireframe;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class NodeFlattenerTest {

    @Test
    public void transparentHeatmapBindingIsRetainedWithPermanentId() {
        ShapeWireframe binding = new ShapeWireframe(
                42L,
                10L,
                20L,
                100L,
                50L,
                null,
                new ShapeStyle("#00000000", 0f, null),
                null
        );
        binding.setPermanentId("row-permanent-id");
        Node root = new Node(
                Collections.<Wireframe>singletonList(binding),
                Collections.<Node>emptyList(),
                Collections.<Wireframe>emptyList()
        );

        List<Wireframe> flattened = new NodeFlattener().flattenNode(root);

        Assert.assertEquals(1, flattened.size());
        Assert.assertEquals("row-permanent-id", flattened.get(0).getPermanentId());
        Assert.assertEquals(0f,
                ((ShapeWireframe) flattened.get(0)).getShapeStyle().getOpacity().floatValue(),
                0f);
    }
}
