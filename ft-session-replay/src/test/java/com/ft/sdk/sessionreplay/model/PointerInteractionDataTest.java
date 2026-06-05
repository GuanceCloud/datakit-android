package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonObject;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PointerInteractionDataTest {

    @Test
    public void mobileIncrementalSnapshotRecord_shouldSerializePointerInteractionForHeatmap() {
        MobileRecord.MobileIncrementalSnapshotRecord record =
                new MobileRecord.MobileIncrementalSnapshotRecord(
                        1710000000000L,
                        new PointerInteractionData(
                                PointerEventType.DOWN,
                                PointerType.TOUCH,
                                7L,
                                120L,
                                380L
                        )
                );

        JsonObject recordJson = record.toJson().getAsJsonObject();
        JsonObject dataJson = recordJson.getAsJsonObject("data");

        assertEquals(11L, recordJson.get("type").getAsLong());
        assertEquals(1710000000000L, recordJson.get("timestamp").getAsLong());
        assertEquals(9L, dataJson.get("source").getAsLong());
        assertEquals("down", dataJson.get("pointerEventType").getAsString());
        assertEquals("touch", dataJson.get("pointerType").getAsString());
        assertEquals(7L, dataJson.get("pointerId").getAsLong());
        assertEquals(120L, dataJson.get("x").getAsLong());
        assertEquals(380L, dataJson.get("y").getAsLong());
    }
}
