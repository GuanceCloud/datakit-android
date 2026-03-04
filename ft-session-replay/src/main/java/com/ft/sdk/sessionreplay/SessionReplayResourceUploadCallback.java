package com.ft.sdk.sessionreplay;

import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;
import com.ft.sdk.sessionreplay.internal.storage.UploadResult;

import java.util.List;

public interface SessionReplayResourceUploadCallback {
    UploadResult onCheckFilesExist(String appId, List<String> fileNames);
    UploadResult onUploadFiles(String appId, List<RawBatchEvent> files);
}
