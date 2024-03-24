package com.ft.sdk.garble.bean;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ft.sdk.FTRUMInnerManager;
import com.ft.sdk.garble.manager.SingletonGson;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

/**
 * 用户行为操作生命周指标，{@link Constants#FT_MEASUREMENT_RUM_ACTION}
 */
public class ActionBean {
    private static final String TAG = Constants.LOG_TAG_PREFIX +"ActionBean";
    /**
     * Action 唯一ID,{@link Constants#KEY_RUM_ACTION_ID}
     */
    String id = Utils.randomUUID();
    /**
     * Action 开始时间
     */
    long startTime = Utils.getCurrentNanoTime();

    /**
     * Action 名称，，{@link Constants#KEY_RUM_ACTION_NAME}
     */
    String actionName;

    /**
     * Action 类型 ，{@link Constants#KEY_RUM_ACTION_TYPE}
     */
    String actionType;

    /**
     * longtask 捕获次数，{@link Constants#KEY_RUM_ACTION_LONG_TASK_COUNT}
     */
    int longTaskCount;

    /**
     * resource 请求次数，{@link Constants#KEY_RUM_ACTION_RESOURCE_COUNT}
     */
    int resourceCount;
    /**
     * 错误次数， {@link Constants#KEY_RUM_ACTION_ERROR_COUNT}
     */
    int errorCount;

    /**
     * 是否关闭
     */
    boolean isClose = false;

    /**
     * Action 持续时间
     */
    long duration = 0;
    /**
     * 会话 ID，{@link FTRUMInnerManager#sessionId}
     */
    String sessionId;

    /**
     * 页面 ID，{@link  ViewBean#id}
     */
    String viewId;

    /**
     * 页面名称 {@link  ViewBean#viewName}
     */
    String viewName;

    /**
     * 页面来源，页面的父级 {@link ViewBean#viewReferrer}
     */
    String viewReferrer;

    /**
     * Action 附加属性,{@link  Constants#KEY_RUM_PROPERTY}
     */
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
        return SingletonGson.getInstance().toJson(map);
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
            LogUtils.e(TAG, Log.getStackTraceString(e));

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
