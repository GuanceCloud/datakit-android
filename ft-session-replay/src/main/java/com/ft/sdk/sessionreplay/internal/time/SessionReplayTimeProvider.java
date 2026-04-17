package com.ft.sdk.sessionreplay.internal.time;

import com.ft.sdk.feature.Feature;
import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.sessionreplay.Supplier;
import com.ft.sdk.sessionreplay.utils.TimeProvider;


public class SessionReplayTimeProvider implements TimeProvider {

    private final FeatureSdkCore sdkCore;
    private final Supplier<Long> currentTimeProvider;

    public SessionReplayTimeProvider(FeatureSdkCore sdkCore) {
        this(sdkCore, System::currentTimeMillis);
    }

    public SessionReplayTimeProvider(FeatureSdkCore sdkCore, Supplier<Long> currentTimeProvider) {
        this.sdkCore = sdkCore;
        this.currentTimeProvider = currentTimeProvider;
    }

    @Override
    public long getDeviceTimestamp() {
        return currentTimeProvider.get() + resolveRumViewTimestampOffset();
    }

    private long resolveRumViewTimestampOffset() {
        Object timestampOffset = sdkCore.getFeatureContext(Feature.RUM_FEATURE_NAME)
                .get(SessionReplayTimeProvider.RUM_VIEW_TIMESTAMP_OFFSET);
        if (timestampOffset instanceof Long) {
            return (Long) timestampOffset;
        } else {
            return 0L;
        }
    }

    public static final String RUM_VIEW_TIMESTAMP_OFFSET = "view_timestamp_offset";
}
