package com.ft.sdk.sessionreplay.recorder.mapper;

import android.view.View;

import androidx.annotation.UiThread;

import com.ft.sdk.sessionreplay.model.Wireframe;
import com.ft.sdk.sessionreplay.recorder.MappingContext;
import com.ft.sdk.sessionreplay.utils.AsyncJobStatusCallback;
import com.ft.sdk.sessionreplay.utils.InternalLogger;

import java.util.List;

public interface WireframeMapper<T extends View> {

    @UiThread
    List<Wireframe> map(
            T view,
            MappingContext mappingContext,
            AsyncJobStatusCallback asyncJobStatusCallback,
            InternalLogger internalLogger
    );
}
