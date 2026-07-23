package com.ft.sdk.sessionreplay.recorder;

import android.view.ViewGroup;

/**
 * Detects containers whose children represent a selectable option group.
 */
public interface OptionSelectorDetector {

    /**
     * Checks and returns true if this ViewGroup is considered as a container of selectable UI
     * elements, otherwise returns false.
     *
     * @param view the view group to inspect
     * @return true when the view group should be treated as an option selector
     */
    boolean isOptionSelector(ViewGroup view);
}
