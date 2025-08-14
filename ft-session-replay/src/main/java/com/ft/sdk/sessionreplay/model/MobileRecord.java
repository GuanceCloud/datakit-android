package com.ft.sdk.sessionreplay.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public abstract class MobileRecord {
    public abstract JsonElement toJson();

    public static MobileRecord fromJson(String jsonString) throws JsonParseException {
        try {
            JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
            return fromJsonObject(jsonObject);
        } catch (IllegalStateException e) {
            throw new JsonParseException("Unable to parse json into one of type MobileRecord", e);
        }
    }

    public static MobileRecord fromJsonObject(JsonObject jsonObject) throws JsonParseException {
        List<Throwable> errors = new ArrayList<>();
        MobileRecord asMobileFullSnapshotRecord = null;
        MobileRecord asMobileIncrementalSnapshotRecord = null;
        MobileRecord asMetaRecord = null;
        MobileRecord asFocusRecord = null;
        MobileRecord asViewEndRecord = null;
        MobileRecord asVisualViewportRecord = null;

        try {
            asMobileFullSnapshotRecord = MobileFullSnapshotRecord.fromJsonObject(jsonObject);
        } catch (JsonParseException e) {
            errors.add(e);
        }
        try {
            asMobileIncrementalSnapshotRecord = MobileIncrementalSnapshotRecord.fromJsonObject(jsonObject);
        } catch (JsonParseException e) {
            errors.add(e);
        }
        try {
            asMetaRecord = MetaRecord.fromJsonObject(jsonObject);
        } catch (JsonParseException e) {
            errors.add(e);
        }
        try {
            asFocusRecord = FocusRecord.fromJsonObject(jsonObject);
        } catch (JsonParseException e) {
            errors.add(e);
        }
        try {
            asViewEndRecord = ViewEndRecord.fromJsonObject(jsonObject);
        } catch (JsonParseException e) {
            errors.add(e);
        }
        try {
            asVisualViewportRecord = VisualViewportRecord.fromJsonObject(jsonObject);
        } catch (JsonParseException e) {
            errors.add(e);
        }

        MobileRecord result = firstNonNull(asMobileFullSnapshotRecord, asMobileIncrementalSnapshotRecord, asMetaRecord, asFocusRecord, asViewEndRecord, asVisualViewportRecord);

        if (result == null) {
            StringBuilder message = new StringBuilder("Unable to parse json into one of type \nMobileRecord\n");
            for (Throwable error : errors) {
                message.append(error.getMessage()).append("\n");
            }
            throw new JsonParseException(message.toString());
        }

        return result;
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... items) {
        for (T item : items) {
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    public static final class MobileFullSnapshotRecord extends MobileRecord {
        public final long timestamp;
        public final Data data;
        public final long type = 10L;

        public MobileFullSnapshotRecord(long timestamp, Data data) {
            this.timestamp = timestamp;
            this.data = data;
        }

        @Override
        public JsonElement toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("timestamp", timestamp);
            json.addProperty("type", type);
            json.add("data", data.toJson());
            return json;
        }

        public static MobileFullSnapshotRecord fromJson(String jsonString) throws JsonParseException {
            try {
                JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
                return fromJsonObject(jsonObject);
            } catch (IllegalStateException e) {
                throw new JsonParseException("Unable to parse json into type MobileFullSnapshotRecord", e);
            }
        }

        public static MobileFullSnapshotRecord fromJsonObject(JsonObject jsonObject) throws JsonParseException {
            try {
                long timestamp = jsonObject.get("timestamp").getAsLong();
                Data data = Data.fromJsonObject(jsonObject.get("data").getAsJsonObject());
                return new MobileFullSnapshotRecord(timestamp, data);
            } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
                throw new JsonParseException("Unable to parse json into type MobileFullSnapshotRecord", e);
            }
        }
    }

    public static final class MobileIncrementalSnapshotRecord extends MobileRecord {
        public final long timestamp;
        public final MobileIncrementalData data;
        public final long type = 11L;

        public MobileIncrementalSnapshotRecord(long timestamp, MobileIncrementalData data) {
            this.timestamp = timestamp;
            this.data = data;
        }

        @Override
        public JsonElement toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("timestamp", timestamp);
            json.addProperty("type", type);
            json.add("data", data.toJson());
            return json;
        }

        public static MobileIncrementalSnapshotRecord fromJson(String jsonString) throws JsonParseException {
            try {
                JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
                return fromJsonObject(jsonObject);
            } catch (IllegalStateException e) {
                throw new JsonParseException("Unable to parse json into type MobileIncrementalSnapshotRecord", e);
            }
        }

        public static MobileIncrementalSnapshotRecord fromJsonObject(JsonObject jsonObject) throws JsonParseException {
            try {
                long timestamp = jsonObject.get("timestamp").getAsLong();
                MobileIncrementalData data = MobileIncrementalData.fromJsonObject(jsonObject.get("data").getAsJsonObject());
                return new MobileIncrementalSnapshotRecord(timestamp, data);
            } catch (IllegalStateException | NumberFormatException | NullPointerException e) {
                throw new JsonParseException("Unable to parse json into type MobileIncrementalSnapshotRecord", e);
            }
        }
    }

    // Define other nested classes as needed
}
