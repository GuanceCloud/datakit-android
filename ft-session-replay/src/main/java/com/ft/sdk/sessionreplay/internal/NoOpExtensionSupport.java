package com.ft.sdk.sessionreplay.internal;

import com.ft.sdk.sessionreplay.MapperTypeWrapper;
import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.ExtensionSupport;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoOpExtensionSupport implements ExtensionSupport {

    @Override
    public List<MapperTypeWrapper<?>> getCustomViewMappers() {
        return new ArrayList<>();
    }

    @Override
    public List<OptionSelectorDetector> getOptionSelectorDetectors() {
        return new ArrayList<>();
    }

    @Override
    public List<DrawableToColorMapper> getCustomDrawableMapper() {
        return new ArrayList<>();
    }
}