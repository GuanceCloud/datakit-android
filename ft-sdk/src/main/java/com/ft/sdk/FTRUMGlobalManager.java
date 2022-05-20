package com.ft.sdk;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.ActionBean;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.ErrorSource;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceBean;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.threadpool.EventConsumerThreadPool;
import com.ft.sdk.garble.utils.BatteryUtils;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * RUM 数据管理
 */
public class FTRUMGlobalManager {
    private static final String TAG = "RUMGlobalManager";
    static final long MAX_RESTING_TIME = 15000000000L;
    static final long SESSION_EXPIRE_TIME = 1440000000000000L;
    static final long FILTER_CAPACITY = 5;
    private final ConcurrentHashMap<String, ResourceBean> resourceBeanMap = new ConcurrentHashMap<>();
    private final ArrayList<String> viewList = new ArrayList<>();

    private FTRUMGlobalManager() {

    }

    private static class SingletonHolder {
        private static final FTRUMGlobalManager INSTANCE = new FTRUMGlobalManager();
    }

    public static FTRUMGlobalManager get() {
        return FTRUMGlobalManager.SingletonHolder.INSTANCE;
    }

    private String sessionId = UUID.randomUUID().toString();


    private final ArrayList<String> notCollectMap = new ArrayList<>();

    private ViewBean activeView;
    private ActionBean activeAction;

    private final ConcurrentHashMap<String, Long> preActivityDuration = new ConcurrentHashMap<>();

    private final long lastSessionTime = Utils.getCurrentNanoTime();
    private long lastActionTime = lastSessionTime;

    String getSessionId() {
        return sessionId;
    }

    private void checkSessionRefresh() {
        long now = Utils.getCurrentNanoTime();
        boolean longResting = now - lastActionTime > MAX_RESTING_TIME;
        boolean longTimeSession = now - lastSessionTime > SESSION_EXPIRE_TIME;
        if (longTimeSession || longResting) {
            sessionId = UUID.randomUUID().toString();
        }
    }

    private String getActionId() {
        return activeAction == null ? null : activeAction.getId();
    }

    void addAction(String actionName, String actionType, long duration) {
        String viewId = activeView != null ? activeView.getId() : null;
        String viewName = activeView != null ? activeView.getViewName() : null;
        String viewReferrer = activeView != null ? activeView.getViewReferrer() : null;
        checkSessionRefresh();

        activeAction = new ActionBean(actionName, actionType,
                sessionId, viewId, viewName, viewReferrer, false);
        activeAction.setClose(true);
        activeAction.setDuration(duration);
        initAction(activeAction);
        this.lastActionTime = activeAction.getStartTime();
    }

    /**
     * action 起始
     *
     * @param actionName action 名称
     * @param actionType action 类型
     */
    public void startAction(String actionName, String actionType) {
        startAction(actionName, actionType, false);
    }

    void startAction(String actionName, String actionType, boolean needWait) {

        String viewId = activeView != null ? activeView.getId() : null;
        String viewName = activeView != null ? activeView.getViewName() : null;
        String viewReferrer = activeView != null ? activeView.getViewReferrer() : null;
        checkSessionRefresh();
        checkActionClose();
        if (activeAction == null || activeAction.isClose()) {
            activeAction = new ActionBean(actionName, actionType, sessionId, viewId, viewName, viewReferrer, needWait);
            initAction(activeAction);
            this.lastActionTime = activeAction.getStartTime();
        }
    }

    private void checkActionClose() {
        if (activeAction == null) return;
        long now = Utils.getCurrentNanoTime();
        long lastActionTime = activeAction.getStartTime();
        boolean waiting = activeAction.isNeedWaitAction() && (activeView != null && !activeView.isClose());
        boolean timeOut = now - lastActionTime > ActionBean.ACTION_NEED_WAIT_TIME_OUT;
        boolean needClose = !waiting
                && (now - lastActionTime > ActionBean.ACTION_NORMAL_TIME_OUT)
                || timeOut || (activeView != null && !activeView.getId().equals(activeAction.getViewId()));
        if (needClose) {
            if (!activeAction.isClose()) {
                activeAction.close();
                closeAction(activeAction.getId(), activeAction.getDuration(), timeOut);
            }
        }
    }

    void stopAction() {
        if (activeAction.isNeedWaitAction()) {
            activeAction.close();
        }
    }

    /**
     * resource 起始
     *
     * @param resourceId 资源 Id
     */
    public void startResource(String resourceId) {
        ResourceBean bean = new ResourceBean();
        attachRUMRelative(bean);
        resourceBeanMap.put(resourceId, bean);
        String actionId = bean.actionId;
        String viewId = bean.viewId;
        EventConsumerThreadPool.get().execute(() -> {
            FTDBManager.get().increaseViewPendingResource(viewId);
            FTDBManager.get().increaseActionPendingResource(actionId);
        });
    }

    /**
     * resource 终止
     *
     * @param resourceId 资源 Id
     */
    public void stopResource(String resourceId) {
        ResourceBean bean = resourceBeanMap.get(resourceId);
        if (bean != null) {
            String actionId = bean.actionId;
            String viewId = bean.viewId;
            increaseResourceCount(viewId, actionId);
            EventConsumerThreadPool.get().execute(() -> {
                FTDBManager.get().reduceViewPendingResource(viewId);
                FTDBManager.get().reduceActionPendingResource(actionId);
                FTTraceManager.get().removeByStopResource(resourceId);
            });
        }
    }

    public void onCreateView(String viewName, long loadTime) {
        preActivityDuration.put(viewName, loadTime);
    }


    /**
     * view 起始
     *
     * @param viewName 当前页面名称
     */
    public void startView(String viewName) {
        if (!viewList.contains(viewName)) {
            viewList.add(viewName);
            if (viewList.size() > 2) {
                viewList.remove(0);
            }
        }

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
        String viewReferrer = getLastView();
        activeView = new ViewBean(viewName, viewReferrer, loadTime, sessionId);
        initView(activeView);

    }

    /**
     * 获取上一页面
     *
     * @return
     */
    String getLastView() {
        LogUtils.d(TAG, viewList.toString());
        if (viewList.size() > 1) {
            String viewName = viewList.get(viewList.size() - 2);
            if (viewName != null) {
                return viewName;
            } else {
                return Constants.FLOW_ROOT;
            }
        } else {
            return Constants.FLOW_ROOT;
        }
    }

    /**
     * view 结束
     */
    public void stopView() {
        LogUtils.d(TAG, "stopView");
        checkActionClose();

        activeView.close();
        closeView(activeView.getId(), activeView.getTimeSpent());

    }

    private void initAction(ActionBean bean) {
        increaseAction(bean.getViewId());
        EventConsumerThreadPool.get().execute(() -> {
            FTDBManager.get().initSumAction(bean);
        });

    }

    private void initView(ViewBean bean) {
        EventConsumerThreadPool.get().execute(() -> {
            FTDBManager.get().initSumView(bean);
        });
    }

    private void increaseResourceCount(String viewId, String actionId) {
        EventConsumerThreadPool.get().execute(() -> {
            FTDBManager.get().increaseViewResource(viewId);
            FTDBManager.get().increaseActionResource(actionId);
            checkActionClose();
        });

    }


    private void increaseError(@NonNull JSONObject tags) {

        String actionId = tags.optString(Constants.KEY_RUM_ACTION_ID);
        String viewId = tags.optString(Constants.KEY_RUM_VIEW_ID);
        EventConsumerThreadPool.get().execute(() -> {
            FTDBManager.get().increaseActionError(actionId);
            FTDBManager.get().increaseViewError(viewId);
            checkActionClose();
        });
    }

    /**
     * 添加错误
     *
     * @param log       日志
     * @param message   消息
     * @param errorType 错误类型
     * @param state     程序运行状态
     */
    public void addError(String log, String message, ErrorType errorType, AppState state) {
        addError(log, message, Utils.getCurrentNanoTime(), errorType, state);
    }

    /**
     * 添加错误
     *
     * @param log       日志
     * @param message   消息
     * @param errorType 错误类型
     * @param state     程序运行状态
     * @param dateline  发生时间，纳秒
     */
    public void addError(String log, String message, long dateline, ErrorType errorType, AppState state) {
        try {
            JSONObject tags = FTRUMConfigManager.get().getRUMPublicDynamicTags();
            attachRUMRelative(tags, true);
            JSONObject fields = new JSONObject();
            tags.put(Constants.KEY_RUM_ERROR_TYPE, errorType.toString());
            tags.put(Constants.KEY_RUM_ERROR_SOURCE, ErrorSource.LOGGER.toString());
            tags.put(Constants.KEY_RUM_ERROR_SITUATION, state.toString());
            fields.put(Constants.KEY_RUM_ERROR_MESSAGE, message);
            fields.put(Constants.KEY_RUM_ERROR_STACK, log);

            try {
                tags.put(Constants.KEY_DEVICE_CARRIER, DeviceUtils.getCarrier(FTApplication.getApplication()));
                tags.put(Constants.KEY_DEVICE_LOCALE, Locale.getDefault());

                if (FTMonitorConfigManager.get().isMonitorType(MonitorType.MEMORY)) {
                    double[] memory = DeviceUtils.getRamData(FTApplication.getApplication());
                    tags.put(Constants.KEY_MEMORY_TOTAL, memory[0] + "GB");
                    fields.put(Constants.KEY_MEMORY_USE, memory[1]);
                }

                if (FTMonitorConfigManager.get().isMonitorType(MonitorType.CPU)) {
                    fields.put(Constants.KEY_CPU_USE, DeviceUtils.getCpuUseRate());
                }
                if (FTMonitorConfigManager.get().isMonitorType(MonitorType.BATTERY)) {
                    fields.put(Constants.KEY_BATTERY_USE, (float) BatteryUtils.getBatteryInfo(FTApplication.getApplication()).getBr());

                }


            } catch (JSONException e) {
                LogUtils.e(TAG, e.getMessage());
            }

            FTTrackInner.getInstance().rum(dateline, Constants.FT_MEASUREMENT_RUM_ERROR, tags, fields);
            increaseError(tags);

        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());
        }

    }

    /**
     * 添加长任务
     *
     * @param log      日志内容
     * @param duration 持续时间，纳秒
     */
    public void addLongTask(String log, long duration) {
        try {
            long time = Utils.getCurrentNanoTime();
            JSONObject tags = FTRUMConfigManager.get().getRUMPublicDynamicTags();
            attachRUMRelative(tags, true);
            JSONObject fields = new JSONObject();
            fields.put(Constants.KEY_RUM_LONG_TASK_DURATION, duration);
            fields.put(Constants.KEY_RUM_LONG_TASK_STACK, log);

            FTTrackInner.getInstance().rum(time, Constants.FT_MEASUREMENT_RUM_LONG_TASK, tags, fields);
            increaseLongTask(tags);

        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());
        }
    }

    void setNetState(String resourceId, NetStatusBean netStatusBean) {
        ResourceBean bean = resourceBeanMap.get(resourceId);

        if (bean == null) {
            return;
        }
        bean.resourceDNS = netStatusBean.getDNSTime();
        bean.resourceSSL = netStatusBean.getSSLTime();
        bean.resourceTCP = netStatusBean.getTcpTime();

        bean.resourceTrans = netStatusBean.getResponseTime();
        bean.resourceTTFB = netStatusBean.getTTFB();
        bean.resourceLoad = netStatusBean.getHoleRequestTime();
        bean.resourceFirstByte = netStatusBean.getFirstByteTime();
        bean.netStateSet = true;
        checkToAddResource(resourceId, bean);
    }

    /**
     * 资源加载性能
     */
    void putRUMResourcePerformance(String resourceId) {
        ResourceBean bean = resourceBeanMap.get(resourceId);

        if (bean == null) {
            return;
        }
        if (bean.resourceStatus < HttpsURLConnection.HTTP_OK) {
            EventConsumerThreadPool.get().execute(() -> {
                resourceBeanMap.remove(resourceId);
                FTTraceManager.get().removeByAddResource(resourceId);
            });
            return;
        }
        long time = Utils.getCurrentNanoTime();
        String actionId = bean.actionId;
        String viewId = bean.viewId;
        String actionName = bean.actionName;
        String viewName = bean.viewName;
        String viewReferrer = bean.viewReferrer;
        String sessionId = bean.sessionId;

        try {
            JSONObject tags = FTRUMConfigManager.get().getRUMPublicDynamicTags();

            tags.put(Constants.KEY_RUM_ACTION_ID, actionId);
            tags.put(Constants.KEY_RUM_ACTION_NAME, actionName);
            tags.put(Constants.KEY_RUM_VIEW_ID, viewId);
            tags.put(Constants.KEY_RUM_VIEW_NAME, viewName);
            tags.put(Constants.KEY_RUM_VIEW_REFERRER, viewReferrer);
            tags.put(Constants.KEY_RUM_SESSION_ID, sessionId);

            JSONObject fields = new JSONObject();

            tags.put(Constants.KEY_RUM_RESOURCE_URL_HOST, bean.urlHost);

            if (bean.resourceType != null && !bean.resourceType.isEmpty()) {
                tags.put(Constants.KEY_RUM_RESOURCE_TYPE, bean.resourceType);
            }
            tags.put(Constants.KEY_RUM_RESPONSE_CONNECTION, bean.responseConnection);
            tags.put(Constants.KEY_RUM_RESPONSE_CONTENT_TYPE, bean.responseContentType);
            tags.put(Constants.KEY_RUM_RESPONSE_CONTENT_ENCODING, bean.responseContentEncoding);
            tags.put(Constants.KEY_RUM_RESOURCE_METHOD, bean.resourceMethod);
            tags.put(Constants.KEY_RUM_RESOURCE_TRACE_ID, bean.traceId);
            tags.put(Constants.KEY_RUM_RESOURCE_SPAN_ID, bean.spanId);

            int resourceStatus = bean.resourceStatus;
            String resourceStatusGroup = "";
            if (resourceStatus > 0) {
                tags.put(Constants.KEY_RUM_RESOURCE_STATUS, resourceStatus);
                long statusGroupPrefix = bean.resourceStatus / 100;
                resourceStatusGroup = statusGroupPrefix + "xx";
                tags.put(Constants.KEY_RUM_RESOURCE_STATUS_GROUP, resourceStatusGroup);
            }

            if (bean.resourceSize > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_SIZE, bean.resourceSize);
            }
            if (bean.resourceLoad > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_DURATION, bean.resourceLoad);
            }

            if (bean.resourceDNS > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_DNS, bean.resourceDNS);
            }
            if (bean.resourceTCP > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_TCP, bean.resourceTCP);
            }
            if (bean.resourceSSL > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_SSL, bean.resourceSSL);
            }
            if (bean.resourceTTFB > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_TTFB, bean.resourceTTFB);
            }

            if (bean.resourceTrans > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_TRANS, bean.resourceTrans);
            }

            if (bean.resourceFirstByte > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_FIRST_BYTE, bean.resourceFirstByte);

            }
            String urlPath = bean.urlPath;
            String urlPathGroup = "";

            if (!urlPath.isEmpty()) {
                urlPathGroup = urlPath.replaceAll("\\/([^\\/]*)\\d([^\\/]*)", "/?");
                tags.put(Constants.KEY_RUM_RESOURCE_URL_PATH, urlPath);
                tags.put(Constants.KEY_RUM_RESOURCE_URL_PATH_GROUP, urlPathGroup);
            }


            tags.put(Constants.KEY_RUM_RESOURCE_URL, bean.url);
            fields.put(Constants.KEY_RUM_REQUEST_HEADER, bean.requestHeader);
            fields.put(Constants.KEY_RUM_RESPONSE_HEADER, bean.responseHeader);

            FTTrackInner.getInstance().rum(time,
                    Constants.FT_MEASUREMENT_RUM_RESOURCE, tags, fields);


            if (bean.resourceStatus >= HttpsURLConnection.HTTP_BAD_REQUEST) {
                JSONObject errorTags = FTRUMConfigManager.get().getRUMPublicDynamicTags();
                JSONObject errorField = new JSONObject();
                errorTags.put(Constants.KEY_RUM_ERROR_TYPE, ErrorType.NETWORK.toString());
                errorTags.put(Constants.KEY_RUM_ERROR_SOURCE, ErrorSource.NETWORK.toString());
                errorTags.put(Constants.KEY_RUM_ERROR_SITUATION, AppState.RUN.toString());
                errorTags.put(Constants.KEY_RUM_ACTION_ID, actionId);
                errorTags.put(Constants.KEY_RUM_ACTION_NAME, actionName);
                errorTags.put(Constants.KEY_RUM_VIEW_ID, viewId);
                errorTags.put(Constants.KEY_RUM_VIEW_NAME, viewName);
                errorTags.put(Constants.KEY_RUM_VIEW_REFERRER, viewReferrer);
                errorTags.put(Constants.KEY_RUM_SESSION_ID, sessionId);

                if (resourceStatus > 0) {
                    errorTags.put(Constants.KEY_RUM_RESOURCE_STATUS, resourceStatus);
                    errorTags.put(Constants.KEY_RUM_RESOURCE_STATUS_GROUP, resourceStatusGroup);
                }
                errorTags.put(Constants.KEY_RUM_RESOURCE_URL, bean.url);
                errorTags.put(Constants.KEY_RUM_RESOURCE_URL_HOST, bean.urlHost);
                errorTags.put(Constants.KEY_RUM_RESOURCE_METHOD, bean.resourceMethod);

                if (!urlPath.isEmpty()) {
                    errorTags.put(Constants.KEY_RUM_RESOURCE_URL_PATH, urlPath);
                    errorTags.put(Constants.KEY_RUM_RESOURCE_URL_PATH_GROUP, urlPathGroup);
                }
                String errorMsg = "[" + bean.resourceStatus + "]" + "[" + bean.url + "]";

                errorField.put(Constants.KEY_RUM_ERROR_MESSAGE, errorMsg);
                errorField.put(Constants.KEY_RUM_ERROR_STACK, bean.errorStack);

                FTTrackInner.getInstance().rum(time, Constants.FT_MEASUREMENT_RUM_ERROR, errorTags, errorField);
                increaseError(tags);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, e.toString());
        }

        EventConsumerThreadPool.get().execute(() -> {
            resourceBeanMap.remove(resourceId);
            FTTraceManager.get().removeByAddResource(resourceId);
        });
    }

    /**
     * 设置网络传输内容
     *
     * @param resourceId
     * @param params
     */
    public void addResource(String resourceId, ResourceParams params, NetStatusBean netStatusBean) {
        setTransformContent(resourceId, params);
        setNetState(resourceId, netStatusBean);
    }

    /**
     * 设置网络传输内容
     *
     * @param params
     */
    void setTransformContent(String resourceId, ResourceParams params) {

        FTTraceHandler handler = FTTraceManager.get().getHandler(resourceId);
        String spanId = "";
        String traceId = "";
        if (handler != null) {
            spanId = handler.getSpanID();
            traceId = handler.getTraceID();
        }

        ResourceBean bean = resourceBeanMap.get(resourceId);

        if (bean == null || params.resourceStatus < HttpsURLConnection.HTTP_OK) {
            return;
        }
        try {
            URL url = Utils.parseFromUrl(params.url);
            bean.url = params.url;
            bean.urlHost = url.getHost();
            bean.urlPath = url.getPath();
            bean.resourceUrlQuery = url.getQuery();

        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }

        bean.requestHeader = params.requestHeader;
        bean.responseHeader = params.responseHeader;
        int responseHeaderSize = bean.responseHeader.getBytes().length;
        bean.responseContentType = params.responseContentType;
        bean.responseConnection = params.responseConnection;
        bean.resourceMethod = params.resourceMethod;
        bean.responseContentEncoding = params.responseContentEncoding;
        bean.resourceType = bean.responseContentType;
        bean.resourceStatus = params.resourceStatus;
        if (bean.resourceStatus >= HttpsURLConnection.HTTP_BAD_REQUEST) {
            bean.errorStack = params.responseBody == null ? "" : params.responseBody;
        }

        bean.resourceSize = params.responseBody == null ? 0 : params.responseBody.getBytes().length;
        bean.resourceSize += responseHeaderSize;
        if (FTTraceConfigManager.get().isEnableLinkRUMData()) {
            bean.traceId = traceId;
            bean.spanId = spanId;
        }
        bean.contentSet = true;
        checkToAddResource(resourceId, bean);
    }


    private void increaseLongTask(@NonNull JSONObject tags) {
        String actionId = tags.optString(Constants.KEY_RUM_ACTION_ID);
        String viewId = tags.optString(Constants.KEY_RUM_VIEW_ID);
        EventConsumerThreadPool.get().execute(() -> {
            FTDBManager.get().increaseActionLongTask(actionId);
            FTDBManager.get().increaseViewLongTask(viewId);
            checkActionClose();

        });
    }

    private void increaseAction(String viewId) {
        EventConsumerThreadPool.get().execute(() -> {
            FTDBManager.get().increaseViewAction(viewId);

        });

    }


    private void closeView(String viewId, long timeSpent) {
        EventConsumerThreadPool.get().execute(() -> {
            FTDBManager.get().closeView(viewId, timeSpent);

        });
        generateRumData();
    }

    private void closeAction(String actionId, long duration, boolean force) {
        EventConsumerThreadPool.get().execute(() -> {
            FTDBManager.get().closeAction(actionId, duration, force);
        });
        generateRumData();
    }


    private String getViewId() {
        if (activeView == null) {
            return null;
        }
        return activeView.getId();
    }

    private String getViewName() {
        if (activeView == null) {
            return null;
        }
        return activeView.getViewName();
    }

    private String getViewReferrer() {
        if (activeView == null) {
            return null;
        }
        return activeView.getViewReferrer();
    }

    private String getActionName() {
        return activeAction == null ? null : activeAction.getActionName();
    }

    /**
     * 设置 RUM 全局 view session action 关联数据
     *
     * @param bean
     */
    private void attachRUMRelative(@NonNull ResourceBean bean) {
        bean.viewId = getViewId();
        bean.viewName = getViewName();
        bean.viewReferrer = getViewReferrer();
        bean.sessionId = getSessionId();
        bean.actionId = getActionId();
        bean.actionName = getActionName();
    }

    /**
     * 添加界面 RUM 相关界面属性
     *
     * @param tags
     * @param withAction
     */
    void attachRUMRelative(@NonNull JSONObject tags, boolean withAction) {
        try {
            tags.put(Constants.KEY_RUM_VIEW_ID, getViewId());

            tags.put(Constants.KEY_RUM_VIEW_NAME, getViewName());
            tags.put(Constants.KEY_RUM_VIEW_REFERRER, getViewReferrer());
            tags.put(Constants.KEY_RUM_SESSION_ID, sessionId);
            if (withAction) {
                tags.put(Constants.KEY_RUM_ACTION_ID, getActionId());
                tags.put(Constants.KEY_RUM_ACTION_NAME, getActionName());
            }
        } catch (JSONException e) {
            LogUtils.e(TAG, e.getMessage());
        }
    }


    Handler mHandler = new Handler(Looper.getMainLooper());
    Runnable mRUMGenerateRunner = () -> {
        try {
            JSONObject tags = FTRUMConfigManager.get().getRUMPublicDynamicTags();
            EventConsumerThreadPool.get().execute(() -> {
                try {
                    generateActionSum(tags);
                    generateViewSum(tags);
                } catch (JSONException e) {
                    LogUtils.e(TAG, e.getMessage());
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());

        }
    };

    private static final int LIMIT_SIZE = 50;

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
                FTTrackInner.getInstance().rum(bean.getStartTime(),
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

            FTTrackInner.getInstance().rum(bean.getStartTime(),
                    Constants.FT_MEASUREMENT_RUM_VIEW, tags, fields);


        }
        FTDBManager.get().cleanCloseViewData();
        if (beans.size() < LIMIT_SIZE) {


        } else {
            generateViewSum(globalTags);
        }
    }

    void initParams(FTRUMConfig config) {
        checkSessionKeep(sessionId, config.getSamplingRate());
        EventConsumerThreadPool.get().execute(() -> {
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
        viewList.clear();
    }

    void checkToAddResource(String key, ResourceBean bean) {
        if (bean.contentSet && bean.netStateSet) {
            putRUMResourcePerformance(key);
        }
    }

}
