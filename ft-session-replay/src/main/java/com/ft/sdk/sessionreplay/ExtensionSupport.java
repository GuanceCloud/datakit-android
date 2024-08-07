package com.ft.sdk.sessionreplay;

import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;

import java.util.List;

public interface ExtensionSupport {

    /**
     * Use this method if you want to apply a custom [WireframeMapper] for a specific [View].
     * @return the list of [MapperTypeWrapper]
     */
    List<MapperTypeWrapper<?>> getCustomViewMappers();

    /**
     * Implement this method if you need to return some specific implementations for the
     * [OptionSelectorDetector].
     * @return a list of custom [OptionSelectorDetector].
     */
    List<OptionSelectorDetector> getOptionSelectorDetectors();
}
