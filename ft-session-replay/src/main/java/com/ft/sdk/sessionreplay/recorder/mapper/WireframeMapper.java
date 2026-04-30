package com.ft.sdk.sessionreplay.recorder.mapper;

import android.view.View;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.util.List;

/**
 * Converts an Android {@link View} into Session Replay wireframes.
 *
 * @param <T> the view type supported by the mapper
 */
public interface WireframeMapper<T extends View> {

    /**
     * Maps the provided view into one or more wireframes.
     *
     * @param view the view to map
     * @param mappingContext contextual data and helpers for the current recording pass
     * @param asyncJobStatusCallback callback for asynchronous resource work
     * @param internalLogger logger used for non-fatal mapper diagnostics
     * @return wireframes representing the view
     */
    @UiThread
    List<Wireframe> map(
            T view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    );
}
