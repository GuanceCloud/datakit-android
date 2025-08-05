package com.ft.sdk;


import java.util.HashMap;

/**
 * Handler view
 */
public class HandlerView {
    private String viewName;
    private HashMap<String, Object> property;

    public HandlerView(String viewName, HashMap<String, Object> property) {
        this.viewName = viewName;
        this.property = property;
    }

    public HandlerView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public HashMap<String, Object> getProperty() {
        return property;
    }

    public void setProperty(HashMap<String, Object> property) {
        this.property = property;
    }
}
