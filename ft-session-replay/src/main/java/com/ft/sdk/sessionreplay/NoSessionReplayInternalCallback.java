package com.ft.sdk.sessionreplay;

import android.app.Activity;

public class NoSessionReplayInternalCallback implements SessionReplayInternalCallback{
    @Override
    public Activity getCurrentActivity() {
        return null;
    }
}
