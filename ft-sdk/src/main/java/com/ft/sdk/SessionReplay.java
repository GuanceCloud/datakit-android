package com.ft.sdk;


import android.content.Context;

import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.sessionreplay.FTSessionReplayConfig;
import com.ft.sdk.sessionreplay.SessionReplayFeature;

public class SessionReplay {
    private static final String TAG = "SessionReplay";

    /**
     * Enables a SessionReplay feature based on the configuration provided.
     *
     * @param ftSessionReplayConfig Configuration to use for the feature.
     */
    static void enable(
            FTSessionReplayConfig ftSessionReplayConfig, Context context
    ) {
        FeatureSdkCore featureSdkCore = SessionReplayManager.get();
        
        // Double-check version compatibility when SessionReplay.enable() is called
        if (SDKVersionValidator.validateSDKVersions()) {
            featureSdkCore.getInternalLogger().e(TAG, "SDK version mismatch detected," +
                    " SessionReplay enable failed");
            return;
        }

        LogUtils.d(TAG, "init SR:" + ftSessionReplayConfig);
        featureSdkCore.init(context);
        SessionReplayFeature sessionReplayFeature = new SessionReplayFeature(
                featureSdkCore,
                ftSessionReplayConfig
        );

        featureSdkCore.registerFeature(sessionReplayFeature);
    }
}
