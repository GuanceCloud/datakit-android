package com.ft.sdk.sessionreplay.internal.net;

import android.util.Pair;

import com.ft.sdk.sessionreplay.internal.processor.EnrichedRecord;
import com.ft.sdk.sessionreplay.model.Application;
import com.ft.sdk.sessionreplay.model.MobileSegment;
import com.ft.sdk.sessionreplay.model.Session;
import com.ft.sdk.sessionreplay.model.Source;
import com.ft.sdk.sessionreplay.model.View;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.ft.sdk.sessionreplay.utils.SessionReplayRumContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchesToSegmentsMapper {

    private static final String TAG = "BatchesToSegmentsMapper";

    private final InternalLogger internalLogger;

    public BatchesToSegmentsMapper(InternalLogger internalLogger) {
        this.internalLogger = internalLogger;
    }

    public List<Pair<MobileSegment, JsonObject>> map(List<byte[]> batchData) {
        return groupBatchDataIntoSegments(batchData);
    }

    // region Internal

    private List<Pair<MobileSegment, JsonObject>> groupBatchDataIntoSegments(List<byte[]> batchData) {
        List<Pair<SessionReplayRumContext, JsonArray>> contextRecordsList = new ArrayList<>();

        for (byte[] data : batchData) {
            JsonObject jsonObject = parseToJsonObject(data);
            if (jsonObject != null) {
                Pair<SessionReplayRumContext, JsonArray> contextRecords = extractRecordsAndContext(jsonObject);
                if (contextRecords != null) {
                    contextRecordsList.add(contextRecords);
                }
            }
        }

        Map<SessionReplayRumContext, JsonArray> groupedRecords = new HashMap<>();
        for (Pair<SessionReplayRumContext, JsonArray> pair : contextRecordsList) {
            SessionReplayRumContext context = pair.first;
            JsonArray records = pair.second;
            if (!groupedRecords.containsKey(context)) {
                groupedRecords.put(context, new JsonArray());
            }
            for (int i = 0; i < records.size(); i++) {
                groupedRecords.get(context).add(records.get(i));
            }
        }

        List<Pair<MobileSegment, JsonObject>> segments = new ArrayList<>();
        for (Map.Entry<SessionReplayRumContext, JsonArray> entry : groupedRecords.entrySet()) {
            Pair<MobileSegment, JsonObject> segment = mapToSegment(entry.getKey(), entry.getValue());
            if (segment != null) {
                segments.add(segment);
            }
        }
        return segments;
    }

    private JsonObject parseToJsonObject(byte[] data) {
        try {
            return new JsonParser().parse(new String(data)).getAsJsonObject();
        } catch (JsonParseException | IllegalStateException e) {
            internalLogger.e(TAG, UNABLE_TO_DESERIALIZE_ENRICHED_RECORD_ERROR_MESSAGE);
            return null;
        }
    }

    private Pair<SessionReplayRumContext, JsonArray> extractRecordsAndContext(JsonObject jsonObject) {
        JsonArray records = jsonObject.getAsJsonArray(RECORDS_KEY);
        SessionReplayRumContext rumContext = extractRumContext(jsonObject);

        if (records == null || rumContext == null || records.size() == 0) {
            return null;
        }

        return new Pair<>(rumContext, records);
    }

    private Pair<MobileSegment, JsonObject> mapToSegment(SessionReplayRumContext rumContext, JsonArray records) {
        JsonArray orderedRecords = orderRecordsByTimestamp(records);

        if (orderedRecords.size() == 0) {
            return null;
        }

        Long startTimestamp = extractTimestamp(orderedRecords.get(0).getAsJsonObject());
        Long stopTimestamp = extractTimestamp(orderedRecords.get(orderedRecords.size() - 1).getAsJsonObject());

        if (startTimestamp == null || stopTimestamp == null) {
            return null;
        }

        boolean hasFullSnapshotRecord = hasFullSnapshotRecord(orderedRecords);
        MobileSegment segment = new MobileSegment(
                new Application(rumContext.getApplicationId()),
                new Session(rumContext.getSessionId()),
                new View(rumContext.getViewId()),
                startTimestamp,
                stopTimestamp,
                (long) orderedRecords.size(),
                null,
                hasFullSnapshotRecord,
                Source.ANDROID,
                Collections.emptyList()
        );
        JsonObject segmentAsJsonObject = segment.toJson().getAsJsonObject();
        if (segmentAsJsonObject == null) {
            return null;
        }
        segmentAsJsonObject.add(RECORDS_KEY, orderedRecords);
        return new Pair<>(segment, segmentAsJsonObject);
    }

    private JsonArray orderRecordsByTimestamp(JsonArray records) {
        List<Pair<JsonObject, Long>> recordList = new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            JsonObject record = records.get(i).getAsJsonObject();
            Long timestamp = extractTimestamp(record);
            if (timestamp != null) {
                recordList.add(new Pair<>(record, timestamp));
            }
        }

        Collections.sort(recordList, new Comparator<Pair<JsonObject, Long>>() {
            @Override
            public int compare(Pair<JsonObject, Long> o1, Pair<JsonObject, Long> o2) {
                return o1.second.compareTo(o2.second);
            }
        });

        JsonArray orderedRecords = new JsonArray();
        for (Pair<JsonObject, Long> pair : recordList) {
            orderedRecords.add(pair.first);
        }

        return orderedRecords;
    }

    private Long extractTimestamp(JsonObject jsonObject) {
        return jsonObject.has(TIMESTAMP_KEY) ? jsonObject.get(TIMESTAMP_KEY).getAsLong() : null;
    }

    private boolean hasFullSnapshotRecord(JsonArray records) {
        for (int i = 0; i < records.size(); i++) {
            JsonObject record = records.get(i).getAsJsonObject();
            Long typeAsLong = record.has(RECORD_TYPE_KEY) ? record.get(RECORD_TYPE_KEY).getAsLong() : null;
            if (typeAsLong != null && (typeAsLong.equals(FULL_SNAPSHOT_RECORD_TYPE_MOBILE) || typeAsLong.equals(FULL_SNAPSHOT_RECORD_TYPE_BROWSER))) {
                return true;
            }
        }
        return false;
    }

    private SessionReplayRumContext extractRumContext(JsonObject jsonObject) {
        String applicationId = jsonObject.has(EnrichedRecord.APPLICATION_ID_KEY) ? jsonObject.get(EnrichedRecord.APPLICATION_ID_KEY).getAsString() : null;
        String sessionId = jsonObject.has(EnrichedRecord.SESSION_ID_KEY) ? jsonObject.get(EnrichedRecord.SESSION_ID_KEY).getAsString() : null;
        String viewId = jsonObject.has(EnrichedRecord.VIEW_ID_KEY) ? jsonObject.get(EnrichedRecord.VIEW_ID_KEY).getAsString() : null;

        if (applicationId == null || sessionId == null || viewId == null) {
            //fixme only once
            internalLogger.e(TAG, ILLEGAL_STATE_ENRICHED_RECORD_ERROR_MESSAGE);
            return null;
        }

        return new SessionReplayRumContext(applicationId, sessionId, viewId);
    }

    // endregion

    private static final long FULL_SNAPSHOT_RECORD_TYPE_MOBILE = 10L;
    private static final long FULL_SNAPSHOT_RECORD_TYPE_BROWSER = 2L;

    private static final String RECORDS_KEY = "records";
    private static final String RECORD_TYPE_KEY = "type";
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String UNABLE_TO_DESERIALIZE_ENRICHED_RECORD_ERROR_MESSAGE =
            "SR BatchesToSegmentMapper: unable to deserialize EnrichedRecord";
    private static final String ILLEGAL_STATE_ENRICHED_RECORD_ERROR_MESSAGE =
            "SR BatchesToSegmentMapper: Enriched record was missing the context information";
}
