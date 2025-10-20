package com.ft.sdk.sessionreplay.internal.processor;

import com.ft.sdk.sessionreplay.model.MobileRecord;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class EnrichedRecord {
    private final String applicationId;
    private final String sessionId;
    private final String viewId;
    private final ConcurrentHashMap<String, Object> globalContext;
    private final List<MobileRecord> records;
    private final boolean isWebRecord;

    public EnrichedRecord(String applicationId, String sessionId, String viewId, List<MobileRecord> records) {

        this(applicationId, sessionId, viewId, false, records, new ConcurrentHashMap<>());

    }

    public EnrichedRecord(String applicationId, String sessionId, String viewId, boolean isWebRecord,
                          List<MobileRecord> records, ConcurrentHashMap<String, Object> globalContext) {
        this.applicationId = applicationId;
        this.sessionId = sessionId;
        this.viewId = viewId;
        this.records = records;
        this.isWebRecord = isWebRecord;
        this.globalContext = globalContext;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getViewId() {
        return viewId;
    }

    public boolean isWebRecord() {
        return isWebRecord;
    }

    public List<MobileRecord> getRecords() {
        return records;
    }

    /**
     * Returns the JSON string equivalent of this object.
     */
    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty(APPLICATION_ID_KEY, applicationId);
        json.addProperty(SESSION_ID_KEY, sessionId);
        json.addProperty(VIEW_ID_KEY, viewId);
        if (!globalContext.isEmpty()) {
            JsonObject jsonGlobalContext = new JsonObject();
            for (String key : globalContext.keySet()) {
                Object value = globalContext.get(key);
                JsonPrimitive jsonPrimitive;
                if (value instanceof String) {
                    jsonPrimitive = new JsonPrimitive((String) value);
                } else if (value instanceof Number) {
                    jsonPrimitive = new JsonPrimitive((Number) value);
                } else if (value instanceof Boolean) {
                    jsonPrimitive = new JsonPrimitive((Boolean) value);
                } else if (value instanceof Character) {
                    jsonPrimitive = new JsonPrimitive((Character) value);
                } else {
                    // Convert other types to String
                    jsonPrimitive = new JsonPrimitive(value + "");
                }
                jsonGlobalContext.add(key, jsonPrimitive);
            }
            json.add("globalContext", jsonGlobalContext);
        }
        JsonArray recordsJsonArray = new JsonArray();
        for (MobileRecord record : records) {
            recordsJsonArray.add(record.toJson());
        }
        json.add(RECORDS_KEY, recordsJsonArray);
        return json.toString();
    }

    public static final String APPLICATION_ID_KEY = "application_id";
    public static final String SESSION_ID_KEY = "session_id";
    public static final String VIEW_ID_KEY = "view_id";
    public static final String RECORDS_KEY = "records";

}
