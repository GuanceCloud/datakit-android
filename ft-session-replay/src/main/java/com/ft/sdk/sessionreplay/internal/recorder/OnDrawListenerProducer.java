package com.ft.sdk.sessionreplay.internal.recorder;

import android.view.View;
import android.view.ViewTreeObserver;

import com.ft.sdk.sessionreplay.SessionReplayPrivacy;

import java.util.List;

public interface OnDrawListenerProducer {

    ViewTreeObserver.OnDrawListener create(List<View> decorViews, SessionReplayPrivacy privacy);
}
