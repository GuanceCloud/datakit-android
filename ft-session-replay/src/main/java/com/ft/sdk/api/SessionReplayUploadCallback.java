package com.ft.sdk.api;

import com.ft.sdk.sessionreplay.internal.storage.UploadResult;

public interface SessionReplayUploadCallback {

    UploadResult onRequest(SessionReplayFormData provider);
}
