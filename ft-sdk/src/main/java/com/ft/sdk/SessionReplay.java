package com.ft.sdk;


import android.content.Context;

import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.FTSessionReplayConfig;
import com.ft.sdk.sessionreplay.SessionReplayFeature;

public class SessionReplay {

    /**
     * Enables a SessionReplay feature based on the configuration provided.
     *
     * @param FTSessionReplayConfig Configuration to use for the feature.
     */
     static void enable(
             FTSessionReplayConfig FTSessionReplayConfig, Context context
    ) {
        FeatureSdkCore featureSdkCore = SessionReplayManager.get();
        featureSdkCore.init(context);
        SessionReplayFeature sessionReplayFeature = new SessionReplayFeature(
                featureSdkCore,
                FTSessionReplayConfig.getCustomEndpointUrl(),
                FTSessionReplayConfig.getPrivacy(),
                FTSessionReplayConfig.getCustomMappers(),
                FTSessionReplayConfig.getCustomOptionSelectorDetectors(),
                FTSessionReplayConfig.getSampleRate()
        );

        featureSdkCore.registerFeature(sessionReplayFeature);
    }
}
