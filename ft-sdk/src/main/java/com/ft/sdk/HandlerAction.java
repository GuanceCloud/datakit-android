package com.ft.sdk;

import java.util.HashMap;

/**
 * Result object returned by {@link FTActionTrackingHandler}.
 * <p>
 * Use it to provide the action name and optional custom properties that should
 * be written to RUM action data.
 */
public class HandlerAction {
    private final String actionName;
    private final HashMap<String, Object> property;

    /**
     * Creates a handled action result with custom properties.
     *
     * @param actionName action name reported to RUM
     * @param property   custom action properties, or null when no extra properties are needed
     */
    public HandlerAction(String actionName, HashMap<String, Object> property) {
        this.actionName = actionName;
        this.property = property;
    }

    /**
     * Creates a handled action result without custom properties.
     *
     * @param actionName action name reported to RUM
     */
    public HandlerAction(String actionName) {
        this.actionName = actionName;
        this.property = null;
    }

    /**
     * Returns the action name reported to RUM.
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * Returns the custom properties attached to the action, or null when none were set.
     */
    public HashMap<String, Object> getProperty() {
        return property;
    }

}
