package com.ft.sdk;

import java.util.HashMap;

public class HandlerAction {
    private final String actionName;
    private final HashMap<String, Object> property;

    public HandlerAction(String actionName, HashMap<String, Object> property) {
        this.actionName = actionName;
        this.property = property;
    }

    public HandlerAction(String actionName) {
        this.actionName = actionName;
        this.property = null;
    }

    public String getActionName() {
        return actionName;
    }


    public HashMap<String, Object> getProperty() {
        return property;
    }

}
