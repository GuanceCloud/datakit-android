package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class ActionBean {
    String id = UUID.randomUUID().toString();
    long startTime = Utils.getCurrentNanoTime();

    String actionName;
    String actionType;

    int longTaskCount;
    int resourceCount;
    int errorCount;

    boolean isClose = false;

    long duration = 0;
    String sessionId;

    String viewId;
    String viewName;
    String viewReferrer;

    HashMap<String, Object> property = new HashMap<>();

    public HashMap<String, Object> getProperty() {
        return property;
    }

    public ActionBean() {

    }

    public String getSessionId() {
        return sessionId;
    }

    public String getViewId() {
        return viewId;
    }

    public String getViewName() {
        return viewName;
    }

    public String getViewReferrer() {
        return viewReferrer;
    }


    public String getActionType() {
        return actionType;
    }


    public String getId() {
        return id;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getActionName() {
        return actionName;
    }

    public boolean isClose() {
        return isClose;
    }

    public void setClose(boolean close) {
        isClose = close;
    }

    public long getDuration() {
        return duration;
    }

    public int getLongTaskCount() {
        return longTaskCount;
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public void setLongTaskCount(int longTaskCount) {
        this.longTaskCount = longTaskCount;
    }

    public void setResourceCount(int resourceCount) {
        this.resourceCount = resourceCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public void setViewReferrer(String viewReferrer) {
        this.viewReferrer = viewReferrer;
    }

    public String getAttrJsonString() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.KEY_RUM_PROPERTY, property);
        return new Gson().toJson(map);
    }

    public void setFromAttrJsonString(String jsonString) {
        if (jsonString == null) return;
        try {

            JSONObject json = new JSONObject(jsonString);
            JSONObject jsonProperty = json.optJSONObject(Constants.KEY_RUM_PROPERTY);
            if (jsonProperty != null) {
                Iterator<String> keys = jsonProperty.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    this.property.put(key, jsonProperty.opt(key));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @NonNull
    @Override
    public String toString() {
        return "ActionBean{" +
                "startTime=" + startTime +
                ", actionName='" + actionName + '\'' +
                ", actionType='" + actionType + '\'' +
                ", longTaskCount=" + longTaskCount +
                ", resourceCount=" + resourceCount +
                ", errorCount=" + errorCount +
                ", isClose=" + isClose +
                ", duration=" + duration +
                ", sessionId='" + sessionId + '\'' +
                ", viewId='" + viewId + '\'' +
                ", viewName='" + viewName + '\'' +
                ", viewReferrer='" + viewReferrer + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

}
