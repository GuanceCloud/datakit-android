package com.ft.sdk.sessionreplay.utils;

import android.view.View;

public interface ViewIdentifierResolver {

    /**
     * Resolves a persistent, unique id for the given view.
     * @param view the view
     * @return an identifier uniquely mapping a view to a wireframe, allowing accurate diffs
     */
    long resolveViewId(View view);

    /**
     * Generates a persistent unique identifier for a virtual child view based on its unique
     * name and its physical parent. The identifier will only be created once and persisted in
     * the parent [View] tag to provide consistency. In case there was already a value with the
     * same key in the tags and this was used by a different party we will try to use this value
     * as identifier if it's a [Long], in other case we will return null. This last scenario is
     * highly unlikely but we are doing this in order to safely treat possible collisions with
     * client tags.
     * @param parent the parent [View] of the virtual child
     * @param childName the unique name of the virtual child
     * @return the unique identifier as [Long] or null if the identifier could not be created
     */
    Long resolveChildUniqueIdentifier(View parent, String childName);
}
