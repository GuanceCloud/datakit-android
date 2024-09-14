package com.ft.sdk.sessionreplay.internal;

import com.ft.sdk.sessionreplay.MapperTypeWrapper;
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.ExtensionSupport;

import java.util.Collections;
import java.util.List;

public class NoOpExtensionSupport implements ExtensionSupport {

    @Override
    public List<MapperTypeWrapper<?>> getCustomViewMappers() {
        return List.of();
    }

    @Override
    public List<OptionSelectorDetector> getOptionSelectorDetectors() {
        return List.of();
    }
}