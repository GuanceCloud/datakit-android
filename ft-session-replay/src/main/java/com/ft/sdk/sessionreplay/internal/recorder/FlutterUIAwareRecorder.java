package com.ft.sdk.sessionreplay.internal.recorder;

import android.app.Application;
import android.view.Window;

import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.MapperTypeWrapper;
import com.ft.sdk.sessionreplay.SessionReplayInternalCallback;
import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.internal.TouchPrivacyManager;
import com.ft.sdk.sessionreplay.internal.resources.ResourceDataStoreManager;
import com.ft.sdk.sessionreplay.internal.storage.RecordWriter;
import com.ft.sdk.sessionreplay.internal.storage.ResourcesWriter;
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;
import com.ft.sdk.sessionreplay.utils.RumContextProvider;
import com.ft.sdk.sessionreplay.utils.TimeProvider;

import java.util.List;

/**
 * 特殊的录制器，专为Flutter UI数据设计，不进行原生UI采集, added by zzq
 */
public class FlutterUIAwareRecorder extends SessionReplayRecorder {

    public FlutterUIAwareRecorder(
            Application appContext,
            ResourcesWriter resourcesWriter,
            RumContextProvider rumContextProvider,
            TextAndInputPrivacy textAndInputPrivacy,
            ImagePrivacy imagePrivacy,
            TouchPrivacyManager touchPrivacyManager,
            RecordWriter recordWriter,
            TimeProvider timeProvider,
            List<MapperTypeWrapper<?>> mappers,
            List<OptionSelectorDetector> customOptionSelectorDetectors,
            List<DrawableToColorMapper> customDrawableMappers,
            WindowInspector windowInspector,
            FeatureSdkCore sdkCore,
            ResourceDataStoreManager resourceDataStoreManager,
            SessionReplayInternalCallback internalCallback,
            boolean dynamicOptimizationEnabled,
            boolean isDelayInit) {
        
        super(appContext, resourcesWriter, rumContextProvider, textAndInputPrivacy,
              imagePrivacy, touchPrivacyManager, recordWriter, timeProvider,
              mappers, customOptionSelectorDetectors, customDrawableMappers,
              windowInspector, sdkCore, resourceDataStoreManager, internalCallback,
              dynamicOptimizationEnabled, isDelayInit);
    }
    
    @Override
    public void resumeRecorders() {
        // 不要调用父类方法，避免启动原生UI采集
        // 仅设置录制状态为活跃
        shouldRecord = true;
    }
    
    @Override
    public void stopRecorders() {
        // 仅设置录制状态为非活跃
        shouldRecord = false;
    }
    
    @Override
    public void onWindowsAdded(List<Window> windows) {
        // 不执行任何窗口拦截，因为我们使用Flutter提供的UI数据
    }
    
    @Override
    public void onWindowsRemoved(List<Window> windows) {
        // 不执行任何窗口释放操作
    }
}
