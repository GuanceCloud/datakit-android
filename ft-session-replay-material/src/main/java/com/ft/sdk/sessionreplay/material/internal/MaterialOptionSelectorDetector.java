package com.ft.sdk.sessionreplay.material.internal;

import android.view.ViewGroup;

import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;

public class MaterialOptionSelectorDetector implements OptionSelectorDetector {

    @Override
    public boolean isOptionSelector(ViewGroup view) {
        return view.getId() == com.google.android.material.R.id.mtrl_picker_header;
    }
}
