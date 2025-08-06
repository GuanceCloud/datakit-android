package com.ft.sdk;

import java.util.HashMap;

public class HandlerAction {
    private String actionName;
    private HashMap<String, Object> property;

    public HandlerAction(String actionName, HashMap<String, Object> property) {
        this.actionName = actionName;
        this.property = property;
    }

    public HandlerAction(String actionName) {
        this.actionName = actionName;
    }

    public String getActionName() {
        return actionName;
    }


    public HashMap<String, Object> getProperty() {
        return property;
    }

}
