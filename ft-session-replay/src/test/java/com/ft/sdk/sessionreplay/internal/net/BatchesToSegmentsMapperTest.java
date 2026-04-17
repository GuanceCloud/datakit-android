package com.ft.sdk.sessionreplay.internal.net;

import android.util.Pair;

import com.ft.sdk.sessionreplay.model.MobileSegment;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BatchesToSegmentsMapperTest {

    @Test
    public void map_shouldSkipInvalidPayloadsAndRecordsWithoutContext() {
        BatchesToSegmentsMapper mapper = new BatchesToSegmentsMapper(new TestLogger());

        List<Pair<MobileSegment, JsonObject>> result = mapper.map(Arrays.asList(
                "not-json".getBytes(StandardCharsets.UTF_8),
                createBatchJson(null, "session-1", "view-1", records(record(10L, 1L)), null)
                        .getBytes(StandardCharsets.UTF_8)
        ));

        assertTrue(result.isEmpty());
    }

    @Test
    public void map_shouldReturnEmptyWhenRecordsDoNotContainTimestamp() {
        BatchesToSegmentsMapper mapper = new BatchesToSegmentsMapper(new TestLogger());

        // Records without timestamps are dropped during ordering, which should collapse the
        // segment entirely instead of producing a partially valid payload.
        String batchOne = createBatchJson(
                "app-1",
                "session-1",
                "view-1",
                records(recordWithoutTimestamp(8L), recordWithoutTimestamp(2L)),
                globalContext()
        );

        List<Pair<MobileSegment, JsonObject>> result = mapper.map(Arrays.asList(
                batchOne.getBytes(StandardCharsets.UTF_8)
        ));

        assertTrue(result.isEmpty());
    }

    @Test
    public void map_shouldReturnEmptyWhenRecordsArrayIsEmpty() {
        BatchesToSegmentsMapper mapper = new BatchesToSegmentsMapper(new TestLogger());

        List<Pair<MobileSegment, JsonObject>> result = mapper.map(Arrays.asList(
                createBatchJson(
                        "app-1",
                        "session-1",
                        "view-1",
                        records(),
                        null
                ).getBytes(StandardCharsets.UTF_8)
        ));

        assertTrue(result.isEmpty());
    }

    private static JsonObject record(long type, long timestamp) {
        JsonObject record = new JsonObject();
        record.addProperty("type", type);
        record.addProperty("timestamp", timestamp);
        return record;
    }

    private static JsonObject recordWithoutTimestamp(long type) {
        JsonObject record = new JsonObject();
        record.addProperty("type", type);
        return record;
    }

    private static JsonArray records(JsonObject... records) {
        JsonArray jsonArray = new JsonArray();
        for (JsonObject record : records) {
            jsonArray.add(record);
        }
        return jsonArray;
    }

    private static JsonObject globalContext() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("env", "release");
        jsonObject.addProperty("retry_count", 3);
        jsonObject.addProperty("enabled", true);
        // Non-primitive entries are intentionally ignored by extractRumContext.
        jsonObject.add("nested", new JsonObject());
        jsonObject.add("tags", new JsonArray());
        return jsonObject;
    }

    private static String createBatchJson(
            String applicationId,
            String sessionId,
            String viewId,
            JsonArray records,
            JsonObject globalContext
    ) {
        JsonObject root = new JsonObject();
        if (applicationId != null) {
            root.addProperty("application_id", applicationId);
        }
        if (sessionId != null) {
            root.addProperty("session_id", sessionId);
        }
        if (viewId != null) {
            root.addProperty("view_id", viewId);
        }
        root.add("records", records);
        if (globalContext != null) {
            root.add("globalContext", globalContext);
        }
        return root.toString();
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
