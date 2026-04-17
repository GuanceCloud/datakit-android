package com.ft.sdk.sessionreplay.internal;

import com.ft.sdk.feature.Feature;
import com.ft.sdk.feature.FeatureStorageConfiguration;

public interface StorageBackedFeature extends Feature {


    /**
     * Provides storage configuration for the given feature. Will be
     * called before [Feature.onInitialize].
     */
    FeatureStorageConfiguration getStorageConfiguration();

    // Assuming Feature interface is already defined elsewhere
}
