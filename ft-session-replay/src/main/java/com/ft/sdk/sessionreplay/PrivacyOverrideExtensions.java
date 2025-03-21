package com.ft.sdk.sessionreplay;

import android.view.View;

public class PrivacyOverrideExtensions {

    /**
     * Allows setting a view to be "hidden" in the hierarchy in Session Replay.
     * When hidden, the view will be replaced with a placeholder in the replay and
     * no attempt will be made to record its children.
     *
     * @param view The view to modify.
     * @param hide Pass `true` to hide the view, or `false` to remove the override.
     */
    public static void setSessionReplayHidden(View view, boolean hide) {
        if (hide) {
            view.setTag(R.id.ft_hidden, true);
        } else {
            view.setTag(R.id.ft_hidden, null);
        }
    }

    /**
     * Allows overriding the image privacy for a view in Session Replay.
     *
     * @param view    The view to modify.
     * @param privacy The new privacy level to use for the view, or `null` to remove the override.
     */
    public static void setSessionReplayImagePrivacy(View view, ImagePrivacy privacy) {
        if (privacy == null) {
            view.setTag(R.id.ft_image_privacy, null);
        } else {
            view.setTag(R.id.ft_image_privacy, privacy.toString());
        }
    }

    /**
     * Allows overriding the text and input privacy for a view in Session Replay.
     *
     * @param view    The view to modify.
     * @param privacy The new privacy level to use for the view, or `null` to remove the override.
     */
    public static void setSessionReplayTextAndInputPrivacy(View view, TextAndInputPrivacy privacy) {
        if (privacy == null) {
            view.setTag(R.id.ft_text_and_input_privacy, null);
        } else {
            view.setTag(R.id.ft_text_and_input_privacy, privacy.toString());
        }
    }

    /**
     * Allows overriding the touch privacy for a view in Session Replay.
     *
     * @param view    The view to modify.
     * @param privacy The new privacy level to use for the view, or `null` to remove the override.
     */
    public static void setSessionReplayTouchPrivacy(View view, TouchPrivacy privacy) {
        if (privacy == null) {
            view.setTag(R.id.ft_touch_privacy, null);
        } else {
            view.setTag(R.id.ft_touch_privacy, privacy.toString());
        }
    }
}