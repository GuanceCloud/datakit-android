package com.ft.sdk.sessionreplay;

import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;
import com.ft.sdk.sessionreplay.internal.storage.UploadResult;

import java.util.List;
import java.util.Map;

public interface SessionReplayResourceUploadCallback {
    UploadResult onCheckFilesExist(String appId, List<String> fileNames, Map<String, Object> globalContext);
    UploadResult onUploadFiles(String appId, List<RawBatchEvent> files, Map<String, Object> globalContext);
}
