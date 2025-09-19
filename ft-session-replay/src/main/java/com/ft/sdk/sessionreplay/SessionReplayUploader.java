package com.ft.sdk.sessionreplay;

import android.util.Pair;

import com.ft.sdk.api.SessionReplayFormData;
import com.ft.sdk.api.SessionReplayUploadCallback;
import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.sessionreplay.internal.excepiton.InvalidPayloadFormatException;
import com.ft.sdk.sessionreplay.internal.net.BatchesToSegmentsMapper;
import com.ft.sdk.sessionreplay.internal.net.BytesCompressor;
import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;
import com.ft.sdk.sessionreplay.internal.storage.UploadResult;
import com.ft.sdk.sessionreplay.model.MobileSegment;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Responsible for session replay upload logic
 */
public class SessionReplayUploader {

    private static final String TAG = "SessionReplayUploader";

    private static final String KEY_START = "start";
    private static final String KEY_END = "end";
    public static final String KEY_RECORDS_COUNT = "records_count";
    private static final String KEY_SEGMENT = "segment";
    private static final String KEY_SOURCE = "source";
    private static final String KEY_RAW_SEGMENT_SIZE = "raw_segment_size";
    private static final String KEY_INDEX_IN_VIEW = "index_in_view";
    private static final String HAS_FULL_SNAPSHOT = "has_full_snapshot";
    private static final String KEY_ENV = "env";
    private static final String KEY_SDK_VERSION = "sdk_version";
    private static final String KEY_SDK_NAME = "sdk_name";
    private static final String KEY_VERSION = "version";
    private static final String KEY_APP_ID = "app_id";
    private static final String KEY_SESSION_ID = "session_id";
    private static final String KEY_VIEW_ID = "view_id";

    private final BatchesToSegmentsMapper batchToSegmentsMapper;
    private final BytesCompressor compressor;
    private final InternalLogger internalLogger;
    private final SessionReplayUploadCallback callback;


    public SessionReplayUploader(BatchesToSegmentsMapper mapper,
                                 InternalLogger internalLogger,
                                 SessionReplayUploadCallback uploadCallback) {
        this.batchToSegmentsMapper = mapper;
        this.compressor = new BytesCompressor();
        this.internalLogger = internalLogger;
        this.callback = uploadCallback;
    }


    /**
     * Upload Session Replay data
     * <p>
     * If an error occurs during upload, the data will be cached
     *
     * @param context
     */
    public UploadResult upload(SessionReplayContext context, List<RawBatchEvent> batchData, byte[] byteArray) throws Exception {
        List<byte[]> serializedSegments = new ArrayList<>();
        for (RawBatchEvent event : batchData) {
            serializedSegments.add(event.getData());
        }

        List<Pair<MobileSegment, JsonObject>> serializedSegmentPair = batchToSegmentsMapper.map(serializedSegments);
        if (serializedSegmentPair == null || serializedSegmentPair.isEmpty()) {
            throw new InvalidPayloadFormatException(
                    "The payload format was broken and an upload request could not be created"
            );
        }
        String viewId = "";
        String sessionId = "";
        long start = -1;
        long end = -1;
        long recordsCount = 0;
        boolean hasFullSnapshot = false;
        StringBuilder jsonString = new StringBuilder();
        for (int index = 0; index < serializedSegmentPair.size(); index++) {
            Pair<MobileSegment, JsonObject> segment = serializedSegmentPair.get(index);
            jsonString.append(segment.second.toString()).append("\n");
            if (start == -1) {
                start = segment.first.start;
            }
            end = Math.max(end, segment.first.end);
            recordsCount += segment.first.recordsCount;
            sessionId = segment.first.session.id;
            viewId = segment.first.view.getId();

            if (segment.first.hasFullSnapshot) {
                hasFullSnapshot = true;
            }
        }

        byte[] segmentAsByteArray = jsonString.toString().getBytes();
        byte[] compressedData = compressor.compressBytes(segmentAsByteArray);

        HashMap<String, String> fieldMap = new HashMap<>();
        fieldMap.put(KEY_START, start + "");
        fieldMap.put(KEY_END, end + "");
        fieldMap.put(KEY_RECORDS_COUNT, recordsCount + "");
        fieldMap.put(KEY_INDEX_IN_VIEW, 0 + "");//fixme Currently has no practical effect on mobile
        fieldMap.put(HAS_FULL_SNAPSHOT, hasFullSnapshot + "");
        fieldMap.put(KEY_SOURCE, "android");
        fieldMap.put(KEY_RAW_SEGMENT_SIZE, segmentAsByteArray.length + "");

        fieldMap.put(KEY_ENV, context.getEnv());
        fieldMap.put(KEY_SDK_VERSION, context.getSdkVersion());
        fieldMap.put(KEY_SDK_NAME, "df_android_rum_sdk");
        fieldMap.put(KEY_APP_ID, context.getAppId());
        fieldMap.put(KEY_SESSION_ID, sessionId);
        fieldMap.put(KEY_VIEW_ID, viewId);
        fieldMap.put(KEY_VERSION, context.getAppVersion());
        HashMap<String, Pair<String, byte[]>> fileFileMap = new HashMap<>();
        fileFileMap.put(KEY_SEGMENT, new Pair<>(viewId, compressedData));

        SessionReplayFormData formData = new SessionReplayFormData(fieldMap, fileFileMap);
        UploadResult result = callback.onRequest(formData);

        if (result.isSuccess()) {
            internalLogger.d(TAG, "Session Upload Success. " + result.getPkgId() + ",view_id:" + viewId
                    + ",count:" + recordsCount + ",hasFullSnapshot:" + hasFullSnapshot);
        } else {
            internalLogger.e(TAG, "Session Upload Failed." + result.getPkgId() + ",view_id:" + viewId
                    + ",count:" + recordsCount + ",code:" + result.getCode() + ",response:" + result.getResponse());
        }
        return result;

    }


}
