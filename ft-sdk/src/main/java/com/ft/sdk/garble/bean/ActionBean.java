package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Utils;

import java.util.UUID;

public class ActionBean {
    public static final long ACTION_NEED_WAIT_TIME_OUT = 5000000000L;
    public static final long ACTION_NORMAL_TIME_OUT = 100000000L;

    long startTime = Utils.getCurrentNanoTime();
    boolean needWaitAction = false;

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

    public boolean isNeedWaitAction() {
        return needWaitAction;
    }

    public static ActionBean createLaunchAction(String sessionId, ViewBean viewBean) {
        return new ActionBean("", "", sessionId, viewBean, false);
    }

    public ActionBean() {

    }

    public ActionBean(String actionName, String actionType, String sessionId, ViewBean viewBean, boolean needWaitAction) {
        this.actionName = actionName;
        this.actionType = actionType;
        this.sessionId = sessionId;
        this.viewId = viewBean.getId();
        this.viewName = viewBean.getViewName();
        this.viewReferrer = viewBean.getViewReferrer();
        this.needWaitAction = needWaitAction;
    }

    public String getActionType() {
        return actionType;
    }

    String id = UUID.randomUUID().toString();

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

    public void close() {
        isClose = true;
        duration = Utils.getCurrentNanoTime() - startTime;
//        if (duration > ACTION_NEED_WAIT_TIME_OUT) {
//            duration = ACTION_NEED_WAIT_TIME_OUT;
//        }
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


    @Override
    public String toString() {
        return "ActionBean{" +
                "startTime=" + startTime +
                ", needWaitAction=" + needWaitAction +
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
