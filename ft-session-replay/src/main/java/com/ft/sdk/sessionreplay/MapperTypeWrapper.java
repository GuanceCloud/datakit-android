package com.ft.sdk.sessionreplay;

import android.view.View;

import com.ft.sdk.sessionreplay.recorder.mapper.WireframeMapper;


public class MapperTypeWrapper<T extends View> {
    private final Class<T> type;
    private final WireframeMapper<T> mapper;

    public MapperTypeWrapper(Class<T> type, WireframeMapper<T> mapper) {
        this.type = type;
        this.mapper = mapper;
    }

    public Class<T> getType() {
        return type;
    }

    public WireframeMapper<T> getMapper() {
        return mapper;
    }

    /**
     * Checks whether the underlying mapper would support mapping the given view.
     *
     * @param view the view to map
     * @return true if the mapper can take the view as an input
     */
    public boolean supportsView(View view) {
        return type.isAssignableFrom(view.getClass());
    }

    /**
     * Returns the mapper unsafely casted to WireframeMapper<View>.
     */
    @SuppressWarnings("unchecked")
    public WireframeMapper<View> getUnsafeMapper() {
        return (WireframeMapper<View>) mapper;
    }
}
