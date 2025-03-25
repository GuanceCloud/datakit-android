package com.ft.sdk.sessionreplay.internal.recorder;

import android.view.View;
import android.view.ViewTreeObserver;

import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.internal.TouchPrivacyManager;

import java.util.List;

public interface OnDrawListenerProducer {

    ViewTreeObserver.OnDrawListener create(List<View> decorViews,
                                           TextAndInputPrivacy textAndInputPrivacy,
                                           ImagePrivacy imagePrivacy,
                                           TouchPrivacyManager touchPrivacyManager);
}
