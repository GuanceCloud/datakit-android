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
    public static final String VERSION_LARGER_THAN = "0.1.3-alpha07";

    /**
     * Enables a SessionReplay feature based on the configuration provided.
     *
     * @param ftSessionReplayConfig Configuration to use for the feature.
     */
    static void enable(
            FTSessionReplayConfig ftSessionReplayConfig, Context context
    ) {
        FeatureSdkCore featureSdkCore = SessionReplayManager.get();
        if (!VersionUtils.firstVerGreaterEqual(BuildConfig.VERSION_NAME, VERSION_LARGER_THAN)) {
            featureSdkCore.getInternalLogger().e(TAG, "need install more than ft-session-replay:"
                    + VERSION_LARGER_THAN);
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
