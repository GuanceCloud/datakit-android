package com.ft.sdk;

import java.util.HashMap;

public class ActionEventWrapper {
    private final Object source;
    private final ActionSourceType sourceType;
    private final HashMap<String, Object> extra;

    public ActionEventWrapper(Object source,
                              ActionSourceType actionSourceType, HashMap<String, Object> extra) {
        this.source = source;
        this.sourceType = actionSourceType;
        this.extra = extra;
    }

    public Object getSource() {
        return source;
    }

    public ActionSourceType getSourceType() {
        return sourceType;
    }

    public HashMap<String, Object> getExtra() {
        return extra;
    }
}