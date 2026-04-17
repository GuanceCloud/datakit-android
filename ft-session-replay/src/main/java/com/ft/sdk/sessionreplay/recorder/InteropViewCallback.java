package com.ft.sdk.sessionreplay.recorder;

import androidx.annotation.UiThread;
import android.view.View;

import com.ft.sdk.sessionreplay.model.Wireframe;

import java.util.List;

public interface InteropViewCallback {

    /**
     * Called when an interop view needs to be mapped.
     */
    @UiThread
    List<Wireframe> map(View view, MappingContext mappingContext);
}