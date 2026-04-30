package com.ft.sdk.sessionreplay;

import com.ft.sdk.sessionreplay.recorder.OptionSelectorDetector;
import com.ft.sdk.sessionreplay.utils.DrawableToColorMapper;

import java.util.List;

/**
 * Provides custom components that extend how Session Replay maps and records views.
 */
public interface ExtensionSupport {

    /**
     * Use this method to apply custom mappers for specific Android {@code View} types.
     *
     * @return the list of mapper wrappers to register
     */
    List<MapperTypeWrapper<?>> getCustomViewMappers();

    /**
     * Implement this method if you need to return some specific implementations for the
     * {@link OptionSelectorDetector}.
     *
     * @return a list of custom option selector detectors
     */
    List<OptionSelectorDetector> getOptionSelectorDetectors();

    /**
     * Returns custom drawable color mappers used while resolving view backgrounds.
     *
     * @return a list of drawable color mappers to register
     */
    List<DrawableToColorMapper> getCustomDrawableMapper();
}
