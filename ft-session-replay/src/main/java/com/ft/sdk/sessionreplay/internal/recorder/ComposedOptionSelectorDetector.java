package com.ft.sdk.sessionreplay.internal.recorder;

import android.view.ViewGroup;

import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;

import java.util.List;

public class ComposedOptionSelectorDetector implements OptionSelectorDetector {

    private final List<OptionSelectorDetector> detectors;

    public ComposedOptionSelectorDetector(List<OptionSelectorDetector> detectors) {
        this.detectors = detectors;
    }

    @Override
    public boolean isOptionSelector(ViewGroup view) {
        for (OptionSelectorDetector detector : detectors) {
            if (detector.isOptionSelector(view)) {
                return true;
            }
        }
        return false;
    }
}
