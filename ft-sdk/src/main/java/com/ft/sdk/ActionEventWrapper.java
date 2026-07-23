package com.ft.sdk;

import java.util.HashMap;

/**
 * Wraps the source object and extra attributes for an automatically collected action.
 * <p>
 * Instances are passed to {@link FTActionTrackingHandler} so callers can decide
 * whether an action should be renamed, enriched, or ignored.
 */
public class ActionEventWrapper {
    private final Object source;
    private final ActionSourceType sourceType;
    private final HashMap<String, Object> extra;

    /**
     * Creates an action event wrapper.
     *
     * @param source           original action source, such as a clicked View or Activity class
     * @param actionSourceType source category describing where the action came from
     * @param extra            SDK-provided action attributes, or null when none are available
     */
    public ActionEventWrapper(Object source,
                              ActionSourceType actionSourceType, HashMap<String, Object> extra) {
        this.source = source;
        this.sourceType = actionSourceType;
        this.extra = extra;
    }

    /**
     * Returns the original action source object.
     */
    public Object getSource() {
        return source;
    }

    /**
     * Returns the category of the original action source.
     */
    public ActionSourceType getSourceType() {
        return sourceType;
    }

    /**
     * Returns SDK-provided action attributes that can be reused or extended.
     */
    public HashMap<String, Object> getExtra() {
        return extra;
    }
}
