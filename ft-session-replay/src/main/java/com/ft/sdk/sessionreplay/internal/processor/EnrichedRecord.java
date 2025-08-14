package com.ft.sdk.sessionreplay.internal.processor;

import com.ft.sdk.sessionreplay.model.MobileRecord;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class EnrichedRecord {
    private final String applicationId;
    private final String sessionId;
    private final String viewId;
    private final List<MobileRecord> records;

    public EnrichedRecord(String applicationId, String sessionId, String viewId, List<MobileRecord> records) {
        this.applicationId = applicationId;
        this.sessionId = sessionId;
        this.viewId = viewId;
        this.records = records;
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
