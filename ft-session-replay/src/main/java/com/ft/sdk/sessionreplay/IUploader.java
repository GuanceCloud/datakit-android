package com.ft.sdk.sessionreplay;

import com.ft.sdk.api.context.SessionReplayContext;
import com.ft.sdk.sessionreplay.internal.storage.RawBatchEvent;
import com.ft.sdk.sessionreplay.internal.storage.UploadResult;

import java.util.List;

public interface IUploader {
    /**
     * Upload session replay data
     *
     * @param context the session replay context
     * @param batchData the batch events to upload
     * @param byteArray the raw byte data
     * @return upload result
     * @throws Exception if upload fails
     */
    UploadResult upload(SessionReplayContext context, List<RawBatchEvent> batchData, byte[] byteArray) throws Exception;
}
