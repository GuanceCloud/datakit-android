package com.ft.sdk.sessionreplay.internal;

import android.app.Application;

import com.ft.sdk.sessionreplay.internal.recorder.Recorder;
import com.ft.sdk.sessionreplay.internal.storage.RecordWriter;
import com.ft.sdk.sessionreplay.internal.storage.ResourcesWriter;
import com.ft.sdk.sessionreplay.resources.ResourceDataStoreManager;

public interface RecorderProvider {
    Recorder provideSessionReplayRecorder(
            ResourceDataStoreManager resourceDataStoreManager,
            ResourcesWriter resourceWriter,
            RecordWriter recordWriter,
            Application application
    );
}
