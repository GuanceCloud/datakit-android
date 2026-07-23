package com.ft.sdk;


import java.util.HashMap;

/**
 * Result object returned by view tracking handlers.
 * <p>
 * Use it to customize the RUM view name and optional custom properties for an
 * Activity or Fragment.
 */
public class HandlerView {
    private String viewName;
    private HashMap<String, Object> property;

    /**
     * Creates a handled view result with custom properties.
     *
     * @param viewName view name reported to RUM
     * @param property custom view properties, or null when no extra properties are needed
     */
    public HandlerView(String viewName, HashMap<String, Object> property) {
        this.viewName = viewName;
        this.property = property;
    }

    /**
     * Creates a handled view result without custom properties.
     *
     * @param viewName view name reported to RUM
     */
    public HandlerView(String viewName) {
        this.viewName = viewName;
    }

    /**
     * Returns the view name reported to RUM.
     */
    public String getViewName() {
        return viewName;
    }

    /**
     * Updates the view name reported to RUM.
     *
     * @param viewName view name to report
     */
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    /**
     * Returns the custom view properties, or null when none were set.
     */
    public HashMap<String, Object> getProperty() {
        return property;
    }

    /**
     * Updates the custom view properties.
     *
     * @param property custom properties to attach to the view
     */
    public void setProperty(HashMap<String, Object> property) {
        this.property = property;
    }
}
