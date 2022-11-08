package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Utils;

public class ActiveActionBean extends ActionBean {
    public static final long ACTION_NEED_WAIT_TIME_OUT = 5000000000L;
    public static final long ACTION_NORMAL_TIME_OUT = 100000000L;

    boolean needWaitAction = false;

    public ActiveActionBean(String actionName, String actionType,
                            String sessionId, String viewId, String viewName, String viewReferrer, boolean needWaitAction) {
        this.actionName = actionName;
        this.actionType = actionType;
        this.sessionId = sessionId;
        this.viewId = viewId;
        this.viewName = viewName;
        this.viewReferrer = viewReferrer;
        this.needWaitAction = needWaitAction;
    }

    public void close() {
        this.isClose = true;
        duration = Utils.getCurrentNanoTime() - startTime;
//        if (duration > ACTION_NEED_WAIT_TIME_OUT) {
//            duration = ACTION_NEED_WAIT_TIME_OUT;
//        }
    }

    public boolean isNeedWaitAction() {
        return needWaitAction;
    }

    public ActionBean convertToActionBean() {
        ActionBean bean = new ActionBean();
        bean.id = this.id;
        bean.startTime = this.startTime;
        bean.actionName = this.actionName;
        bean.actionType = this.actionType;
        bean.duration = this.duration;
        bean.errorCount = this.errorCount;
        bean.longTaskCount = this.longTaskCount;
        bean.resourceCount = this.resourceCount;
        bean.isClose = this.isClose;
        bean.sessionId = this.sessionId;
        bean.viewId = this.viewId;
        bean.viewName = this.viewName;
        bean.viewReferrer = this.viewReferrer;
        bean.property = this.property;
        return bean;
    }
}
