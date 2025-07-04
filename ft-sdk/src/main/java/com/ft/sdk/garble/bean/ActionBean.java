package com.ft.sdk.garble.bean;

import androidx.annotation.NonNull;

import com.ft.sdk.FTRUMInnerManager;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * User behavior operation lifecycle metrics, {@link Constants#FT_MEASUREMENT_RUM_ACTION}
 */
public class ActionBean {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "ActionBean";
    /**
     * Action unique ID, {@link Constants#KEY_RUM_ACTION_ID}
     */
    String id = Utils.randomUUID();
    /**
     * Action start time
     */
    long startTime = Utils.getCurrentNanoTime();

    /**
     * Action name, {@link Constants#KEY_RUM_ACTION_NAME}
     */
    String actionName;

    /**
     * Action type, {@link Constants#KEY_RUM_ACTION_TYPE}
     */
    String actionType;

    /**
     * longtask capture count, {@link Constants#KEY_RUM_ACTION_LONG_TASK_COUNT}
     */
    int longTaskCount;

    /**
     * resource request count, {@link Constants#KEY_RUM_ACTION_RESOURCE_COUNT}
     */
    int resourceCount;
    /**
     * Error count, {@link Constants#KEY_RUM_ACTION_ERROR_COUNT}
     */
    int errorCount;

    /**
     * Whether closed
     */
    boolean isClose = false;

    /**
     * Action duration
     */
    long duration = 0;
    /**
     * Session ID, {@link FTRUMInnerManager#sessionId}
     */
    String sessionId;

    /**
     * Page ID, {@link  ViewBean#id}
     */
    String viewId;

    /**
     * Page name {@link  ViewBean#viewName}
     */
    String viewName;

    /**
     * Page source, page parent {@link ViewBean#viewReferrer}
     */
    String viewReferrer;

    /**
     * Action additional properties, {@link  Constants#KEY_RUM_PROPERTY}
     */
    HashMap<String, Object> property = new HashMap<>();

    public HashMap<String, Object> getProperty() {
        return property;
    }


    HashMap<String, Object> tags = new HashMap<>();

    public HashMap<String, Object> getTags() {
        return tags;
    }

    public void setTags(HashMap<String, Object> tags) {
        this.tags = tags;
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

    public String collectType;

    public CollectType getCollectType() {
        return CollectType.fromValue(collectType);
    }

    public void setCollectType(CollectType collectType) {
        this.collectType = collectType.getValue();
    }

    /**
     * Convert action property data to JSON string, written when data is stored in local cache
     *
     * @return
     */
    public String getAttrJsonString() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.KEY_RUM_PROPERTY, property);
        map.put(Constants.KEY_RUM_TAGS, tags);
        map.put(Constants.KEY_COLLECT_TYPE, collectType);
        return Utils.hashMapObjectToJson(map);
    }

    /**
     * JSON data re-serialization, called when data is retrieved from local cache
     *
     * @param jsonString
     */
    public void setFromAttrJsonString(String jsonString) {
        if (jsonString == null) return;
        try {

            JSONObject json = new JSONObject(jsonString);
            this.collectType = json.getString(Constants.KEY_COLLECT_TYPE);
            JSONObject jsonProperty = json.optJSONObject(Constants.KEY_RUM_PROPERTY);
            if (jsonProperty != null) {
                Iterator<String> keys = jsonProperty.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    this.property.put(key, jsonProperty.opt(key));
                }
            }

            JSONObject jsonTags = json.optJSONObject(Constants.KEY_RUM_TAGS);
            if (jsonTags != null) {
                Iterator<String> keys = jsonTags.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    this.tags.put(key, jsonTags.opt(key));
                }
            }

        } catch (JSONException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));

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
