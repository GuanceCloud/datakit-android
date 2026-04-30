package com.ft.sdk.sessionreplay.recorder.mapper;

import android.view.ViewGroup;

/**
 * Marker mapper for view groups whose children should all be traversed.
 *
 * @param <T> the view group type supported by the mapper
 */
public interface TraverseAllChildrenMapper<T extends ViewGroup> extends WireframeMapper<T> {
}
