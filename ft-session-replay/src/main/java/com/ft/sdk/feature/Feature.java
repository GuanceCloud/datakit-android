package com.ft.sdk.feature;

import android.content.Context;

public interface Feature {

    /**
     * Gets the name of the feature.
     */
    String getName();

    /**
     * This method is called during feature initialization. At this stage feature should set up itself.
     *
     * @param appContext Application context.
     */
    void onInitialize(Context appContext);

    /**
     * This method is called during feature de-initialization. At this stage feature should stop
     * itself and release resources held.
     */
    void onStop();

    /**
     * RUM feature name.
     */
    String RUM_FEATURE_NAME = "rum";

    /**
     * Session Replay feature name.
     */
    String SESSION_REPLAY_FEATURE_NAME = "session-replay";

    /**
     * Session Replay Resources sub-feature name.
     */
    String SESSION_REPLAY_RESOURCES_FEATURE_NAME = "session-replay-resources";
}
