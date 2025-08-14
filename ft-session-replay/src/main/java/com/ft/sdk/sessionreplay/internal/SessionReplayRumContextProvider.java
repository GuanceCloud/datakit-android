package com.ft.sdk.sessionreplay.internal;

import static com.ft.sdk.sessionreplay.utils.SessionReplayRumContext.NULL_UUID;

import com.ft.sdk.feature.Feature;
import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.utils.RumContextProvider;
import com.ft.sdk.sessionreplay.utils.SessionReplayRumContext;

import java.util.Map;
import java.util.UUID;

public class SessionReplayRumContextProvider implements RumContextProvider {

    private final FeatureSdkCore sdkCore;

    public SessionReplayRumContextProvider(FeatureSdkCore sdkCore) {
        this.sdkCore = sdkCore;
    }

    @Override
    public SessionReplayRumContext getRumContext() {
        Map<String, Object> rumContext = sdkCore.getFeatureContext(Feature.RUM_FEATURE_NAME);
        return new SessionReplayRumContext(
                rumContext.containsKey("application_id") ? (String) rumContext.get("application_id") : NULL_UUID,
                rumContext.containsKey("session_id") ? (String) rumContext.get("session_id") : NULL_UUID,
                rumContext.containsKey("view_id") ? (String) rumContext.get("view_id") : NULL_UUID
        );
    }

}
