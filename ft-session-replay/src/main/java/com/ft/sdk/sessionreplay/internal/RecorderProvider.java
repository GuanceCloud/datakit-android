package com.ft.sdk.sessionreplay.internal;

import android.app.Application;

import com.ft.sdk.sessionreplay.internal.recorder.Recorder;
import com.ft.sdk.sessionreplay.internal.storage.RecordWriter;
import com.ft.sdk.sessionreplay.internal.storage.ResourcesWriter;
import com.ft.sdk.sessionreplay.internal.resources.ResourceDataStoreManager;

public interface RecorderProvider {
    Recorder provideSessionReplayRecorder(
            ResourceDataStoreManager resourceDataStoreManager,
            ResourcesWriter resourceWriter,
            RecordWriter recordWriter,
            Application application
    );
    /**
     * 指示是否使用Flutter UI数据而不是原生UI采集, added by zzq
     * @param useFlutterData 是否使用Flutter数据
     */
    void setUseFlutterUIData(boolean useFlutterData);
    
    /**
     * 检查是否使用Flutter UI数据, added by zzq 
     * @return 是否使用Flutter数据
     */
    boolean isUsingFlutterUIData();
}
