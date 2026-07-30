package com.ft.sdk.sessionreplay;

import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;
import com.ft.sdk.sessionreplay.internal.storage.UploadResult;

import java.util.List;
import java.util.Map;

/**
 * Callback used to plug custom resource file existence checks and uploads into
 * Session Replay.
 */
public interface SessionReplayResourceUploadCallback {
    /**
     * Checks whether resource files already exist on the backend.
     *
     * @param appId the application id associated with the resources
     * @param fileNames resource file names to check
     * @param globalContext RUM link context associated with the resources
     * @return upload result describing the check outcome
     */
    UploadResult onCheckFilesExist(String appId, List<String> fileNames,
                                   Map<String, Object> globalContext);

    /**
     * Uploads resource files required by Session Replay segments.
     *
     * @param appId the application id associated with the resources
     * @param files resource files to upload
     * @param globalContext RUM link context associated with the resources
     * @return upload result describing the upload outcome
     */
    UploadResult onUploadFiles(String appId, List<RawBatchEvent> files,
                               Map<String, Object> globalContext);
}
