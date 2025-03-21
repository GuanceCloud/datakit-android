package com.ft.sdk;


import android.content.Context;

import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.FTSessionReplayConfig;
import com.ft.sdk.sessionreplay.SessionReplayFeature;
import com.ft.sdk.sessionreplay.internal.TouchPrivacyManager;

public class SessionReplay {

    /**
     * Enables a SessionReplay feature based on the configuration provided.
     *
     * @param ftSessionReplayConfig Configuration to use for the feature.
     */
    static void enable(
            FTSessionReplayConfig ftSessionReplayConfig, Context context
    ) {
        FeatureSdkCore featureSdkCore = SessionReplayManager.get();
        featureSdkCore.init(context);
        SessionReplayFeature sessionReplayFeature = new SessionReplayFeature(
                featureSdkCore,
                ftSessionReplayConfig.getCustomEndpointUrl(),
                ftSessionReplayConfig.getPrivacy(),
                ftSessionReplayConfig.getTextAndInputPrivacy(),
                ftSessionReplayConfig.getTouchPrivacy(),
                new TouchPrivacyManager(ftSessionReplayConfig.getTouchPrivacy()),
                ftSessionReplayConfig.getImagePrivacy(),
                ftSessionReplayConfig.getCustomMappers(),
                ftSessionReplayConfig.getCustomOptionSelectorDetectors(),
                ftSessionReplayConfig.getCustomDrawableMapper(),
                ftSessionReplayConfig.getSampleRate(),
                ftSessionReplayConfig.isDelayInit(),
                false
        );

        featureSdkCore.registerFeature(sessionReplayFeature);
    }
}
