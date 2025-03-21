package com.ft;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.ft.sdk.sessionreplay.ImagePrivacy;
import com.ft.sdk.sessionreplay.PrivacyOverrideExtensions;
import com.ft.sdk.sessionreplay.TextAndInputPrivacy;
import com.ft.sdk.sessionreplay.TouchPrivacy;

public class SessionReplayPrivacyOverrideActivity extends NameTitleActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_replay_privacy);
//        PrivacyOverrideExtensions.setSessionReplayHidden(findViewById(R.id.session_replay_privacy_ll), true);
        PrivacyOverrideExtensions.setSessionReplayImagePrivacy(findViewById(R.id.session_replay_privacy_ll), ImagePrivacy.MASK_ALL);
        PrivacyOverrideExtensions.setSessionReplayTextAndInputPrivacy(findViewById(R.id.session_replay_privacy_ll), TextAndInputPrivacy.MASK_ALL);
        PrivacyOverrideExtensions.setSessionReplayTouchPrivacy(findViewById(R.id.session_replay_privacy_ll), TouchPrivacy.HIDE);

    }
}
