package com.ft.sdk.sessionreplay;


import android.content.Context;

import com.ft.sdk.SessionReplayManager;
import com.ft.sdk.api.SdkCore;
import com.ft.sdk.feature.FeatureSdkCore;

public class SessionReplay {

    /**
     * Enables a SessionReplay feature based on the configuration provided.
     *
     * @param sessionReplayConfiguration Configuration to use for the feature.
     */
    public static void enable(
            SessionReplayConfiguration sessionReplayConfiguration, Context context
    ) {
        FeatureSdkCore featureSdkCore = SessionReplayManager.get();
        featureSdkCore.init(context);
        SessionReplayFeature sessionReplayFeature = new SessionReplayFeature(
                featureSdkCore,
                sessionReplayConfiguration.getCustomEndpointUrl(),
                sessionReplayConfiguration.getPrivacy(),
                sessionReplayConfiguration.getCustomMappers(),
                sessionReplayConfiguration.getCustomOptionSelectorDetectors(),
                sessionReplayConfiguration.getSampleRate()
        );

        featureSdkCore.registerFeature(sessionReplayFeature);
    }
}
