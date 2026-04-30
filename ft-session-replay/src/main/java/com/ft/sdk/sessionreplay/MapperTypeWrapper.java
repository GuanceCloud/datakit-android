package com.ft.sdk.sessionreplay;

import android.view.View;

import com.ft.sdk.sessionreplay.recorder.mapper.WireframeMapper;

/**
 * Associates an Android {@link View} type with the mapper that can convert it
 * into Session Replay wireframes.
 *
 * @param <T> the supported view type
 */
public class MapperTypeWrapper<T extends View> {
    private final Class<T> type;
    private final WireframeMapper<T> mapper;

    /**
     * Creates a wrapper for a custom view mapper.
     *
     * @param type the view class handled by the mapper
     * @param mapper the mapper used for views assignable to {@code type}
     */
    public MapperTypeWrapper(Class<T> type, WireframeMapper<T> mapper) {
        this.type = type;
        this.mapper = mapper;
    }

    /**
     * Returns the view type supported by this mapper.
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Returns the mapper associated with the view type.
     */
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
     *
     * @return the mapper cast to a generic View mapper
     */
    @SuppressWarnings("unchecked")
    public WireframeMapper<View> getUnsafeMapper() {
        return (WireframeMapper<View>) mapper;
    }
}
