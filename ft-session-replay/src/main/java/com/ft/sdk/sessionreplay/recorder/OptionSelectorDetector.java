package com.ft.sdk.sessionreplay.recorder;

import android.view.ViewGroup;

public interface OptionSelectorDetector {

    /**
     * Checks and returns true if this ViewGroup is considered as a container of selectable UI
     * elements, otherwise returns false.
     */
    boolean isOptionSelector(ViewGroup view);
}