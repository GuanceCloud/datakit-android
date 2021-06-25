package com.ft.sdk;

import android.os.Handler;
import android.os.Looper;

import com.ft.sdk.garble.bean.ActionBean;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;
import com.ft.sdk.garble.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FTRUMGlobalManager {
    private static final String TAG = "RUMGlobalManager";
    static final long MAX_RESTING_TIME = 15000000000L;
    static final long SESSION_EXPIRE_TIME = 1440000000000000L;
    static final long FILTER_CAPACITY = 5;


    private FTRUMGlobalManager() {

    }

    private static class SingletonHolder {
        private static final FTRUMGlobalManager INSTANCE = new FTRUMGlobalManager();
    }

    public static FTRUMGlobalManager get() {
        return FTRUMGlobalManager.SingletonHolder.INSTANCE;
    }

    private FTRUMConfig config;

    private String sessionId = UUID.randomUUID().toString();


    private final ArrayList<String> notCollectMap = new ArrayList<>();

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
        if (!config.isEnableTraceUserAction()) {
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
        if (!config.isEnableTraceUserAction()) {
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
            activeAction.close();
            closeAction(activeAction.getId(), activeAction.getDuration(), timeOut);
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


    void increaseError(@NotNull JSONObject tags) {

        String actionId = tags.optString(Constants.KEY_RUM_ACTION_ID);
        String viewId = tags.optString(Constants.KEY_RUM_VIEW_ID);
        ThreadPoolUtils.get().execute(() -> {
            FTDBManager.get().increaseActionError(actionId);
            FTDBManager.get().increaseViewError(viewId);
            checkActionClose();
        });
    }

    void increaseLongTask(@NotNull JSONObject tags) {
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
        if (activeView == null) {
            return null;
        }
        return activeView.getId();
    }

    String getViewName() {
        if (activeView == null) {
            return null;
        }
        return activeView.getViewName();
    }

    String getViewReferrer() {
        if (activeView == null) {
            return null;
        }
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
    public void attachRUMRelative(@NotNull JSONObject tags) {
        attachRUMRelative(tags, true);
    }

    /**
     * @param tags
     * @param withAction
     */
    public void attachRUMRelative(@NotNull JSONObject tags, boolean withAction) {
        try {
            tags.put(Constants.KEY_RUM_VIEW_ID, getViewId());

            tags.put(Constants.KEY_RUM_VIEW_NAME, FTRUMGlobalManager.get().getViewName());
            tags.put(Constants.KEY_RUM_VIEW_REFERRER, FTRUMGlobalManager.get().getViewReferrer());
            tags.put(Constants.KEY_RUM_SESSION_ID, sessionId);
            if (withAction) {
                tags.put(Constants.KEY_RUM_ACTION_ID, getActionId());
                tags.put(Constants.KEY_RUM_ACTION_NAME, FTRUMGlobalManager.get().getActionName());
            }
        } catch (JSONException e) {
            LogUtils.e(TAG, e.getMessage());
        }
    }


    Handler mHandler = new Handler(Looper.getMainLooper());
    Runnable mRUMGenerateRunner = () -> {
        JSONObject tags = FTAutoTrack.getRUMPublicTags();

        ThreadPoolUtils.get().execute(() -> {
            try {
                generateActionSum(tags);
                generateViewSum(tags);
            } catch (JSONException e) {
                LogUtils.e(TAG, e.getMessage());
            }
        });
    };

    public static int LIMIT_SIZE = 50;

    private void generateRumData() {
        //避免过于频繁的刷新
        mHandler.removeCallbacks(mRUMGenerateRunner);
        mHandler.postDelayed(mRUMGenerateRunner, 100);
    }

    private void generateActionSum(JSONObject globalTags) throws JSONException {
        ArrayList<ActionBean> beans = FTDBManager.get().querySumAction(LIMIT_SIZE);
        for (ActionBean bean : beans) {
            JSONObject fields = new JSONObject();
            JSONObject tags = new JSONObject(globalTags.toString());
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
                LogUtils.e(TAG, e.getMessage());
            }
            FTDBManager.get().cleanCloseActionData();
        }
        if (beans.size() < LIMIT_SIZE) {

        } else {
            generateActionSum(globalTags);
        }
    }

    private void generateViewSum(JSONObject globalTags) throws JSONException {
        ArrayList<ViewBean> beans = FTDBManager.get().querySumView(LIMIT_SIZE);
        for (ViewBean bean : beans) {
            JSONObject fields = new JSONObject();
            JSONObject tags = new JSONObject(globalTags.toString());
            long time = Utils.getCurrentNanoTime();

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
                if (activeView.isClose()) {
                    fields.put(Constants.KEY_RUM_VIEW_TIME_SPENT, bean.getTimeSpent());
                } else {
                    fields.put(Constants.KEY_RUM_VIEW_TIME_SPENT, time - bean.getStartTime());
                }
                fields.put(Constants.KEY_RUM_VIEW_LONG_TASK_COUNT, bean.getLongTaskCount());
                fields.put(Constants.KEY_RUM_VIEW_IS_ACTIVE, !bean.isClose());
            } catch (JSONException e) {
                LogUtils.e(TAG, e.getMessage());
            }

            FTTrackInner.getInstance().rum(time,
                    Constants.FT_MEASUREMENT_RUM_VIEW, tags, fields);


        }
        FTDBManager.get().cleanCloseViewData();
        if (beans.size() < LIMIT_SIZE) {


        } else {
            generateViewSum(globalTags);
        }
    }

    public void initParams(FTRUMConfig config) {
        this.config = config;
        checkSessionKeep(sessionId, config.getSamplingRate());
        ThreadPoolUtils.get().execute(() -> {
            FTDBManager.get().closeAllActionAndView();
        });
    }

    private void checkSessionKeep(String sessionId, float sampleRate) {
        boolean collect = Utils.enableTraceSamplingRate(sampleRate);
        if (!collect) {
            if (notCollectMap.size() + 1 > FILTER_CAPACITY) {
                notCollectMap.remove(0);
            }
            notCollectMap.add(sessionId);

        }
    }

    public boolean checkSessionWillCollect(String sessionId) {
        return !notCollectMap.contains(sessionId);
    }

    public void release() {
        mHandler.removeCallbacks(mRUMGenerateRunner);
    }


}
