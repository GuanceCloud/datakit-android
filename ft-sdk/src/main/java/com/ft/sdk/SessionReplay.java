package com.ft.sdk;


import android.content.Context;

import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.VersionUtils;
import com.ft.sdk.sessionreplay.BuildConfig;
import com.ft.sdk.sessionreplay.FTSessionReplayConfig;
import com.ft.sdk.sessionreplay.SessionReplayFeature;

public class SessionReplay {
    private static final String TAG = "SessionReplay";

    /**
     * Enables a SessionReplay feature based on the configuration provided.
     *
     * @param ftSessionReplayConfig Configuration to use for the feature.
     */
    public static void enable(
            FTSessionReplayConfig ftSessionReplayConfig, Context context
    ) {
        FeatureSdkCore featureSdkCore = SessionReplayManager.get();
        if (!VersionUtils.firstVerGreaterEqual(BuildConfig.VERSION_NAME, "0.1.2-alpha01")) {
            featureSdkCore.getInternalLogger().e(TAG, "need install more than ft-session-replay:0.1.2-alpha01");
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
