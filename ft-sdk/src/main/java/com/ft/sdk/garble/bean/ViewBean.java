package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Utils;

import java.util.UUID;

public class ViewBean {
    String id = UUID.randomUUID().toString();
    String viewReferrer;
    String viewName;

    int longTaskCount;
    int resourceCount;
    int errorCount;
    int actionCount;

    boolean isClose = false;
    long startTime = Utils.getCurrentNanoTime();
    long loadTime = 0;
    long timeSpent = 0;
    String sessionId;

    public static ViewBean createLaunchViewBean(String sessionId) {
        return new ViewBean("", "", 0, sessionId);
    }

    public ViewBean() {
    }

    public ViewBean(String name, String viewReferrer, long loadTime, String sessionId) {
        this.viewName = name;
        this.viewReferrer = viewReferrer;
        this.loadTime = loadTime;
        this.sessionId = sessionId;
    }


    public String getViewReferrer() {
        return viewReferrer;
    }

    public String getViewName() {
        return viewName;
    }


    public String getId() {
        return id;
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isClose() {
        return isClose;
    }

    public void close() {
        isClose = true;
        timeSpent = Utils.getCurrentNanoTime() - startTime;
    }

    public int getLongTaskCount() {
        return longTaskCount;
    }

    public void setClose(boolean close) {
        isClose = close;
    }

    public long getLoadTime() {
        return loadTime;
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getActionCount() {
        return actionCount;
    }

    public long getTimeSpent() {
        return timeSpent;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setViewReferrer(String viewReferrer) {
        this.viewReferrer = viewReferrer;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
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

    public void setActionCount(int actionCount) {
        this.actionCount = actionCount;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setLoadTime(long loadTime) {
        this.loadTime = loadTime;
    }

    public void setTimeSpent(long timeSpent) {
        this.timeSpent = timeSpent;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
