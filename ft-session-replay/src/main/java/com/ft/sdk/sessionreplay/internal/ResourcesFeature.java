package com.ft.sdk.sessionreplay.internal;

import android.content.Context;

import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.feature.FeatureStorageConfiguration;
import com.ft.sdk.sessionreplay.internal.storage.NoOpResourcesWriter;
import com.ft.sdk.sessionreplay.internal.storage.ResourcesWriter;
import com.ft.sdk.sessionreplay.internal.storage.SessionReplayResourcesWriter;

import java.util.concurrent.atomic.AtomicBoolean;

public class ResourcesFeature implements StorageBackedFeature {

    private final FeatureSdkCore sdkCore;
    private ResourcesWriter dataWriter = new NoOpResourcesWriter();
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public String getName() {
        return SESSION_REPLAY_RESOURCES_FEATURE_NAME;
    }


    @Override
    public void onInitialize(Context appContext) {
        dataWriter = new SessionReplayResourcesWriter(sdkCore);
        initialized.set(true);
    }

    @Override
    public FeatureStorageConfiguration getStorageConfiguration() {
        return STORAGE_CONFIGURATION;
    }

    @Override
    public void onStop() {
        dataWriter = new NoOpResourcesWriter();
        initialized.set(false);
    }

    private static final FeatureStorageConfiguration STORAGE_CONFIGURATION =
            FeatureStorageConfiguration.DEFAULT.copyWith(10 * 1024 * 1024, 0, 10 * 1024 * 1024, 0);

    public ResourcesFeature(FeatureSdkCore sdkCore, String customEndpointUrl) {
        this.sdkCore = sdkCore;
    }

    public ResourcesWriter getDataWriter() {
        return dataWriter;
    }
}
