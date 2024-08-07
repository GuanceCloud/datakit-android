package com.ft.sdk.sessionreplay.model;

import com.google.gson.*;
import java.util.ArrayList;
import java.util.List;

public class MobileSegment {
    public final Application application;
    public final Session session;
    public final View view;
    public final long start;
    public final long end;
    public final long recordsCount;
    public final Long indexInView;
    public final Boolean hasFullSnapshot;
    public final Source source;
    public final List<MobileRecord> records;

    public MobileSegment(Application application, Session session, View view, long start, long end, long recordsCount, Long indexInView, Boolean hasFullSnapshot, Source source, List<MobileRecord> records) {
        this.application = application;
        this.session = session;
        this.view = view;
        this.start = start;
        this.end = end;
        this.recordsCount = recordsCount;
        this.indexInView = indexInView;
        this.hasFullSnapshot = hasFullSnapshot;
        this.source = source;
        this.records = records;
    }

    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.add("application", application.toJson());
        json.add("session", session.toJson());
        json.add("view", view.toJson());
        json.addProperty("start", start);
        json.addProperty("end", end);
        json.addProperty("records_count", recordsCount);
        if (indexInView != null) {
            json.addProperty("index_in_view", indexInView);
        }
        if (hasFullSnapshot != null) {
            json.addProperty("has_full_snapshot", hasFullSnapshot);
        }
        json.add("source", source.toJson());
        JsonArray recordsArray = new JsonArray();
        for (MobileRecord record : records) {
            recordsArray.add(record.toJson());
        }
        json.add("records", recordsArray);
        return json;
    }

    public static MobileSegment fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into type MobileSegment", e);
        }
    }

    public static MobileSegment fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        try {
            Application application = Application.fromJsonObject(jsonObject.getAsJsonObject("application"));
            Session session = Session.fromJsonObject(jsonObject.getAsJsonObject("session"));
            View view = View.fromJsonObject(jsonObject.getAsJsonObject("view"));
            long start = jsonObject.get("start").getAsLong();
            long end = jsonObject.get("end").getAsLong();
            long recordsCount = jsonObject.get("records_count").getAsLong();
            Long indexInView = jsonObject.has("index_in_view") ? jsonObject.get("index_in_view").getAsLong() : null;
            Boolean hasFullSnapshot = jsonObject.has("has_full_snapshot") ? jsonObject.get("has_full_snapshot").getAsBoolean() : null;
            Source source = Source.fromJson(jsonObject.get("source").getAsString());
            List<MobileRecord> records = new ArrayList<>();
            JsonArray jsonArray = jsonObject.getAsJsonArray("records");
            for (JsonElement element : jsonArray) {
                records.add(MobileRecord.fromJsonObject(element.getAsJsonObject()));
            }
            return new MobileSegment(application, session, view, start, end, recordsCount, indexInView, hasFullSnapshot, source, records);
        } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
            throw new JsonParseException("Unable to parse json into type MobileSegment", e);
        }
    }

    // Getters (optional, for accessing the fields)
}
