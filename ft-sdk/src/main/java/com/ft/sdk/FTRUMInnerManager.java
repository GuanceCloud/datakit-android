package com.ft.sdk;


import android.os.Handler;
import android.os.Looper;

import com.ft.sdk.garble.bean.ActionBean;
import com.ft.sdk.garble.bean.ActiveActionBean;
import com.ft.sdk.garble.bean.ActiveViewBean;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.bean.ErrorSource;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceBean;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.bean.ResourceType;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * RUM data management, records Action, View, LongTask, Error, and statistics
 * {@link Constants#KEY_RUM_VIEW_ACTION_COUNT }
 * {@link Constants#KEY_RUM_ACTION_ERROR_COUNT}
 * , can be viewed through the <a href="https://docs.guance.com/real-user-monitoring/explorer/">Explorer</a>
 */
public class FTRUMInnerManager {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "RUMGlobalManager";
    /**
     * Session reset event for intermittent operation (sleep in the middle) is 15 minutes
     */
    static final long MAX_RESTING_TIME = 900000000000L;
//    static final long MAX_RESTING_TIME = 1000000000L;//1 Second
    /**
     * Maximum reset event of continuous Session, 4 hours
     */
    static final long SESSION_EXPIRE_TIME = 14400000000000L;
//    static final long SESSION_EXPIRE_TIME = 5000000000L;//5 Seconds
    /**
     * Maximum storage value of Session
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
     * Do not collect
     */
    private final LinkedList<String> notCollectArr = new LinkedList<>();


    /**
     * Do not collect Error Session id
     */
    private final LinkedList<String> notSessionErrorCollectArr = new LinkedList<>();

    /**
     * Current active View
     */
    private ActiveViewBean activeView;

    /**
     * Current active Action
     */
    private ActiveActionBean activeAction;


    /**
     * Record the time consumed when the activity is created
     */
    private final ConcurrentHashMap<String, Long> preActivityDuration = new ConcurrentHashMap<>();

    /**
     * Recent Session time, unit nanosecond
     */
    private long lastSessionTime = System.nanoTime();


    /**
     * Recent Action time
     * <p>
     * Note: AndroidTest will call this method {@link com.ft.test.base.FTBaseTest#setSessionExpire()}
     */
    private long lastUserActiveTime = lastSessionTime;

    /**
     * Sampling rate, {@link FTRUMConfig#samplingRate}
     */
    private float sampleRate = 1f;


    /**
     * Error session sampling rate
     */
    private float sessionErrorSampleError = 1f;

    String getSessionId() {
        return sessionId;
    }

    private final Object sessionRefreshLock = new Object();

    /**
     * Check reset session_id
     *
     */
    private void checkSessionRefresh(boolean checkRefreshView) {
        synchronized (sessionRefreshLock) {
            long now = System.nanoTime();
            boolean longResting = now - lastUserActiveTime > MAX_RESTING_TIME;
            boolean longTimeSession = now - lastSessionTime > SESSION_EXPIRE_TIME;
            if (longTimeSession || longResting) {
                lastSessionTime = now;
                lastUserActiveTime = now;
                sessionId = Utils.randomUUID();
                LogUtils.d(TAG, "Session Track -> New SessionId:" + sessionId + ",longTimeSessionReset:" + longTimeSession);
                checkSessionKeep(sessionId, sampleRate, sessionErrorSampleError);

                if (checkRefreshView) {
                    ActiveViewBean viewBean = activeView;
                    if (viewBean != null) {
                        if (!viewBean.isClose()) {
                            viewBean.close();
                            closeView(viewBean);
                        }

                        String viewName = viewBean.getViewName();
                        String viewReferrer = viewBean.getViewReferrer();
                        HashMap<String, Object> property = viewBean.getProperty();
                        long loadTime = viewBean.getLoadTime();

                        viewBean = new ActiveViewBean(viewName, viewReferrer, loadTime, sessionId);
                        activeView = viewBean;
                        if (property != null) {
                            viewBean.getProperty().putAll(property);
                        }
                        viewBean.setCollectType(checkSessionWillCollect(sessionId));
                        FTMonitorManager.get().addMonitor(viewBean.getId());
                        FTMonitorManager.get().attachMonitorData(viewBean);
                        initView(viewBean);
                        LogUtils.d(TAG, "Session Track -> checkRefreshView sessionId:" + viewBean.getSessionId() + ",viewId:" + viewBean.getId());
                    }
                }
            } else {
                lastUserActiveTime = now;
            }
        }
//        boolean isAppForward = FTApplication.isAppForward;
//        if (isAppForward) {
        //As long as there is RUM data collection activity, the user time will be extended, including background
//        }
    }


    void hotUpdate(Float sampleRate, Float sessionErrorSampleError) {
        if (sampleRate == null && sessionErrorSampleError == null) return;

        if (sampleRate != null) {
            this.sampleRate = sampleRate;
        }
        if (sessionErrorSampleError != null) {
            this.sessionErrorSampleError = sessionErrorSampleError;
        }

        boolean needRefresh = false;
        CollectType collectType = checkSessionWillCollect(sessionId);
        if (collectType.equals(CollectType.NOT_COLLECT)) {
            if (sampleRate != null) {
                if (sampleRate == 1f) {
                    needRefresh = true;
                }
            }
            if (sessionErrorSampleError != null) {
                if (sessionErrorSampleError == 1f) {
                    needRefresh = true;
                }
            }
        } else if (collectType.equals(CollectType.COLLECT_BY_SAMPLE)) {
            if (sampleRate != null) {
                if (sampleRate == 0f) {
                    needRefresh = true;
                }
            }
        } else if (collectType.equals(CollectType.COLLECT_BY_ERROR_SAMPLE)) {
            if (sampleRate != null) {
                if (sampleRate == 1f || sampleRate == 0f) {
                    needRefresh = true;
                }
            }
            if (sessionErrorSampleError != null) {
                if (sessionErrorSampleError == 0f) {
                    needRefresh = true;
                }
            }
        }

        if (needRefresh) {
            forceRefreshSessionId();
        }
    }

    void forceRefreshSessionId() {
        lastSessionTime = -1;
        lastUserActiveTime = -1;
        checkSessionRefresh(true);
    }

    private String getActionId() {
        ActiveActionBean actionBean = activeAction;
        return actionBean == null ? null : actionBean.getId();
    }

    void addAction(String actionName, String actionType, long duration, long startTime) {
        addAction(actionName, actionType, duration, startTime, null);
    }

    void addAction(String actionName, String actionType, long duration, long startTime, HashMap<String, Object> property) {
        ActiveViewBean viewBean = activeView;
        String viewId = viewBean != null ? viewBean.getId() : null;
        String viewName = viewBean != null ? viewBean.getViewName() : null;
        String viewReferrer = viewBean != null ? viewBean.getViewReferrer() : null;
        checkSessionRefresh(true);

        ActiveActionBean activeAction = new ActiveActionBean(actionName, actionType,
                sessionId, viewId, viewName, viewReferrer, false);
        activeAction.setClose(true);
        activeAction.setTags(FTRUMConfigManager.get().getRUMPublicDynamicTags());
        activeAction.setDuration(duration);
        activeAction.setStartTime(startTime);
        if (property != null) {
            activeAction.getProperty().putAll(property);
        }
        activeAction.setCollectType(checkSessionWillCollect(activeAction.getSessionId()));
        initAction(activeAction, true);
//        this.lastUserActiveTime = activeAction.getStartTime();
    }

    /**
     * Add action
     *
     * @param actionName action name
     * @param actionType action type
     */
    void startAction(String actionName, String actionType) {
        startAction(actionName, actionType, false);
    }

    /**
     * Add action
     *
     * @param actionName action name
     * @param actionType action type
     * @param property   additional attribute parameters
     */
    void startAction(String actionName, String actionType, HashMap<String, Object> property) {
        startAction(actionName, actionType, false, property);
    }

    /**
     * Add action
     *
     * @param actionName action name
     * @param actionType action type
     * @param needWait   whether to wait
     */
    void startAction(String actionName, String actionType, boolean needWait) {
        startAction(actionName, actionType, needWait, null);
    }

    /**
     * Start action
     *
     * @param actionName action name
     * @param actionType action type
     * @param needWait   whether to wait
     * @param property   additional attribute parameters
     */
    void startAction(String actionName, String actionType, boolean needWait, HashMap<String, Object> property) {
        ActiveActionBean actionBean = activeAction;
        ActiveViewBean viewBean = activeView;
        String viewId = viewBean != null ? viewBean.getId() : null;
        String viewName = viewBean != null ? viewBean.getViewName() : null;
        String viewReferrer = viewBean != null ? viewBean.getViewReferrer() : null;
        checkSessionRefresh(true);
        checkActionClose();
        if (actionBean == null || actionBean.isClose()) {
            actionBean = new ActiveActionBean(actionName, actionType, sessionId, viewId, viewName, viewReferrer, needWait);
            activeAction = actionBean;
            if (property != null) {
                actionBean.getProperty().putAll(property);
            }
            actionBean.setTags(FTRUMConfigManager.get().getRUMPublicDynamicTags());
            actionBean.setCollectType(checkSessionWillCollect(actionBean.getSessionId()));
            initAction(actionBean, false);
//            this.lastUserActiveTime = activeAction.getStartTime();

            mHandler.removeCallbacks(mActionRecheckRunner);
            mHandler.postDelayed(mActionRecheckRunner, 5000);
        }
    }

    /**
     * Check if action needs to be closed
     */
    private void checkActionClose() {
        ActiveActionBean actionBean = activeAction;
        if (actionBean == null) return;
        ActiveViewBean viewBean = activeView;
        long now = System.nanoTime();
        long lastActionTime = actionBean.getStartTimeNanoForDuration();
        boolean waiting = actionBean.isNeedWaitAction() && (viewBean != null && !viewBean.isClose());
        boolean timeOut = now - lastActionTime > ActiveActionBean.ACTION_NEED_WAIT_TIME_OUT;
        boolean needClose = !waiting
                && (now - lastActionTime > ActiveActionBean.ACTION_NORMAL_TIME_OUT)
                || timeOut || (viewBean != null && !viewBean.getId().equals(actionBean.getViewId()));
        if (needClose) {
            if (!actionBean.isClose()) {
                actionBean.close();
                closeAction(actionBean, timeOut);
            }
        }
    }

    /**
     * Stop action
     */
    void stopAction() {
        ActiveActionBean actionBean = activeAction;
        if (actionBean == null) return;
        if (actionBean.isNeedWaitAction()) {
            actionBean.close();
        }
    }

    /**
     * create resource relative map
     *
     * @param resourceId resource unique id
     * @return
     */
    ResourceBean onCreateResource(String resourceId) {
        ResourceBean bean = resourceBeanMap.get(resourceId);
        if (bean == null) {
            bean = new ResourceBean();
            resourceBeanMap.put(resourceId, bean);
        }
        return bean;
    }

    /**
     * resource start
     *
     * @param resourceId resource Id
     */
    void startResource(String resourceId) {
        startResource(resourceId, null);
    }

    /**
     * resource start
     *
     * @param resourceId resource Id
     */
    void startResource(String resourceId, HashMap<String, Object> property) {
        checkSessionRefresh(true);
        ResourceBean bean = onCreateResource(resourceId);
        LogUtils.d(TAG, "startResource:" + resourceId);
        if (property != null) {
            bean.property.putAll(property);
        }
        attachRUMRelativeForResource(bean);
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
     * resource end
     *
     * @param resourceId resource Id
     */
    void stopResource(String resourceId) {
        LogUtils.d(TAG, "stopResource:" + resourceId);
        stopResource(resourceId, null);
    }

    /**
     * @param resourceId resource unique uuid
     * @param property   additional attribute parameters
     */
    void stopResource(final String resourceId, HashMap<String, Object> property) {
        ResourceBean bean = resourceBeanMap.get(resourceId);
        if (bean != null) {
            if (property != null) {
                bean.property.putAll(property);
            }
            final String actionId = bean.actionId;
            final String viewId = bean.viewId;
            bean.endTimeNanoForDuration = System.nanoTime();
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
            generateRumData();
        }
    }

    /**
     * Create view
     *
     * @param viewName view name
     * @param loadTime load time, nanosecond
     */
    void onCreateView(String viewName, long loadTime) {
        preActivityDuration.put(viewName, loadTime);
    }

    /**
     * update current view
     */
    void updateLoadTime(long duration) {
        if (activeView != null) {
            activeView.setLoadTime(duration);
        } else {
            LogUtils.e(TAG, "updateLoadTime activeView null");
        }
    }

    /**
     * view start
     *
     * @param viewName current page name
     */
    void startView(String viewName) {
        startView(viewName, null);
    }

    /**
     * view start
     *
     * @param viewName current page name
     * @param property additional attribute parameters
     */
    void startView(String viewName, HashMap<String, Object> property) {
        if (viewList.isEmpty() || !viewList.get(viewList.size() - 1).equals(viewName)) {
            viewList.add(viewName);
            if (viewList.size() > 2) {
                viewList.remove(0);
            }
        }

        checkSessionRefresh(false);
        ActiveViewBean activeViewBean = activeView;
        if (activeViewBean != null && !activeViewBean.isClose()) {
            activeViewBean.close();
            closeView(activeViewBean);
        }


        long loadTime = -1;
        Long preDuration = preActivityDuration.get(viewName);
        if (preDuration != null) {
            loadTime = preDuration;
            preActivityDuration.remove(viewName);
        }
        String viewReferrer = getLastView();
        activeViewBean = new ActiveViewBean(viewName, viewReferrer, loadTime, sessionId);
        activeView = activeViewBean;
        if (property != null) {
            activeViewBean.getProperty().putAll(property);
        }
        activeViewBean.setTags(FTRUMConfigManager.get().getRUMPublicDynamicTags());
        activeViewBean.setCollectType(checkSessionWillCollect(activeViewBean.getSessionId()));
        FTMonitorManager.get().addMonitor(activeViewBean.getId());
        FTMonitorManager.get().attachMonitorData(activeViewBean);
        initView(activeViewBean);
//        lastUserActiveTime = activeView.getStartTime();

    }

    /**
     * Get the previous page
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
     * view end
     */
    void stopView() {
        stopView(null, null);
    }

    /**
     * view end
     *
     * @param property additional attribute parameters
     */
    void stopView(HashMap<String, Object> property, RunnerCompleteCallBack callBack) {
        ActiveViewBean activeViewBean = activeView;
        if (activeViewBean == null) {
            if (callBack != null) {
                callBack.onComplete();
            }
            return;
        }
        checkActionClose();
        if (property != null) {
            activeViewBean.getProperty().putAll(property);
        }
        FTMonitorManager.get().attachMonitorData(activeViewBean);
        activeViewBean.setTags(FTRUMConfigManager.get().getRUMPublicDynamicTags());
        activeViewBean.close();
        closeView(activeViewBean, callBack);
    }

    /**
     * Initialize action
     * <p>
     * Here startAction will first enter {@link com.ft.sdk.garble.db.FTSQL#FT_TABLE_ACTION},
     * then enter {@link com.ft.sdk.garble.db.FTSQL#FT_SYNC_DATA_FLAT_TABLE_NAME};
     * addAction will directly enter {@link com.ft.sdk.garble.db.FTSQL#FT_SYNC_DATA_FLAT_TABLE_NAME}
     *
     * @param activeActionBean current active operation
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
     * Initialize view
     *
     * @param activeViewBean current active page
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
     * Increase Resource quantity
     * {@link Constants#FT_MEASUREMENT_RUM_ACTION} data increase {@link Constants#KEY_RUM_ACTION_RESOURCE_COUNT}
     * {@link Constants#FT_MEASUREMENT_RUM_VIEW} data increase {@link Constants#KEY_RUM_VIEW_ERROR_COUNT}
     *
     * @param viewId   view unique id
     * @param actionId action unique id
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
     * Increase Error quantity
     * {@link Constants#FT_MEASUREMENT_RUM_ACTION} data increase {@link Constants#KEY_RUM_ACTION_ERROR_COUNT}
     * {@link Constants#FT_MEASUREMENT_RUM_VIEW} data increase {@link Constants#KEY_RUM_VIEW_ERROR_COUNT}
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
     * Add error information
     *
     * @param log       log
     * @param message   message
     * @param errorType error type
     * @param state     program running state
     */
    void addError(String log, String message, String errorType, AppState state) {
        addError(log, message, Utils.getCurrentNanoTime(), errorType, state, null);
    }


    /**
     * Add error information
     *
     * @param log       log
     * @param message   message
     * @param errorType error type
     * @param state     program running state
     * @param property  additional attribute parameters
     */
    void addError(String log, String message, String errorType, AppState state, HashMap<String, Object> property) {
        addError(log, message, Utils.getCurrentNanoTime(), errorType, state, property, null);
    }

    /**
     * Add error
     *
     * @param log       log
     * @param message   message
     * @param errorType error type
     * @param state     program running state
     * @param dateline  occurrence time, nanosecond
     * @param callBack  thread pool call back
     */
    void addError(String log, String message, long dateline, String errorType, AppState state, RunnerCompleteCallBack callBack) {
        addError(log, message, dateline, errorType, state, null, callBack);
    }

    /**
     * Add error
     *
     * @param log       log
     * @param message   message
     * @param errorType error type
     * @param state     program running state
     * @param dateline  occurrence time, nanosecond
     * @param callBack  thread pool call back
     */
    public void addError(String log, String message, long dateline, String errorType,
                         AppState state, HashMap<String, Object> property, RunnerCompleteCallBack callBack) {

        try {
            checkSessionRefresh(true);
            CollectType collectType = checkSessionWillCollect(sessionId);

            if (property == null || property.get(FTExceptionHandler.IS_PRE_CRASH) != (Boolean) true) {
                //Exclude the scenario of reporting the last native crash error
                SyncTaskManager.get().setErrorTimeLine(dateline, activeView);
            }
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

                        //---- Get device information, resource time-consuming operation ----->
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
                        FTTrackInner.getInstance().rum(dateline, Constants.FT_MEASUREMENT_RUM_ERROR, tags, fields,
                                new RunnerCompleteCallBack() {
                                    @Override
                                    public void onComplete() {
                                        increaseError(tags);
                                        if (callBack != null) {
                                            // Java Crash, Native Crash need to record the crash status of the current state
                                            stopView(null, callBack);
                                        }
                                    }
                                }, collectType);

                    } catch (Exception e) {
                        LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                        if (callBack != null) {
                            callBack.onComplete();
                        }
                    }
                }
            });
            generateViewSum();

        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
            if (callBack != null) {
                callBack.onComplete();
            }
        }
    }

    /**
     * Add long task
     *
     * @param log      log
     * @param duration duration, nanosecond
     */
    void addLongTask(String log, long duration, HashMap<String, Object> property) {
        try {
            checkSessionRefresh(true);
            CollectType type = checkSessionWillCollect(sessionId);
            HashMap<String, Object> tags = FTRUMConfigManager.get().getRUMPublicDynamicTags();
            attachRUMRelative(tags, true);
            HashMap<String, Object> fields = new HashMap<>();
            fields.put(Constants.KEY_RUM_LONG_TASK_DURATION, duration);
            fields.put(Constants.KEY_RUM_LONG_TASK_STACK, log);

            if (property != null) {
                fields.putAll(property);
            }

            FTTrackInner.getInstance().rum(Utils.getCurrentNanoTime() - duration,
                    Constants.FT_MEASUREMENT_RUM_LONG_TASK, tags, fields, null, type);
            increaseLongTask(tags);
            generateRumData();
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
    }

    /**
     * Add long task
     *
     * @param log      log
     * @param duration duration, nanosecond
     */
    void addLongTask(String log, long duration) {
        addLongTask(log, duration, null);
    }

    /**
     * Transfer network connection indicator parameters
     *
     * @param resourceId    resource id
     * @param netStatusBean network status bean
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
        bean.resourceDNSStart = netStatusBean.getDNSStartTime();
        bean.resourceSSL = netStatusBean.getSSLTime();
        bean.resourceSSLStart = netStatusBean.getSslStartTime();
        bean.resourceTCP = netStatusBean.getTcpTime();
        bean.resourceTCPStart = netStatusBean.getConnectStartTime();
        bean.resourceTrans = netStatusBean.getResponseTime();
        bean.resourceTTFB = netStatusBean.getTTFB();
        long resourceLoad = netStatusBean.getHoleRequestTime();
        bean.resourceLoad = resourceLoad > 0 ? resourceLoad : bean.endTimeNanoForDuration - bean.startTimeNanoForDuration;
        bean.resourceFirstByte = netStatusBean.getFirstByteTime();
        bean.resourceFirstByteStart = netStatusBean.getFirstByteStartTime();
        bean.resourceDownloadTime = netStatusBean.getDownloadTime();
        bean.resourceDownloadTimeStart = netStatusBean.getDownloadTimeStart();
        bean.resourceHostIP = netStatusBean.resourceHostIP;
        bean.resourceResponseBodySize = netStatusBean.responseBodySize;
        bean.resourceRequestBodySize = netStatusBean.requestBodySize;
        bean.resourceConnectionReuse = netStatusBean.connectionReuse;
        bean.netStateSet = true;
        checkToAddResource(resourceId, bean);
    }

    /**
     * Resource loading performance
     *
     * @param resourceId resource id
     */
    void putRUMResourcePerformance(final String resourceId) {
        ResourceBean bean = resourceBeanMap.get(resourceId);
        if (bean == null) {
            LogUtils.e(TAG, "putRUMResourcePerformance:" + resourceId + ",bean null");
            return;
        } else {
            LogUtils.d(TAG, "putRUMResourcePerformance:" + resourceId);
        }
        String actionId = bean.actionId;
        String viewId = bean.viewId;
        String actionName = bean.actionName;
        String viewName = bean.viewName;
        String viewReferrer = bean.viewReferrer;
        String sessionId = bean.sessionId;

        CollectType collectType = checkSessionWillCollect(bean.sessionId);


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

            tags.put(Constants.KEY_RUM_RESOURCE_TYPE, bean.resourceType);
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

            long resourceSize = bean.resourceResponseBodySize + (bean.responseHeader == null ?
                    0 : bean.responseHeader.length());
            if (resourceSize > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_SIZE, resourceSize);
            }

            long requestSize = bean.resourceRequestBodySize + (bean.requestHeader == null ?
                    0 : bean.requestHeader.length());
            if (requestSize > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_REQUEST_SIZE, requestSize);
            }

            if (bean.resourceLoad > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_DURATION, bean.resourceLoad);
            }

            if (bean.resourceDNS > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_DNS, bean.resourceDNS);
                HashMap<String, Object> map = new HashMap<>();
                map.put("start", bean.resourceDNSStart);
                map.put("duration", bean.resourceDNS);
                fields.put(Constants.KEY_RUM_RESOURCE_DNS_TIME, Utils.hashMapObjectToJson(map));
            }
            if (bean.resourceTCP > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_TCP, bean.resourceTCP);
                HashMap<String, Object> map = new HashMap<>();
                map.put("start", bean.resourceTCPStart);
                map.put("duration", bean.resourceTCP);
                fields.put(Constants.KEY_RUM_RESOURCE_CONNECT_TIME, Utils.hashMapObjectToJson(map));
            }
            if (bean.resourceSSL > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_SSL, bean.resourceSSL);
                HashMap<String, Object> map = new HashMap<>();
                map.put("start", bean.resourceSSLStart);
                map.put("duration", bean.resourceSSL);
                fields.put(Constants.KEY_RUM_RESOURCE_SSL_TIME, Utils.hashMapObjectToJson(map));
            }
            if (bean.resourceTTFB > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_TTFB, bean.resourceTTFB);
            }

            if (bean.resourceTrans > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_TRANS, bean.resourceTrans);
            }

            if (bean.resourceFirstByte > 0) {
                fields.put(Constants.KEY_RUM_RESOURCE_FIRST_BYTE, bean.resourceFirstByte);
                HashMap<String, Object> map = new HashMap<>();
                map.put("start", bean.resourceFirstByteStart);
                map.put("duration", bean.resourceFirstByte);
                fields.put(Constants.KEY_RUM_RESOURCE_FIRST_BYTE_TIME, Utils.hashMapObjectToJson(map));
            }

            if (bean.resourceDownloadTime > 0) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("start", bean.resourceDownloadTimeStart);
                map.put("duration", bean.resourceDownloadTime);
                fields.put(Constants.KEY_RUM_DOWNLOAD_TIME, Utils.hashMapObjectToJson(map));
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
            tags.put(Constants.KEY_RUM_RESOURCE_ID, bean.id);
            tags.put(Constants.KEY_RUM_RESOURCE_URL, bean.url);

            fields.putAll(bean.property);
            fields.put(Constants.KEY_RUM_REQUEST_HEADER, bean.requestHeader);
            if (!Utils.isNullOrEmpty(bean.resourceProtocol)) {
                fields.put(Constants.KEY_RUM_RESOURCE_HTTP_PROTOCOL, bean.resourceProtocol);
            }
            fields.put(Constants.KEY_RUM_RESOURCE_CONNECTION_REUSE, bean.resourceConnectionReuse);
            fields.put(Constants.KEY_RUM_RESPONSE_HEADER, bean.responseHeader);


            FTTrackInner.getInstance().rum(bean.startTime,
                    Constants.FT_MEASUREMENT_RUM_RESOURCE, tags, fields, null, collectType);

            long time = Utils.getCurrentNanoTime();

            if (bean.resourceStatus >= HttpsURLConnection.HTTP_BAD_REQUEST
                    || (bean.resourceStatus == 0 && !Utils.isNullOrEmpty(bean.errorStack))) {
                SyncTaskManager.get().setErrorTimeLine(time, activeView);

                HashMap<String, Object> errorTags = FTRUMConfigManager.get().getRUMPublicDynamicTags();
                HashMap<String, Object> errorField = new HashMap<>();
                errorTags.put(Constants.KEY_RUM_ERROR_TYPE, ErrorType.NETWORK.toString());
                errorTags.put(Constants.KEY_RUM_ERROR_SOURCE, ErrorSource.NETWORK.toString());
                errorTags.put(Constants.KEY_RUM_ERROR_SITUATION, FTActivityManager.get().getAppState());
                errorTags.put(Constants.KEY_RUM_ACTION_ID, actionId);
                errorTags.put(Constants.KEY_RUM_ACTION_NAME, actionName);
                errorTags.put(Constants.KEY_RUM_VIEW_ID, viewId);
                errorTags.put(Constants.KEY_RUM_VIEW_NAME, viewName);
                errorTags.put(Constants.KEY_RUM_VIEW_REFERRER, viewReferrer);
                errorTags.put(Constants.KEY_RUM_SESSION_ID, sessionId);

                errorTags.put(Constants.KEY_RUM_RESOURCE_STATUS, resourceStatus);
                if (resourceStatus > 0) {
                    errorTags.put(Constants.KEY_RUM_RESOURCE_STATUS_GROUP, resourceStatusGroup);
                }
                errorTags.put(Constants.KEY_RUM_RESOURCE_ID, bean.id);
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

                FTTrackInner.getInstance().rum(bean.startTime, Constants.FT_MEASUREMENT_RUM_ERROR, errorTags, errorField, new RunnerCompleteCallBack() {
                    @Override
                    public void onComplete() {
                        increaseError(tags);
                    }
                }, collectType);
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
     * Set network transmission content
     *
     * @param resourceId    resource id
     * @param params
     * @param netStatusBean
     */
    public void addResource(String resourceId, ResourceParams params, NetStatusBean netStatusBean) {
        setTransformContent(resourceId, params, false);
        setNetState(resourceId, netStatusBean);
    }

    /**
     * Set network transmission content
     *
     * @param resourceId resource id
     * @param params
     */
    void setTransformContent(String resourceId, ResourceParams params, boolean reRegenerate) {

        FTTraceInterceptor.TraceRUMLinkable handler = FTTraceManager.get().getHandler(resourceId);
        String spanId = "";
        String traceId = "";
        if (handler != null) {
            spanId = handler.getSpanID();
            traceId = handler.getTraceID();
        }
//        else {
//            LogUtils.e(TAG, "setTransformContent trace null");
//        }

        ResourceBean bean = resourceBeanMap.get(resourceId);

        if (bean == null) {
            LogUtils.e(TAG, "setTransformContent bean null");
            return;
        }

//        if (params.resourceStatus < HttpsURLConnection.HTTP_OK) {
//            LogUtils.d(TAG, "setTransformContent code < 200");
//            return;
//        }
        try {
            URL url = Utils.parseFromUrl(params.url);
            bean.url = params.url;
            if (!reRegenerate) {
                bean.id = resourceId;
            } else {
                bean.id = Utils.randomUUID();
            }
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
        bean.resourceProtocol = params.resourceProtocol;
        bean.responseContentEncoding = params.responseContentEncoding;
        bean.resourceType = ResourceType.fromMimeType(params.responseContentType).getValue();
        bean.resourceStatus = params.resourceStatus;
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
     * LongTask increase
     * {@link Constants#KEY_RUM_ACTION_LONG_TASK_COUNT}
     * {@link Constants#KEY_RUM_VIEW_LONG_TASK_COUNT}
     *
     * @param tags rum globalContext
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
     * action quantity increase
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
     * Close View, calculate {@link ViewBean#timeSpent},{@link ViewBean#isClose} to true, and update
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
        final long loadTIme = viewBean.getLoadTime();
        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                FTDBManager.get().closeView(viewId, loadTIme, timeSpent, viewBean.getAttrJsonString());
                FTDBManager.get().updateViewUpdateTime(viewId, System.currentTimeMillis());
                if (callBack != null) {
                    callBack.onComplete();
                }
                try {
                    generateViewSum(); // Force generate data when closing view
                } catch (JSONException e) {
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                }
            }
        });
    }

    /**
     * Close Action, calculate {@link ActionBean#duration},{@link ActionBean#isClose} to true
     *
     * @param bean  active action bean
     * @param force force close
     */
    private void closeAction(ActiveActionBean bean, final boolean force) {
        final String actionId = bean.getId();
        final long duration = bean.getDuration();
        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                FTDBManager.get().closeAction(actionId, duration, force);
                try {
                    generateActionSum(); // Force generate data when closing view
                } catch (JSONException e) {
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                }
            }
        });
    }


    private String getViewId() {
        ActiveViewBean activeViewBean = activeView;
        if (activeViewBean == null) {
            return null;
        }
        return activeViewBean.getId();
    }

    String getViewName() {
        ActiveViewBean activeViewBean = activeView;
        if (activeViewBean == null) {
            return null;
        }
        return activeViewBean.getViewName();
    }

    private String getViewReferrer() {
        ActiveViewBean activeViewBean = activeView;
        if (activeViewBean == null) {
            return null;
        }
        return activeViewBean.getViewReferrer();
    }

    private String getActionName() {
        ActiveActionBean actionBean = activeAction;
        return actionBean == null ? null : actionBean.getActionName();
    }

    /**
     * Set RUM global view session action related data
     *
     * @param bean
     */
    private void attachRUMRelativeForResource(ResourceBean bean) {
        bean.viewId = getViewId();
        bean.viewName = getViewName();
        bean.viewReferrer = getViewReferrer();
        bean.sessionId = getSessionId();
        ActiveActionBean actionBean = activeAction;
        if (actionBean != null && !actionBean.isClose()) {
            bean.actionId = getActionId();
            bean.actionName = getActionName();
        }
    }

    /**
     * Add interface RUM related interface properties
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
            ActiveActionBean actionBean = activeAction;
            if (actionBean != null && !actionBean.isClose()) {
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
    private volatile long lastGenerateTime = 0;
    private static final long GENERATE_DEBOUNCE_DELAY = 200; // 200ms debounce delay
    private final Object generateLock = new Object();

    private void generateRumData() {
        generateRumData(false);
    }

    /**
     * Generate RUM data with optional force flag
     *
     * @param force if true, bypass debounce mechanism and generate immediately
     */
    private void generateRumData(boolean force) {
        // Use debounce mechanism to avoid too frequent refresh
        long currentTime = System.currentTimeMillis();
        if (!force) {
            synchronized (generateLock) {
                if (currentTime - lastGenerateTime < GENERATE_DEBOUNCE_DELAY) {
                    return; // Return directly if within debounce time
                }
                lastGenerateTime = currentTime;
            }
        }

        // Execute directly using thread pool, no longer using Handler
        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FTRUMInnerManager.this.generateActionSum();
                    FTRUMInnerManager.this.generateViewSum();
                } catch (Exception e) {
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                }
            }
        });
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
                Constants.FT_MEASUREMENT_RUM_ACTION, tags, fields, null, bean.getCollectType());
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
                    fields.put(Constants.KEY_RUM_VIEW_TIME_SPENT, System.nanoTime() - bean.getStartTimeNanoForDuration());
                }
                fields.put(Constants.KEY_RUM_VIEW_LONG_TASK_COUNT, bean.getLongTaskCount());
                fields.put(Constants.KEY_RUM_VIEW_IS_ACTIVE, !bean.isClose());
                fields.put(Constants.KEY_RUM_SDK_VIEW_UPDATE_TIME, bean.getViewUpdateTime());

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
                if (bean.getLastErrorTime() > 0) {
                    fields.put(Constants.KEY_RUM_SESSION_ERROR_TIMESTAMP, bean.getLastErrorTime());
                }

                FTTrackInner.getInstance().rum(bean.getStartTime(),
                        Constants.FT_MEASUREMENT_RUM_VIEW, tags, fields, new RunnerCompleteCallBack() {
                            @Override
                            public void onComplete() {
                                FTDBManager.get().updateViewUploadTime(bean.getId(), System.currentTimeMillis());
                            }
                        }, bean.getCollectType());
            }

            FTDBManager.get().cleanCloseViewData();
        } while (beans.size() >= LIMIT_SIZE);
    }

    void initParams(FTRUMConfig config) {
        sampleRate = config.getSamplingRate();
        sessionErrorSampleError = config.getSessionErrorSampleRate();
        sessionId = Utils.randomUUID();
        checkSessionKeep(sessionId, sampleRate, sessionErrorSampleError);
        EventConsumerThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                FTDBManager.get().closeAllActionAndView();
            }
        });

    }

    /**
     * According to the sampling rate, confirm whether this session_id needs to be collected
     *
     * @param sessionId
     * @param sampleRate
     */
    private void checkSessionKeep(String sessionId, float sampleRate, float errorSampleRate) {
        boolean sessionCollect = Utils.enableTraceSamplingRate(sampleRate);
        if (!sessionCollect) {
            addSessionToFilter(notCollectArr, sessionId, false);

            boolean sessionErrorCollect = Utils.enableTraceSamplingRate(errorSampleRate);
            if (!sessionErrorCollect) {
                addSessionToFilter(notSessionErrorCollectArr, sessionId, true);
            }
        }

    }

    private void addSessionToFilter(List<String> sessionList, String sessionId, boolean isErrorSession) {
        synchronized (isErrorSession ? notSessionErrorCollectArr : notCollectArr) {
            if (sessionList.size() >= SESSION_FILTER_CAPACITY) {
                try {
                    sessionList.remove(0);
                } catch (IndexOutOfBoundsException e) {
                    LogUtils.d(TAG, "Session list remove error: " + LogUtils.getStackTraceString(e));
                }
            }
            sessionList.add(sessionId);
        }
        LogUtils.w(TAG, String.format("According to FTRUMConfig %s sampling rate, not hit, Session Track-> session_id: %s",
                isErrorSession ? "errorSampleRate" : "SampleRate", sessionId));
    }

    /**
     * Confirm whether the session needs to be collected
     *
     * @param sessionId
     * @return
     */
    public CollectType checkSessionWillCollect(String sessionId) {
        if (!notCollectArr.contains(sessionId)) {
            return CollectType.COLLECT_BY_SAMPLE;
        } else if (!notSessionErrorCollectArr.contains(sessionId)) {
            return CollectType.COLLECT_BY_ERROR_SAMPLE;
        } else {
            return CollectType.NOT_COLLECT;
        }
    }

    public void release() {
        // Reset debounce timestamps
        synchronized (generateLock) {
            lastGenerateTime = 0;
        }

        mHandler.removeCallbacks(mActionRecheckRunner);
        activeAction = null;
        if (activeView != null) {
            // Force generate final data before release
            generateRumData(true);
            activeView = null;
        }
        notCollectArr.clear();
        notSessionErrorCollectArr.clear();
        resourceBeanMap.clear();
        viewList.clear();
    }

    /**
     * Check the time to add AddResource data
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
