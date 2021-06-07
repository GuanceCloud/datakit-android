package com.ft.sdk;

import com.ft.sdk.garble.FTUserActionConfig;
import com.ft.sdk.garble.bean.ActionBean;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class RUMGlobalManager {
    private static final String TAG = "RUMGlobalManager";
    static final long MAX_RESTING_TIME = 15000000000L;
    static final long SESSION_EXPIRE_TIME = 1440000000000000L;


    private RUMGlobalManager() {

    }

    private static class SingletonHolder {
        private static final RUMGlobalManager INSTANCE = new RUMGlobalManager();
    }

    public static RUMGlobalManager getInstance() {
        return RUMGlobalManager.SingletonHolder.INSTANCE;
    }


    private String sessionId = UUID.randomUUID().toString();

    ViewBean activeView;
    ActionBean activeAction;

    HashMap<String, Long> preActivityDuration = new HashMap<>();

    long lastSessionTime = Utils.getCurrentNanoTime();
    long lastActionTime = lastSessionTime;

    String getSessionId() {
        return sessionId;
    }

    void checkSessionRefresh() {
        long now = Utils.getCurrentNanoTime();
        boolean longResting = now - lastActionTime > MAX_RESTING_TIME;
        boolean longTimeSession = now - lastSessionTime > SESSION_EXPIRE_TIME;
        if (longTimeSession || longResting) {
            sessionId = UUID.randomUUID().toString();
        }
    }

    String getActionId() {
        return activeAction == null ? null : activeAction.getId();
    }

    void addAction(String actionName, String actionType, long duration) {
        if (!FTUserActionConfig.get().isEnableTraceUserAction()) {
            return;
        }
        checkSessionRefresh();

        activeAction = new ActionBean(actionName, actionType,
                sessionId, activeView, false);
        activeAction.setClose(true);
        activeAction.setDuration(duration);
        initAction(activeAction);
        this.lastActionTime = activeAction.getStartTime();
    }

    void startAction(String actionName, String actionType) {
        startAction(actionName, actionType, false);
    }

    void startAction(String actionName, String actionType, boolean needWait) {
        if (!FTUserActionConfig.get().isEnableTraceUserAction()) {
            return;
        }

        checkSessionRefresh();
        checkActionClose();
        if (activeAction == null || activeAction.isClose() || !activeView.getId().equals(activeView.getId())) {
            activeAction = new ActionBean(actionName, actionType, sessionId, activeView, needWait);
            initAction(activeAction);
            this.lastActionTime = activeAction.getStartTime();
        }
    }

    void checkActionClose() {
        if (activeAction == null) return;
        long now = Utils.getCurrentNanoTime();
        long lastActionTime = activeAction.getStartTime();
        boolean waiting = activeAction.isNeedWaitAction() && !activeView.isClose();
        boolean timeOut = now - lastActionTime > ActionBean.ACTION_NEED_WAIT_TIME_OUT;
        boolean needClose = !waiting
                && (now - lastActionTime > ActionBean.ACTION_NORMAL_TIME_OUT)
                || timeOut;
        if (needClose) {
            closeAction(activeAction.getId(), activeAction.getDuration(), timeOut);
            activeAction.close();
        }
    }

    void stopAction() {
        if (activeAction.isNeedWaitAction()) {
            activeAction.close();
        }
    }

    void startResource(String viewId, String actionId) {
        ThreadPoolUtils.get().execute(() -> {
            FTDBManager.get().increaseViewPendingResource(viewId);
            FTDBManager.get().increaseActionPendingResource(actionId);
        });
    }

    void stopResource(String viewId, String actionId) {
        increaseResourceCount(viewId, actionId);
        ThreadPoolUtils.get().execute(() -> {
            FTDBManager.get().reduceViewPendingResource(viewId);
            FTDBManager.get().reduceActionPendingResource(actionId);
        });
    }

    void onCreateView(String viewName, long loadTime) {
        preActivityDuration.put(viewName, loadTime);
    }


    void startView(String viewName, String viewReferrer) {
        checkSessionRefresh();
        if (activeView != null && !activeView.isClose()) {
            activeView.close();
            closeView(activeView.getId(), activeView.getTimeSpent());
        }

        long loadTime = -1;
        if (preActivityDuration.get(viewName) != null) {
            loadTime = preActivityDuration.get(viewName);
            preActivityDuration.remove(viewName);
        }
        activeView = new ViewBean(viewName, viewReferrer, loadTime, sessionId);
        initView(activeView);

    }

    void stopView() {
        checkActionClose();

        activeView.close();
        closeView(activeView.getId(), activeView.getTimeSpent());
    }

    void initAction(ActionBean bean) {
        increaseAction(bean.getViewId());
        ThreadPoolUtils.get().execute(() -> {
            FTDBManager.get().initSumAction(bean);
        });

    }

    void initView(ViewBean bean) {
        ThreadPoolUtils.get().execute(() -> {
            FTDBManager.get().initSumView(bean);
        });
    }

    void increaseResourceCount(String viewId, String actionId) {
        ThreadPoolUtils.get().execute(() -> {
            FTDBManager.get().increaseViewResource(viewId);
            FTDBManager.get().increaseActionResource(actionId);
            checkActionClose();
        });

    }


    void increaseError(JSONObject tags) {

        String actionId = tags.optString(Constants.KEY_RUM_ACTION_ID);
        String viewId = tags.optString(Constants.KEY_RUM_VIEW_ID);
        ThreadPoolUtils.get().execute(() -> {
            FTDBManager.get().increaseActionError(actionId);
            FTDBManager.get().increaseViewError(viewId);
            checkActionClose();
        });
    }

    void increaseLongTask(JSONObject tags) {
        String actionId = tags.optString(Constants.KEY_RUM_ACTION_ID);
        String viewId = tags.optString(Constants.KEY_RUM_VIEW_ID);
        ThreadPoolUtils.get().execute(() -> {
            FTDBManager.get().increaseActionLongTask(actionId);
            FTDBManager.get().increaseViewLongTask(viewId);
            checkActionClose();

        });
    }

    void increaseAction(String viewId) {
        ThreadPoolUtils.get().execute(() -> {
            FTDBManager.get().increaseViewAction(viewId);

        });

    }


    void closeView(String viewId, long timeSpent) {
        ThreadPoolUtils.get().execute(() -> {
            FTDBManager.get().closeView(viewId, timeSpent);

        });
        generateRumData();
    }

    void closeAction(String actionId, long duration, boolean force) {
        ThreadPoolUtils.get().execute(() -> {
            FTDBManager.get().closeAction(actionId, duration, force);
        });
        generateRumData();
    }


    String getViewId() {
        return activeView.getId();
    }

    String getViewName() {
        return activeView.getViewName();
    }

    String getViewReferrer() {
        return activeView.getViewReferrer();
    }

    String getActionName() {
        return activeAction == null ? null : activeAction.getActionName();
    }

    /**
     * 设置 RUM 全局 view session action 关联数据
     *
     * @param tags
     */
    public void attachRUMRelative(JSONObject tags) {
        try {
            tags.put(Constants.KEY_RUM_VIEW_ID, getViewId());

            tags.put(Constants.KEY_RUM_VIEW_NAME, RUMGlobalManager.getInstance().getViewName());
            tags.put(Constants.KEY_RUM_VIEW_REFERRER, RUMGlobalManager.getInstance().getViewReferrer());
            tags.put(Constants.KEY_RUM_SESSION_ID, sessionId);

            tags.put(Constants.KEY_RUM_ACTION_ID, getActionId());
            tags.put(Constants.KEY_RUM_ACTION_NAME, RUMGlobalManager.getInstance().getActionName());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static int LIMIT_SIZE = 50;

    private void generateRumData() {
        JSONObject tags = FTAutoTrack.getRUMPublicTags();

        ThreadPoolUtils.get().execute(() -> {
            generateActionSum(tags);
            generateViewSum(tags);
        });
    }

    private void generateActionSum(JSONObject tags) {
        ArrayList<ActionBean> beans = FTDBManager.get().querySumAction(LIMIT_SIZE);
        for (ActionBean bean : beans) {
            JSONObject fields = new JSONObject();
            try {
                tags.put(Constants.KEY_RUM_VIEW_NAME, bean.getViewName());
                tags.put(Constants.KEY_RUM_VIEW_REFERRER, bean.getViewReferrer());
                tags.put(Constants.KEY_RUM_VIEW_ID, bean.getViewId());
                tags.put(Constants.KEY_RUM_ACTION_NAME, bean.getActionName());
                tags.put(Constants.KEY_RUM_ACTION_ID, bean.getId());
                tags.put(Constants.KEY_RUM_ACTION_TYPE, bean.getActionType());
                tags.put(Constants.KEY_RUM_SESSION_ID, bean.getSessionId());
                fields.put(Constants.KEY_RUM_ACTION_LONG_TASK_COUNT, bean.getLongTaskCount());
                fields.put(Constants.KEY_RUM_ACTION_RESOURCE_COUNT, bean.getResourceCount());
                fields.put(Constants.KEY_RUM_ACTION_ERROR_COUNT, bean.getErrorCount());
                fields.put(Constants.KEY_RUM_ACTION_DURATION, bean.getDuration());
                FTTrackInner.getInstance().rum(Utils.getCurrentNanoTime(),
                        Constants.FT_MEASUREMENT_RUM_ACTION, tags, fields);
            } catch (JSONException e) {
                LogUtils.d(TAG, e.getMessage());
            }

            FTDBManager.get().cleanCloseActionData();


        }
        if (beans.size() < LIMIT_SIZE) {

        } else {
            generateActionSum(tags);
        }
    }


    private void generateViewSum(JSONObject tags) {
        ArrayList<ViewBean> beans = FTDBManager.get().querySumView(LIMIT_SIZE);
        for (ViewBean bean : beans) {
            JSONObject fields = new JSONObject();
            try {
                tags.put(Constants.KEY_RUM_SESSION_ID, bean.getSessionId());
                tags.put(Constants.KEY_RUM_VIEW_NAME, bean.getViewName());
                tags.put(Constants.KEY_RUM_VIEW_REFERRER, bean.getViewReferrer());
                tags.put(Constants.KEY_RUM_VIEW_ID, bean.getId());
                if (bean.getLoadTime() > 0) {
                    fields.put(Constants.KEY_RUM_VIEW_LOAD, bean.getLoadTime());
                }
                fields.put(Constants.KEY_RUM_VIEW_ACTION_COUNT, bean.getActionCount());
                fields.put(Constants.KEY_RUM_VIEW_RESOURCE_COUNT, bean.getResourceCount());
                fields.put(Constants.KEY_RUM_VIEW_ERROR_COUNT, bean.getErrorCount());
                fields.put(Constants.KEY_RUM_VIEW_TIME_SPENT, bean.getTimeSpent());
                fields.put(Constants.KEY_RUM_VIEW_LONG_TASK_COUNT, bean.getLongTaskCount());
                fields.put(Constants.KEY_RUM_VIEW_IS_ACTIVE, !bean.isClose());
            } catch (JSONException e) {
                LogUtils.d(TAG, e.getMessage());
            }
            FTTrackInner.getInstance().rum(Utils.getCurrentNanoTime(),
                    Constants.FT_MEASUREMENT_RUM_VIEW, tags, fields);

            FTDBManager.get().cleanCloseViewData();

        }
        if (beans.size() < LIMIT_SIZE) {


        } else {
            generateViewSum(tags);
        }
    }

    public void init() {
        ThreadPoolUtils.get().execute(() -> {
            FTDBManager.get().closeAllActionAndView();

        });
    }

}
