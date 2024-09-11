package com.ft.sdk.sessionreplay;

import android.os.Bundle;
import android.util.Pair;

import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.sessionreplay.internal.excepiton.InvalidPayloadFormatException;
import com.ft.sdk.sessionreplay.internal.net.BatchesToSegmentsMapper;
import com.ft.sdk.sessionreplay.internal.net.BytesCompressor;
import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;
import com.ft.sdk.sessionreplay.internal.storage.UploadResult;
import com.ft.sdk.sessionreplay.model.MobileSegment;
import com.ft.sdk.sessionreplay.utils.InternalLogger;
import com.google.gson.JsonObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 负责 session replay 上传逻辑
 */
public class SessionReplayUploader {

    private static final String TAG = "SessionReplayUploader";

    private static final String KEY_START = "start";
    private static final String KEY_END = "end";
    private static final String KEY_RECORDS_COUNT = "records_count";
    private static final String KEY_SEGMENT = "segment";
    private static final String KEY_SOURCE = "source";
    private static final String KEY_RAW_SEGMENT_SIZE = "raw_segment_size";
    private static final String KEY_INDEX_IN_VIEW = "index_in_view";
    private static final String HAS_FULL_SNAPSHOT = "has_full_snapshot";
    private static final String KEY_ENV = "env";
    private static final String KEY_SDK_VERSION = "sdk_version";
    private static final String KEY_APP_ID = "app_id";
    private static final String KEY_SESSION_ID = "session_id";
    private static final String KEY_VIEW_ID = "view_id";

    private final String requestUrl;
    private final BatchesToSegmentsMapper batchToSegmentsMapper;
    private final BytesCompressor compressor;
    private final InternalLogger internalLogger;


    public SessionReplayUploader(SessionReplayContext context, BatchesToSegmentsMapper mapper, InternalLogger internalLogger) {
        this.requestUrl = context.getRequestUrl();
        this.batchToSegmentsMapper = mapper;
        this.compressor = new BytesCompressor();
        this.internalLogger = internalLogger;
    }


    /**
     * 上传 Session Replay 数据
     * <p>
     * 上传过程中发生错误数据，会缓存
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
            internalLogger.i(TAG, "segment.hasFullSnapshot:" + segment.first.hasFullSnapshot);
        }

        byte[] segmentAsByteArray = jsonString.toString().getBytes();
        byte[] compressedData = compressor.compressBytes(segmentAsByteArray);
        MsMultiPartFormData data = new MsMultiPartFormData(this.requestUrl, "UTF-8");
        data.addFilePart(KEY_SEGMENT, new ByteArrayInputStream(compressedData), viewId);
        data.addFormField(KEY_START, start + "");
        data.addFormField(KEY_END, end + "");
        data.addFormField(KEY_RECORDS_COUNT, recordsCount + "");
//
        data.addFormField(KEY_INDEX_IN_VIEW, 0 + "");//fixme 目前在移动端无实际作用
        data.addFormField(HAS_FULL_SNAPSHOT, hasFullSnapshot + "");
        data.addFormField(KEY_SOURCE, "android");
        data.addFormField(KEY_RAW_SEGMENT_SIZE, segmentAsByteArray.length + "");

        data.addFormField(KEY_ENV, context.getEnv());
        data.addFormField(KEY_SDK_VERSION, context.getSdkVersion());
        data.addFormField(KEY_APP_ID, context.getAppId());
        data.addFormField(KEY_SESSION_ID, sessionId);
        data.addFormField(KEY_VIEW_ID, viewId);

        Bundle b = data.finish();
        UploadResult result = new UploadResult(b.getInt("code"), b.getString("response"));
        if (result.isSuccess()) {
            internalLogger.d(TAG, "Session Upload Success. view_id:" + viewId
                    + ",count:" + recordsCount + ",hasFullSnapshot:" + hasFullSnapshot);
        } else {
            internalLogger.e(TAG, "Session Upload Failed. view_id:" + viewId
                    + ",count:" + recordsCount + ",code:" + result.getCode() + ",response:" + result.getResponse());
        }
        return result;

    }


}
