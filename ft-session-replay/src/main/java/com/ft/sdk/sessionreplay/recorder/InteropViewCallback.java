package com.ft.sdk.sessionreplay.recorder;

import androidx.annotation.UiThread;
import android.view.View;

import com.ft.sdk.sessionreplay.model.Wireframe;

import java.util.List;

/**
 * Callback used by mappers to delegate recording of embedded or interop views.
 */
public interface InteropViewCallback {

    /**
     * Called when an interop view needs to be mapped.
     *
     * @param view the interop view to map
     * @param mappingContext context for the current recording pass
     * @return wireframes representing the interop view
     */
    @UiThread
    List<Wireframe> map(View view, MappingContext mappingContext);
}
