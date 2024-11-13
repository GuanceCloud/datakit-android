package com.ft.sdk;

import android.os.Handler;
import android.os.Looper;

import com.ft.sdk.garble.bean.ActionBean;
import com.ft.sdk.garble.bean.ActiveActionBean;
import com.ft.sdk.garble.bean.ActiveViewBean;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.ErrorSource;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceBean;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.threadpool.EventConsumerThreadPool;
import com.ft.sdk.garble.threadpool.RunnerCompleteCallBack;
import com.ft.sdk.garble.utils.BatteryUtils;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.HashMapUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * RUM 数据管理，记录 Action，View，LongTask，Error，并统计
 * {@link Constants#KEY_RUM_VIEW_ACTION_COUNT }
 * {@link Constants#KEY_RUM_ACTION_ERROR_COUNT}
 * ，可以通过<a href="https://docs.guance.com/real-user-monitoring/explorer/">查看器</a>
 */
public class FTRUMInnerManager {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "RUMGlobalManager";
    /**
     * 间断操作（中途休眠） Session 重置事件为 15分钟
     */
    static final long MAX_RESTING_TIME = 900000000000L;
//    static final long MAX_RESTING_TIME = 1000000000L;//1秒
    /**
     * 持续 Session 最大重置事件，4小时
     */
    static final long SESSION_EXPIRE_TIME = 14400000000000L;
//    static final long SESSION_EXPIRE_TIME = 5000000000L;//5秒
    /**
     * Session 最大存储数值
     */
    static final long SESSION_FILTER_CAPACITY = 5;
    private final ConcurrentHashMap<String, ResourceBean> resourceBeanMap = new ConcurrentHashMap<>();

    private final ArrayList<String> viewList = new ArrayList<>();

    private FTRUMInnerManager() {

    }

    private static class SingletonHolder {
        private static final FTRUMInnerManager INSTANCE = new FTRUMInnerManager();
    }

    public static FTRUMInnerManager get() {
        return FTRUMInnerManager.SingletonHolder.INSTANCE;
    }

    /**
     * {@link Constants#KEY_RUM_SESSION_ID}
     */
    private String sessionId = Utils.getEmptyUUID();


    /**
     * 不收集
     */
    private final ArrayList<String> notCollectArr = new ArrayList<>();

    /**
     * 当前激活 View
     */
    private ActiveViewBean activeView;

    /**
     * 当前激活 Action
     */
    private ActiveActionBean activeAction;


    /**
     * 记录 activity 的创建时，消耗的时间
     */
    private final ConcurrentHashMap<String, Long> preActivityDuration = new ConcurrentHashMap<>();

    /**
     * 最近 Session 时间，单位纳秒
     */
    private long lastSessionTime = Utils.getCurrentNanoTime();

    /**
     * 最近 Action 时间
     * <p>
     * 注意 ：AndroidTest 会调用这个方法 {@link com.ft.test.base.FTBaseTest#setSessionExpire()}
     */
    private long lastActionTime = lastSessionTime;

    /**
     * 采样率，{@link FTRUMConfig#samplingRate}
     */
    private float sampleRate = 1f;

    String getSessionId() {
        return sessionId;
    }

    /**
     * 检测重置 session_id
     *
     * @return
     */
    private void checkSessionRefresh(boolean checkRefreshView) {
        long now = Utils.getCurrentNanoTime();
        boolean longResting = now - lastActionTime > MAX_RESTING_TIME;
        boolean longTimeSession = now - lastSessionTime > SESSION_EXPIRE_TIME;
        if (longTimeSession || longResting) {
            lastSessionTime = now;
            lastActionTime = now;

            sessionId = Utils.randomUUID();
            LogUtils.d(TAG, "New SessionId:" + sessionId);

            checkSessionKeep(sessionId, sampleRate);

            if (checkRefreshView) {
                if (activeView != null && !activeView.isClose()) {
                    activeView.close();
                    closeView(activeView);

                    String viewName = activeView.getViewName();
                    String viewReferrer = activeView.getViewReferrer();
                    HashMap<String, Object> property = activeView.getProperty();
                    long loadTime = activeView.getLoadTime();

                    activeView = new ActiveViewBean(viewName, viewReferrer, loadTime, sessionId);
                    if (property != null) {
                        activeView.getProperty().putAll(property);
                    }
                    FTMonitorManager.get().addMonitor(activeView.getId());
                    FTMonitorManager.get().attachMonitorData(activeView);
                    initView(activeView);
                    LogUtils.d(TAG, "checkRefreshView sessionId:" + activeView.getSessionId() + ",viewId:" + activeView.getId());
                }
            }

        }
    }

    private String getActionId() {
        return activeAction == null ? null : activeAction.getId();
    }

    void addAction(String actionName, String actionType, long duration, long startTime) {
        addAction(actionName, actionType, duration, startTime, null);
    }

    void addAction(String actionName, String actionType, long duration, long startTime, HashMap<String, Object> property) {
        String viewId = activeView != null ? activeView.getId() : null;
        String viewName = activeView != null ? activeView.getViewName() : null;
        String viewReferrer = activeView != null ? activeView.getViewReferrer() : null;
        checkSessionRefresh(true);

        ActiveActionBean activeAction = new ActiveActionBean(actionName, actionType,
                sessionId, viewId, viewName, viewReferrer, false);
        activeAction.setClose(true);
        activeAction.setDuration(duration);
        activeAction.setStartTime(startTime);
        if (property != null) {
            activeAction.getProperty().putAll(property);
        }
        initAction(activeAction, true);
        this.lastActionTime = activeAction.getStartTime();
    }

    /**
     * 添加 action
     *
     * @param actionName action 名称
     * @param actionType action 类型
     */
    void startAction(String actionName, String actionType) {
        startAction(actionName, actionType, false);
    }

    /**
     * 添加 action
     *
     * @param actionName action 名称
     * @param actionType action 类型
     * @param property   附加属性参数
     */
    void startAction(String actionName, String actionType, HashMap<String, Object> property) {
        startAction(actionName, actionType, false, property);
    }

    /**
     * 添加 action
     *
     * @param actionName action 名称
     * @param actionType action 类型
     * @param needWait   是否需要等待
     */
    void startAction(String actionName, String actionType, boolean needWait) {
        startAction(actionName, actionType, needWait, null);
    }

    /**
     * action 起始
     *
     * @param actionName action 名称
     * @param actionType action 类型
     * @param needWait   是否需要等待
     * @param property   附加属性参数
     */
    void startAction(String actionName, String actionType, boolean needWait, HashMap<String, Object> property) {

        String viewId = activeView != null ? activeView.getId() : null;
        String viewName = activeView != null ? activeView.getViewName() : null;
        String viewReferrer = activeView != null ? activeView.getViewReferrer() : null;
        checkSessionRefresh(true);
        checkActionClose();
        if (activeAction == null || activeAction.isClose()) {
            activeAction = new ActiveActionBean(actionName, actionType, sessionId, viewId, viewName, viewReferrer, needWait);
            if (property != null) {
                activeAction.getProperty().putAll(property);
            }
            activeAction.setTags(FTRUMConfigManager.get().getRUMPublicDynamicTags());
            initAction(activeAction, false);
            this.lastActionTime = activeAction.getStartTime();

            mHandler.removeCallbacks(mActionRecheckRunner);
            mHandler.postDelayed(mActionRecheckRunner, 5000);
        }
    }

    /**
     * 检测 action 是否需要关闭
     */
    private void checkActionClose() {
        if (activeAction == null) return;
        long now = Utils.getCurrentNanoTime();
        long lastActionTime = activeAction.getStartTime();
        boolean waiting = activeAction.isNeedWaitAction() && (activeView != null && !activeView.isClose());
        boolean timeOut = now - lastActionTime > ActiveActionBean.ACTION_NEED_WAIT_TIME_OUT;
        boolean needClose = !waiting
                && (now - lastActionTime > ActiveActionBean.ACTION_NORMAL_TIME_OUT)
                || timeOut || (activeView != null && !activeView.getId().equals(activeAction.getViewId()));
        if (needClose) {
            if (!activeAction.isClose()) {
                activeAction.close();
                closeAction(activeAction, timeOut);
            }
        }
    }

    /**
     * 停止 action
     */
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
    void startResource(String resourceId) {
        startResource(resourceId, null);
    }

    /**
     * resource 起始
     *
     * @param resourceId 资源 Id
     */
    void startResource(String resourceId, HashMap<String, Object> property) {
        LogUtils.d(TAG, "startResource:" + resourceId);
        checkSessionRefresh(true);
        ResourceBean bean = new ResourceBean();
        if (property != null) {
            bean.property.putAll(property);
        }
        attachRUMRelativeForResource(bean);
        resourceBeanMap.put(resourceId, bean);
        final String actionId = bean.actionId;
        final String viewId = bean.viewId;
        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                FTDBManager.get().increaseViewPendingResource(viewId);
                FTDBManager.get().increaseActionPendingResource(actionId);
            }
        });
    }

    /**
     * resource 终止
     *
     * @param resourceId 资源 Id
     */
    void stopResource(String resourceId) {
        LogUtils.d(TAG, "stopResource:" + resourceId);
        stopResource(resourceId, null);
    }

    /**
     * @param resourceId
     * @param property   附加属性参数
     */
    void stopResource(final String resourceId, HashMap<String, Object> property) {
        ResourceBean bean = resourceBeanMap.get(resourceId);
        if (bean != null) {
            if (property != null) {
                bean.property.putAll(property);
            }
            final String actionId = bean.actionId;
            final String viewId = bean.viewId;
            bean.endTime = Utils.getCurrentNanoTime();
            increaseResourceCount(viewId, actionId);
            EventConsumerThreadPool.get().execute(new Runnable() {
                @Override
                public void run() {
                    FTDBManager.get().reduceViewPendingResource(viewId);
                    FTDBManager.get().updateViewUpdateTime(viewId, System.currentTimeMillis());
                    FTDBManager.get().reduceActionPendingResource(actionId);
                    FTTraceManager.get().removeByStopResource(resourceId);
                }
            });
        }
    }

    /**
     * 创建 view
     *
     * @param viewName 界面名称
     * @param loadTime 加载事件，单位毫秒 ms
     */
    void onCreateView(String viewName, long loadTime) {
        preActivityDuration.put(viewName, loadTime);
    }


    /**
     * view 起始
     *
     * @param viewName 当前页面名称
     */
    void startView(String viewName) {
        startView(viewName, null);
    }

    /**
     * view 起始
     *
     * @param viewName 当前页面名称
     * @param property 附加属性参数
     */
    void startView(String viewName, HashMap<String, Object> property) {
        if (viewList.isEmpty() || !viewList.get(viewList.size() - 1).equals(viewName)) {
            viewList.add(viewName);
            if (viewList.size() > 2) {
                viewList.remove(0);
            }
        }

        checkSessionRefresh(false);
        if (activeView != null && !activeView.isClose()) {
            activeView.close();
            closeView(activeView);
        }


        long loadTime = -1;
        if (preActivityDuration.get(viewName) != null) {
            loadTime = preActivityDuration.get(viewName);
            preActivityDuration.remove(viewName);
        }
        String viewReferrer = getLastView();
        activeView = new ActiveViewBean(viewName, viewReferrer, loadTime, sessionId);
        if (property != null) {
            activeView.getProperty().putAll(property);
        }
        activeView.setTags(FTRUMConfigManager.get().getRUMPublicDynamicTags());
        FTMonitorManager.get().addMonitor(activeView.getId());
        FTMonitorManager.get().attachMonitorData(activeView);
        initView(activeView);
        lastActionTime = activeView.getStartTime();

    }

    /**
     * 获取上一页面
     *
     * @return
     */
    String getLastView() {
        LogUtils.d(TAG, "getLastView:" + viewList);
        if (viewList.size() > 1) {
            String viewName = viewList.get(viewList.size() - 2);
            if (viewName != null) {
                return viewName;
            } else {
                return Constants.VIEW_NAME_ROOT;
            }
        } else {
            return Constants.VIEW_NAME_ROOT;
        }
    }

    /**
     * view 结束
     */
    void stopView() {
        stopView(null, null);
    }

    /**
     * view 结束
     *
     * @param property 附加属性参数
     */
    void stopView(HashMap<String, Object> property, RunnerCompleteCallBack callBack) {
        if (activeView == null) {
            if (callBack != null) {
                callBack.onComplete();
            }
            return;
        }
        checkActionClose();
        if (property != null) {
            activeView.getProperty().putAll(property);
        }
        FTMonitorManager.get().attachMonitorData(activeView);
        activeView.close();
        closeView(activeView, callBack);
    }

    /**
     * 初始化 action
     * <p>
     * 这里 startAction 会先进入 {@link com.ft.sdk.garble.db.FTSQL#FT_TABLE_ACTION},
     * 再进入 {@link com.ft.sdk.garble.db.FTSQL#FT_SYNC_DATA_FLAT_TABLE_NAME};
     * addAction 会直接进入 {@link com.ft.sdk.garble.db.FTSQL#FT_SYNC_DATA_FLAT_TABLE_NAME}
     * @param activeActionBean 当前激活的操作
     */
    private void initAction(ActiveActionBean activeActionBean, boolean isAddAction) {
        final ActionBean bean = activeActionBean.convertToActionBean();
        increaseAction(bean.getViewId());
        if (isAddAction) {
            EventConsumerThreadPool.get().execute(new Runnable() {
                @Override
                public void run() {
                    insertAction(bean);
                }
            });
        } else {
            EventConsumerThreadPool.get().execute(new Runnable() {
                @Override
                public void run() {
                    FTDBManager.get().initSumAction(bean);
                }
            });
        }


    }

    /**
     * 初始化 view
     *
     * @param activeViewBean 当前激活的页面
     */
    private void initView(ActiveViewBean activeViewBean) {
        final ViewBean bean = activeViewBean.convertToViewBean();
        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                FTDBManager.get().initSumView(bean);
            }
        });
    }

    /**
     * 增加 Resource 数量
     * {@link Constants#FT_MEASUREMENT_RUM_ACTION} 数据增加 {@link Constants#KEY_RUM_ACTION_RESOURCE_COUNT}
     * {@link Constants#FT_MEASUREMENT_RUM_VIEW} 数据增加 {@link Constants#KEY_RUM_VIEW_ERROR_COUNT}
     *
     * @param viewId   view 唯一 id
     * @param actionId action 唯一 id
     */
    private void increaseResourceCount(final String viewId, final String actionId) {
        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                FTDBManager.get().increaseViewResource(viewId);
                FTDBManager.get().increaseActionResource(actionId);
                FTRUMInnerManager.this.checkActionClose();
            }
        });

    }


    /**
     * 增加 Error 数量
     * {@link Constants#FT_MEASUREMENT_RUM_ACTION} 数据增加 {@link Constants#KEY_RUM_ACTION_ERROR_COUNT}
     * {@link Constants#FT_MEASUREMENT_RUM_VIEW} 数据增加 {@link Constants#KEY_RUM_VIEW_ERROR_COUNT}
     *
     * @param tags
     */
    private void increaseError(HashMap<String, Object> tags) {

        final String actionId = HashMapUtils.getString(tags, Constants.KEY_RUM_ACTION_ID);
        final String viewId = HashMapUtils.getString(tags, Constants.KEY_RUM_VIEW_ID);
        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                FTDBManager.get().increaseActionError(actionId);
                FTDBManager.get().increaseViewError(viewId);
                FTRUMInnerManager.this.checkActionClose();
            }
        });
    }

    /**
     * 添加错误信息
     *
     * @param log       日志
     * @param message   消息
     * @param errorType 错误类型
     * @param state     程序运行状态
     */
    void addError(String log, String message, String errorType, AppState state) {
        addError(log, message, Utils.getCurrentNanoTime(), errorType, state, null);
    }


    /**
     * 添加错误信息
     *
     * @param log       日志
     * @param message   消息
     * @param errorType 错误类型
     * @param state     程序运行状态
     * @param property  附加属性
     */
    void addError(String log, String message, String errorType, AppState state, HashMap<String, Object> property) {
        addError(log, message, Utils.getCurrentNanoTime(), errorType, state, property, null);
    }

    /**
     * 添加错误
     *
     * @param log       日志
     * @param message   消息
     * @param errorType 错误类型
     * @param state     程序运行状态
     * @param dateline  发生时间，纳秒
     * @param callBack
     */
    void addError(String log, String message, long dateline, String errorType, AppState state, RunnerCompleteCallBack callBack) {
        addError(log, message, dateline, errorType, state, null, callBack);
    }

    /**
     * 添加错误
     *
     * @param log       日志
     * @param message   消息
     * @param errorType 错误类型
     * @param state     程序运行状态
     * @param dateline  发生时间，纳秒
     * @param callBack  线程池调用结束回调
     */
    public void addError(String log, String message, long dateline, String errorType,
                         AppState state, HashMap<String, Object> property, RunnerCompleteCallBack callBack) {

        try {
            checkSessionRefresh(true);

            final HashMap<String, Object> tags = FTRUMConfigManager.get().getRUMPublicDynamicTags();
            attachRUMRelative(tags, true);
            final HashMap<String, Object> fields = new HashMap<>();
            tags.put(Constants.KEY_RUM_ERROR_TYPE, errorType);
            tags.put(Constants.KEY_RUM_ERROR_SOURCE, ErrorSource.LOGGER.toString());
            tags.put(Constants.KEY_RUM_ERROR_SITUATION, state.toString());

            if (property != null) {
                fields.putAll(property);
            }

            fields.put(Constants.KEY_RUM_ERROR_MESSAGE, message);
            fields.put(Constants.KEY_RUM_ERROR_STACK, log);

            EventConsumerThreadPool.get().execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        //---- 获取设备信息，资源耗时操作----->
                        tags.put(Constants.KEY_DEVICE_LOCALE, Locale.getDefault());
                        tags.put(Constants.KEY_DEVICE_CARRIER, DeviceUtils.getCarrier(FTApplication.getApplication()));

                        if (FTMonitorManager.get().isErrorMonitorType(ErrorMonitorType.MEMORY)) {
                            double[] memory = DeviceUtils.getRamData(FTApplication.getApplication());
                            tags.put(Constants.KEY_MEMORY_TOTAL, memory[0] + "GB");
                            fields.put(Constants.KEY_MEMORY_USE, memory[1]);
                        }

                        if (FTMonitorManager.get().isErrorMonitorType(ErrorMonitorType.CPU)) {
                            fields.put(Constants.KEY_CPU_USE, DeviceUtils.getCpuUsage());
                        }
                        if (FTMonitorManager.get().isErrorMonitorType(ErrorMonitorType.BATTERY)) {
                            fields.put(Constants.KEY_BATTERY_USE, (float) BatteryUtils.getBatteryInfo(FTApplication.getApplication()).getBr());

                        }
                        //<--------
                        FTTrackInner.getInstance().rum(dateline, Constants.FT_MEASUREMENT_RUM_ERROR, tags, fields, new RunnerCompleteCallBack() {
                            @Override
                            public void onComplete() {
                                increaseError(tags);
                                if (callBack != null) {
                                    // Java Crash，Native Crash 需要记录当下状态的崩溃
                                    stopView(null, callBack);
                                }
                            }
                        });

                    } catch (Exception e) {
                        LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                        if (callBack != null) {
                            callBack.onComplete();
                        }
                    }
                }
            });

        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
            if (callBack != null) {
                callBack.onComplete();
            }
        }
    }

    /**
     * 添加长任务
     *
     * @param log      日志内容
     * @param duration 持续时间，纳秒
     */
    void addLongTask(String log, long duration, HashMap<String, Object> property) {
        try {
            checkSessionRefresh(true);
            HashMap<String, Object> tags = FTRUMConfigManager.get().getRUMPublicDynamicTags();
            attachRUMRelative(tags, true);
            HashMap<String, Object> fields = new HashMap<>();
            fields.put(Constants.KEY_RUM_LONG_TASK_DURATION, duration);
            fields.put(Constants.KEY_RUM_LONG_TASK_STACK, log);

            if (property != null) {
                fields.putAll(property);
            }

            FTTrackInner.getInstance().rum(Utils.getCurrentNanoTime() - duration, Constants.FT_MEASUREMENT_RUM_LONG_TASK, tags, fields, null);
            increaseLongTask(tags);

        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
    }

    /**
     * 添加长任务
     *
     * @param log      日志内容
     * @param duration 持续时间，纳秒
     */
    void addLongTask(String log, long duration) {
        addLongTask(log, duration, null);
    }

    /**
     * 传输网络连接指标参数
     *
     * @param resourceId
     * @param netStatusBean
     */
    void setNetState(String resourceId, NetStatusBean netStatusBean) {
        ResourceBean bean = resourceBeanMap.get(resourceId);
        if (bean == null) {
            LogUtils.e(TAG, "setNetState:" + resourceId + ",bean null");
            return;
        } else {
            LogUtils.d(TAG, "setNetState:" + resourceId);
        }
        bean.resourceDNS = netStatusBean.getDNSTime();
        bean.resourceSSL = netStatusBean.getSSLTime();
        bean.resourceTCP = netStatusBean.getTcpTime();

        bean.resourceTrans = netStatusBean.getResponseTime();
        bean.resourceTTFB = netStatusBean.getTTFB();
        long resourceLoad = netStatusBean.getHoleRequestTime();
        bean.resourceLoad = resourceLoad > 0 ? resourceLoad : bean.endTime - bean.startTime;
        bean.resourceFirstByte = netStatusBean.getFirstByteTime();
        bean.resourceHostIP = netStatusBean.resourceHostIP;
        bean.netStateSet = true;
        checkToAddResource(resourceId, bean);
    }

    /**
     * 资源加载性能
     *
     * @param resourceId 资源 id
     */
    void putRUMResourcePerformance(final String resourceId) {
        ResourceBean bean = resourceBeanMap.get(resourceId);
        if (bean == null) {
            LogUtils.e(TAG, "putRUMResourcePerformance:" + resourceId + ",bean null");
            return;
        } else {
            LogUtils.d(TAG, "putRUMResourcePerformance:" + resourceId);
        }
        long time = Utils.getCurrentNanoTime();
        String actionId = bean.actionId;
        String viewId = bean.viewId;
        String actionName = bean.actionName;
        String viewName = bean.viewName;
        String viewReferrer = bean.viewReferrer;
        String sessionId = bean.sessionId;

        try {
            HashMap<String, Object> tags = FTRUMConfigManager.get().getRUMPublicDynamicTags();

            tags.put(Constants.KEY_RUM_ACTION_ID, actionId);
            tags.put(Constants.KEY_RUM_ACTION_NAME, actionName);
            tags.put(Constants.KEY_RUM_VIEW_ID, viewId);
            tags.put(Constants.KEY_RUM_VIEW_NAME, viewName);
            tags.put(Constants.KEY_RUM_VIEW_REFERRER, viewReferrer);
            tags.put(Constants.KEY_RUM_SESSION_ID, sessionId);

            HashMap<String, Object> fields = new HashMap<>();

            tags.put(Constants.KEY_RUM_RESOURCE_URL_HOST, bean.urlHost);

            tags.put(Constants.KEY_RUM_RESOURCE_TYPE, "network");
            tags.put(Constants.KEY_RUM_RESPONSE_CONNECTION, bean.responseConnection);
            tags.put(Constants.KEY_RUM_RESPONSE_CONTENT_TYPE, bean.responseContentType);
            tags.put(Constants.KEY_RUM_RESPONSE_CONTENT_ENCODING, bean.responseContentEncoding);
            tags.put(Constants.KEY_RUM_RESOURCE_METHOD, bean.resourceMethod);
            tags.put(Constants.KEY_RUM_RESOURCE_TRACE_ID, bean.traceId);
            tags.put(Constants.KEY_RUM_RESOURCE_SPAN_ID, bean.spanId);

            int resourceStatus = bean.resourceStatus;
            String resourceStatusGroup = "";
            tags.put(Constants.KEY_RUM_RESOURCE_STATUS, resourceStatus);
            if (resourceStatus > 0) {
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

            if (!Utils.isNullOrEmpty(bean.resourceHostIP)) {
                tags.put(Constants.KEY_RUM_RESOURCE_HOST_IP, bean.resourceHostIP);
            }

            tags.put(Constants.KEY_RUM_RESOURCE_URL, bean.url);

            fields.putAll(bean.property);
            fields.put(Constants.KEY_RUM_REQUEST_HEADER, bean.requestHeader);
            fields.put(Constants.KEY_RUM_RESPONSE_HEADER, bean.responseHeader);

            FTTrackInner.getInstance().rum(time,
                    Constants.FT_MEASUREMENT_RUM_RESOURCE, tags, fields, null);


            if (bean.resourceStatus >= HttpsURLConnection.HTTP_BAD_REQUEST
                    || (bean.resourceStatus == 0 && !Utils.isNullOrEmpty(bean.errorStack))) {
                HashMap<String, Object> errorTags = FTRUMConfigManager.get().getRUMPublicDynamicTags();
                HashMap<String, Object> errorField = new HashMap<>();
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
                String localErrorMsg = Utils.isNullOrEmpty(bean.errorMsg) ? "" : ":" + bean.errorMsg;
                String errorMsg = "[" + bean.resourceStatus + localErrorMsg + "]" + "[" + bean.url + "]";

                errorField.put(Constants.KEY_RUM_ERROR_MESSAGE, errorMsg);
                errorField.put(Constants.KEY_RUM_ERROR_STACK, bean.errorStack);

                FTTrackInner.getInstance().rum(time, Constants.FT_MEASUREMENT_RUM_ERROR, errorTags, errorField, new RunnerCompleteCallBack() {
                    @Override
                    public void onComplete() {
                        increaseError(tags);
                    }
                });
            }
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }

        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                LogUtils.d(TAG, "final remove id:" + resourceId);
                resourceBeanMap.remove(resourceId);
                FTTraceManager.get().removeByAddResource(resourceId);
            }
        });
    }

    /**
     * 设置网络传输内容
     *
     * @param resourceId    资源 id
     * @param params
     * @param netStatusBean
     */
    public void addResource(String resourceId, ResourceParams params, NetStatusBean netStatusBean) {
        setTransformContent(resourceId, params);
        setNetState(resourceId, netStatusBean);
    }

    /**
     * 设置网络传输内容
     *
     * @param resourceId 资源 id
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

        if (bean == null) {
            LogUtils.d(TAG, "setTransformContent bean null");
            return;
        }

//        if (params.resourceStatus < HttpsURLConnection.HTTP_OK) {
//            LogUtils.d(TAG, "setTransformContent code < 200");
//            return;
//        }
        try {
            URL url = Utils.parseFromUrl(params.url);
            bean.url = params.url;
            bean.urlHost = url.getHost();
            bean.urlPath = url.getPath();
            bean.resourceUrlQuery = url.getQuery();

        } catch (MalformedURLException | URISyntaxException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }

        bean.requestHeader = params.requestHeader;
        bean.responseHeader = params.responseHeader;
        bean.responseContentType = params.responseContentType;
        bean.responseConnection = params.responseConnection;
        bean.resourceMethod = params.resourceMethod;
        bean.responseContentEncoding = params.responseContentEncoding;
        bean.resourceType = bean.responseContentType;
        bean.resourceStatus = params.resourceStatus;
        bean.resourceSize = params.responseContentLength;
        if (bean.resourceStatus >= HttpsURLConnection.HTTP_BAD_REQUEST) {
            bean.errorStack = params.responseBody == null ? "" : params.responseBody;
        } else if (bean.resourceStatus == 0 && !Utils.isNullOrEmpty(params.requestErrorStack)) {
            bean.errorStack = params.requestErrorStack;
            bean.errorMsg = params.requestErrorMsg;
        }

        if (params.property != null) {
            bean.property.putAll(params.property);
        }

        if (FTTraceConfigManager.get().isEnableLinkRUMData()) {
            bean.traceId = traceId;
            bean.spanId = spanId;
        }
        bean.contentSet = true;
        checkToAddResource(resourceId, bean);
    }


    /**
     * LongTask 自增
     * {@link Constants#KEY_RUM_ACTION_LONG_TASK_COUNT}
     * {@link Constants#KEY_RUM_VIEW_LONG_TASK_COUNT}
     *
     * @param tags
     */
    private void increaseLongTask(HashMap<String, Object> tags) {
        final String actionId = HashMapUtils.getString(tags, Constants.KEY_RUM_ACTION_ID);
        final String viewId = HashMapUtils.getString(tags, Constants.KEY_RUM_VIEW_ID);
        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                FTDBManager.get().increaseActionLongTask(actionId);
                FTDBManager.get().increaseViewLongTask(viewId);
                FTRUMInnerManager.this.checkActionClose();

            }
        });
    }

    /**
     * action 数量自增
     * {@link Constants#KEY_RUM_VIEW_ACTION_COUNT}
     *
     * @param viewId
     */
    private void increaseAction(final String viewId) {
        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                FTDBManager.get().increaseViewAction(viewId);

            }
        });

    }

    private void closeView(ActiveViewBean activeViewBean) {
        closeView(activeViewBean, null);
    }

    /**
     * 关闭 View，计算 {@link ViewBean#timeSpent},{@link ViewBean#isClose} 为 true，并更新
     * {@link FTSQL#RUM_DATA_UPDATE_TIME}
     *
     * @param activeViewBean
     * @param callBack
     */
    private void closeView(ActiveViewBean activeViewBean, RunnerCompleteCallBack callBack) {
        FTMonitorManager.get().removeMonitor(activeViewBean.getId());
        final ViewBean viewBean = activeViewBean.convertToViewBean();
        final String viewId = viewBean.getId();
        final long timeSpent = viewBean.getTimeSpent();
        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                FTDBManager.get().closeView(viewId, timeSpent, viewBean.getAttrJsonString());
                FTDBManager.get().updateViewUpdateTime(viewId, System.currentTimeMillis());
                if (callBack != null) {
                    callBack.onComplete();
                }
            }
        });
        generateRumData();
    }

    /**
     * 关闭 Action，计算 {@link ActionBean#duration},{@link ActionBean#isClose} 为 true
     *
     * @param bean
     * @param force 强制关闭
     */
    private void closeAction(ActiveActionBean bean, final boolean force) {
        final String actionId = bean.getId();
        final long duration = bean.getDuration();
        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                FTDBManager.get().closeAction(actionId, duration, force);
            }
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
    private void attachRUMRelativeForResource(ResourceBean bean) {
        bean.viewId = getViewId();
        bean.viewName = getViewName();
        bean.viewReferrer = getViewReferrer();
        bean.sessionId = getSessionId();
        if (activeAction != null && !activeAction.isClose()) {
            bean.actionId = getActionId();
            bean.actionName = getActionName();
        }
    }

    /**
     * 添加界面 RUM 相关界面属性
     *
     * @param tags
     * @param withAction
     */
    void attachRUMRelative(HashMap<String, Object> tags, boolean withAction) {
        tags.put(Constants.KEY_RUM_VIEW_ID, getViewId());

        tags.put(Constants.KEY_RUM_VIEW_NAME, getViewName());
        tags.put(Constants.KEY_RUM_VIEW_REFERRER, getViewReferrer());
        tags.put(Constants.KEY_RUM_SESSION_ID, sessionId);
        if (withAction) {
            if (activeAction != null && !activeAction.isClose()) {
                tags.put(Constants.KEY_RUM_ACTION_ID, getActionId());
                tags.put(Constants.KEY_RUM_ACTION_NAME, getActionName());
            }
        }

    }


    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mRUMGenerateRunner = new Runnable() {
        @Override
        public void run() {
            try {
                EventConsumerThreadPool.get().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FTRUMInnerManager.this.generateActionSum();
                            FTRUMInnerManager.this.generateViewSum();
                        } catch (JSONException e) {
                            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                        }
                    }
                });
            } catch (Exception e) {
                LogUtils.e(TAG, LogUtils.getStackTraceString(e));

            }
        }
    };

    private final Runnable mActionRecheckRunner = new Runnable() {
        @Override
        public void run() {
            FTRUMInnerManager.this.checkActionClose();
        }
    };


    private static final int LIMIT_SIZE = 50;

    private void generateRumData() {
        //避免过于频繁的刷新
        mHandler.removeCallbacks(mRUMGenerateRunner);
        mHandler.postDelayed(mRUMGenerateRunner, 100);
    }

    /**
     * @throws JSONException
     */
    private void generateActionSum() throws JSONException {
        ArrayList<ActionBean> beans;
        do {

            beans = FTDBManager.get().querySumAction(LIMIT_SIZE);
            ArrayList<String> deleteIds = new ArrayList<>();
            for (ActionBean bean : beans) {
                insertAction(bean);
                deleteIds.add(bean.getId());
            }
            FTDBManager.get().cleanCloseActionData(deleteIds.toArray(new String[0]));
        } while (beans.size() >= LIMIT_SIZE);
    }

    private void insertAction(ActionBean bean) {
        HashMap<String, Object> tags = bean.getTags();
        tags.put(Constants.KEY_RUM_VIEW_NAME, bean.getViewName());
        tags.put(Constants.KEY_RUM_VIEW_REFERRER, bean.getViewReferrer());
        tags.put(Constants.KEY_RUM_VIEW_ID, bean.getViewId());
        tags.put(Constants.KEY_RUM_ACTION_NAME, bean.getActionName());
        tags.put(Constants.KEY_RUM_ACTION_ID, bean.getId());
        tags.put(Constants.KEY_RUM_ACTION_TYPE, bean.getActionType());
        tags.put(Constants.KEY_RUM_SESSION_ID, bean.getSessionId());

        HashMap<String, Object> fields = new HashMap<>(bean.getProperty());
        fields.put(Constants.KEY_RUM_ACTION_LONG_TASK_COUNT, bean.getLongTaskCount());
        fields.put(Constants.KEY_RUM_ACTION_RESOURCE_COUNT, bean.getResourceCount());
        fields.put(Constants.KEY_RUM_ACTION_ERROR_COUNT, bean.getErrorCount());
        fields.put(Constants.KEY_RUM_ACTION_DURATION, bean.getDuration());

        FTTrackInner.getInstance().rum(bean.getStartTime(),
                Constants.FT_MEASUREMENT_RUM_ACTION, tags, fields, null);
    }

    private void generateViewSum() throws JSONException {
        ArrayList<ViewBean> beans;
        do {
            beans = FTDBManager.get().querySumView(LIMIT_SIZE);
            for (ViewBean bean : beans) {
                HashMap<String, Object> tags = bean.getTags();
                tags.put(Constants.KEY_RUM_SESSION_ID, bean.getSessionId());
                tags.put(Constants.KEY_RUM_VIEW_NAME, bean.getViewName());
                tags.put(Constants.KEY_RUM_VIEW_REFERRER, bean.getViewReferrer());
                tags.put(Constants.KEY_RUM_VIEW_ID, bean.getId());

                HashMap<String, Object> fields = new HashMap<>(bean.getProperty());
                if (bean.getLoadTime() > 0) {
                    fields.put(Constants.KEY_RUM_VIEW_LOAD, bean.getLoadTime());
                }
                fields.put(Constants.KEY_RUM_VIEW_ACTION_COUNT, bean.getActionCount());
                fields.put(Constants.KEY_RUM_VIEW_RESOURCE_COUNT, bean.getResourceCount());
                fields.put(Constants.KEY_RUM_VIEW_ERROR_COUNT, bean.getErrorCount());
                if (bean.isClose()) {
                    fields.put(Constants.KEY_RUM_VIEW_TIME_SPENT, bean.getTimeSpent());
                } else {
                    fields.put(Constants.KEY_RUM_VIEW_TIME_SPENT, Utils.getCurrentNanoTime() - bean.getStartTime());
                }
                fields.put(Constants.KEY_RUM_VIEW_LONG_TASK_COUNT, bean.getLongTaskCount());
                fields.put(Constants.KEY_RUM_VIEW_IS_ACTIVE, !bean.isClose());
                fields.put(Constants.KEY_SDK_VIEW_UPDATE_TIME, bean.getViewUpdateTime());

                if (FTMonitorManager.get().isDeviceMetricsMonitorType(DeviceMetricsMonitorType.CPU)) {
                    double cpuTickCountPerSecond = bean.getCpuTickCountPerSecond();
                    long cpuTickCount = bean.getCpuTickCount();
                    if (cpuTickCountPerSecond > -1) {
                        fields.put(Constants.KEY_CPU_TICK_COUNT_PER_SECOND, cpuTickCountPerSecond);
                    }
                    if (cpuTickCount > -1) {
                        fields.put(Constants.KEY_CPU_TICK_COUNT, cpuTickCount);
                    }
                }
                if (FTMonitorManager.get().isDeviceMetricsMonitorType(DeviceMetricsMonitorType.MEMORY)) {
                    fields.put(Constants.KEY_MEMORY_MAX, bean.getMemoryMax());
                    fields.put(Constants.KEY_MEMORY_AVG, bean.getMemoryAvg());
                }
                if (FTMonitorManager.get().isDeviceMetricsMonitorType(DeviceMetricsMonitorType.BATTERY)) {
                    fields.put(Constants.KEY_BATTERY_CURRENT_AVG, bean.getBatteryCurrentAvg());
                    fields.put(Constants.KEY_BATTERY_CURRENT_MAX, bean.getBatteryCurrentMax());
                }
                if (FTMonitorManager.get().isDeviceMetricsMonitorType(DeviceMetricsMonitorType.FPS)) {
                    fields.put(Constants.KEY_FPS_AVG, bean.getFpsAvg());
                    fields.put(Constants.KEY_FPS_MINI, bean.getFpsMini());
                }


                FTTrackInner.getInstance().rum(bean.getStartTime(),
                        Constants.FT_MEASUREMENT_RUM_VIEW, tags, fields, new RunnerCompleteCallBack() {
                            @Override
                            public void onComplete() {
                                FTDBManager.get().updateViewUploadTime(bean.getId(), System.currentTimeMillis());
                            }
                        });
            }

            FTDBManager.get().cleanCloseViewData();
        } while (beans.size() >= LIMIT_SIZE);
    }

    void initParams(FTRUMConfig config) {
        sampleRate = config.getSamplingRate();
        sessionId = Utils.randomUUID();
        checkSessionKeep(sessionId, sampleRate);
        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                FTDBManager.get().closeAllActionAndView();
            }
        });

    }

    /**
     * 根据采样率确认这个 session_id 是否需要被采集
     *
     * @param sessionId
     * @param sampleRate
     */
    private void checkSessionKeep(String sessionId, float sampleRate) {
        boolean collect = Utils.enableTraceSamplingRate(sampleRate);
        if (!collect) {
            synchronized (notCollectArr) {
                if (notCollectArr.size() + 1 > SESSION_FILTER_CAPACITY) {
                    try {
                        notCollectArr.remove(0);
                    } catch (Exception e) {
                        LogUtils.d(TAG, LogUtils.getStackTraceString(e));
                    }
                }
                notCollectArr.add(sessionId);
            }
            LogUtils.d(TAG, "根据 FTRUMConfig SampleRate 采样率计算，当前 session 不被采集，session_id:" + sessionId);
        }
    }

    /**
     * 确认 session 是否需要被采集
     *
     * @param sessionId
     * @return
     */
    public boolean checkSessionWillCollect(String sessionId) {
        return !notCollectArr.contains(sessionId);
    }

    public void release() {
        mHandler.removeCallbacks(mRUMGenerateRunner);
        activeAction = null;
        activeView = null;
        notCollectArr.clear();
        resourceBeanMap.clear();
        viewList.clear();
    }

    /**
     * 检测追加 AddResource 数据时间
     *
     * @param key
     * @param bean
     */
    void checkToAddResource(String key, ResourceBean bean) {
        //LogUtils.d(TAG, "checkToAddResource:" + key + ",header：" + bean.requestHeader + "," + bean.url);
        if (bean.contentSet && bean.netStateSet) {
            putRUMResourcePerformance(key);
        }
    }

}
