package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.BaseContentBean;
import com.ft.sdk.garble.bean.CollectType;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.LineProtocolBean;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.FTDBCachePolicy;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.http.FTResponseData;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.NetCodeStatus;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.manager.RequestCallback;
import com.ft.sdk.garble.threadpool.DataProcessThreadPool;
import com.ft.sdk.garble.threadpool.RunnerCompleteCallBack;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.HashMapUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * author: huangDianHua
 * time: 2020/8/7 16:37:09
 * description: Track methods for internal use
 */
public class FTTrackInner {
    private final static String TAG = Constants.LOG_TAG_PREFIX + "FTTrackInner";
    private static volatile FTTrackInner instance;

    /**
     * Called by test cases
     * {@link com.ft.test.base.FTBaseTest#getInnerSyncDataHelper()}
     * {@link com.ft.sdk.tests.UtilsTest#convertToLineProtocolLines(List)}
     */
    private final SyncDataHelper dataHelper = new SyncDataHelper();

    private FTTrackInner() {
    }

    public static FTTrackInner getInstance() {
        if (instance == null) {
            synchronized (FTTrackInner.class) {
                if (instance == null) {
                    instance = new FTTrackInner();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize basic SDK configuration
     *
     * @param config
     */
    void initBaseConfig(FTSDKConfig config) {
        dataHelper.initBaseConfig(config);
    }

    /**
     * Initialize SDK Log configuration
     *
     * @param config
     */
    void initLogConfig(FTLoggerConfig config) {
        dataHelper.initLogConfig(config);
    }

    /**
     * Initialize SDK RUM configuration
     *
     * @param config
     */
    void initRUMConfig(FTRUMConfig config) {
        dataHelper.initRUMConfig(config);
    }

    /**
     * Dynamically set global tag
     *
     * @param globalContext
     */
    void appendGlobalContext(HashMap<String, Object> globalContext) {
        dataHelper.appendGlobalContext(globalContext);
    }

    /**
     * Dynamically set global tag
     *
     * @param key
     * @param value
     */
    void appendGlobalContext(String key, String value) {
        dataHelper.appendGlobalContext(key, value);
    }

    /**
     * Dynamically set RUM tag
     *
     * @param globalContext
     */
    void appendRUMGlobalContext(HashMap<String, Object> globalContext) {
        dataHelper.appendRUMGlobalContext(globalContext);
    }

    /**
     * Dynamically set RUM tag
     *
     * @param key
     * @param value
     */
    void appendRUMGlobalContext(String key, String value) {
        dataHelper.appendRUMGlobalContext(key, value);
    }

    /**
     * Dynamically set Log tag
     *
     * @param globalContext
     */
    void appendLogGlobalContext(HashMap<String, Object> globalContext) {
        dataHelper.appendLogGlobalContext(globalContext);
    }

    /**
     * Dynamically set Log tag
     *
     * @param key
     * @param value
     */
    void appendLogGlobalContext(String key, String value) {
        dataHelper.appendLogGlobalContext(key, value);
    }


    /**
     * rum event data
     *
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     */
    void rum(long time, String measurement, final HashMap<String, Object> tags, HashMap<String, Object> fields,
             RunnerCompleteCallBack callBack, CollectType collectType) {
        long viewDataGenerateTime = 0;//Due to the view update principle, the start time cannot be used, otherwise it will be discarded
        switch (collectType) {
            case NOT_COLLECT:
                break;
            case COLLECT_BY_ERROR_SAMPLE:
                if (measurement.equals(Constants.FT_MEASUREMENT_RUM_VIEW)) {
                    fields.put(Constants.KEY_SAMPLED_FOR_ERROR_SESSION, true);
                    long errorTimestamp = HashMapUtils.getLong(fields,
                            Constants.KEY_SESSION_ERROR_TIMESTAMP, 0L);
                    viewDataGenerateTime = errorTimestamp > 0 ? errorTimestamp : Utils.getCurrentNanoTime();
                }
            case COLLECT_BY_SAMPLE:
                syncRUMDataBackground(collectType == CollectType.COLLECT_BY_ERROR_SAMPLE ?
                                DataType.RUM_APP_ERROR_SAMPLED : DataType.RUM_APP,
                        viewDataGenerateTime, time, measurement,
                        tags, fields, callBack);
                break;
        }

    }

    /**
     * rum sync web data
     *
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     */
    void rumWebView(long time, String measurement, final HashMap<String, Object> tags, HashMap<String, Object> fields, CollectType collectType) {
        switch (collectType) {
            case NOT_COLLECT:
                break;
            case COLLECT_BY_ERROR_SAMPLE:
            case COLLECT_BY_SAMPLE:
                syncRUMDataBackground(collectType == CollectType.COLLECT_BY_ERROR_SAMPLE ?
                        DataType.RUM_WEBVIEW_ERROR_SAMPLED : DataType.RUM_WEBVIEW, time, measurement, tags, fields);

        }
    }

    /**
     * Asynchronously write sync data
     * <p>
     * Called by AndroidTest {@link com.ft.test.base.FTBaseTest#invokeSyncData(DataType, String, JSONObject, JSONObject)}
     *
     * @param dataType
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     */
    private void syncRUMDataBackground(final DataType dataType, final long time,
                                       final String measurement, final HashMap<String, Object> tags, final HashMap<String, Object> fields) {
        syncRUMDataBackground(dataType, Utils.getCurrentNanoTime(), time, measurement, tags, fields, null);
    }

    /**
     * Asynchronously write sync data
     *
     * @param dataType
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     * @param callBack
     */
    private void syncRUMDataBackground(final DataType dataType, final long dataGenerateTime, final long time,
                                       final String measurement, final HashMap<String, Object> tags,
                                       final HashMap<String, Object> fields, RunnerCompleteCallBack callBack) {
        DataProcessThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SyncData recordData = SyncData.getSyncData(dataHelper, dataType,
                            new LineProtocolBean(measurement, tags, fields, time), dataGenerateTime);
                    synchronized (FTDBCachePolicy.get().getRumLock()) {
                        int status = FTDBCachePolicy.get().optRUMCachePolicy(1);
                        StringBuilder errorDec = new StringBuilder();
                        if (measurement.equals(Constants.FT_MEASUREMENT_RUM_ERROR)) {
                            errorDec.append(" | ")
                                    .append(tags.get(Constants.KEY_RUM_ERROR_TYPE))
                                    .append(" | \"")
                                    .append(fields.get(Constants.KEY_RUM_ERROR_MESSAGE))
                                    .append("\"");
                        }
                        switch (status) {
                            case 0:
                            case 1:
                                boolean result = FTDBManager.get().insertFtOperation(recordData, false);
                                LogUtils.d(TAG, "syncDataBackground:" + measurement + errorDec + " "
                                        + dataType.toString() + ":insert=" + result +
                                        ",uuid:" + recordData.getUuid() + (status == 1 ? ",drop OldCache" : ""));
                                if (callBack != null) {
                                    callBack.onComplete();
                                }
                                if (result) {
                                    FTDBCachePolicy.get().optRUMCount(1);
                                }
                                SyncTaskManager.get().executeSyncPoll();
                                break;
                            case -1:
                                LogUtils.e(TAG, "syncDataBackground:" + measurement + errorDec + " " +
                                        dataType.toString() + errorDec + ",uuid:" + recordData.getUuid() + ",drop by Cache limit");
                                break;
                        }
                    }
                } catch (Exception e) {
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                }
            }
        });
    }


    /**
     * Synchronize buried point data in a sub-thread (not through the database)
     * inovke by test Case
     *
     * @param callback
     */
    void trackLogAsync(@NonNull final BaseContentBean bean, final RequestCallback callback) {
        DataProcessThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SyncData recordData = SyncData.getFromLogBean(dataHelper, bean);
                    String body = recordData.getDataString();
                    String model = Constants.URL_MODEL_LOG;
                    String content_type = "text/plain";
                    FTResponseData result = HttpBuilder.Builder()
                            .setModel(model)
                            .addHeadParam("Content-Type", content_type)
                            .setMethod(RequestMethod.POST)
                            .setBodyString(body).executeSync();
                    if (callback != null) {
                        callback.onResponse(result.getCode(), result.getMessage(), result.getErrorCode());
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        if (e instanceof InvalidParameterException) {
                            callback.onResponse(NetCodeStatus.INVALID_PARAMS_EXCEPTION_CODE, e.getMessage(), "");
                        } else {
                            callback.onResponse(NetCodeStatus.UNKNOWN_EXCEPTION_CODE, e.getMessage(), "");
                        }
                    }
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                }
            }
        });
    }

    /**
     * Store a single log entry locally and synchronize
     *
     * @param logBean
     * @param isSilence Whether to trigger sync immediately
     */
    void logBackground(@NonNull LogBean logBean, boolean isSilence) {
        TrackLogManager.get().trackLog(logBean, isSilence);
    }

    /**
     * Store a single log entry locally and synchronize
     *
     * @param logBean
     */
    void logBackground(@NonNull LogBean logBean) {
        logBackground(logBean, false);
    }

    /**
     * Store multiple log entries locally and synchronize (synchronously)
     *
     * @param logBeans
     */
    void batchLogBeanSync(@NonNull List<BaseContentBean> logBeans, boolean isSilence) {
        try {
            ArrayList<SyncData> datas = new ArrayList<>();
            for (BaseContentBean logBean : logBeans) {
                try {
                    datas.add(SyncData.getFromLogBean(dataHelper, logBean));
                } catch (Exception e) {
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                }
            }
            judgeLogCachePolicy(datas, isSilence);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
    }

    /**
     * Determine whether to execute the sync policy
     *
     * @param recordDataList {@link  SyncData} list
     */
    private void judgeLogCachePolicy(@NonNull List<SyncData> recordDataList, boolean silence) {
        //If the OP type is not LOG, perform database operation directly; otherwise, execute the sync policy, and determine whether to perform database operation based on the result of the sync policy
        synchronized (FTDBCachePolicy.get().getLogLock()) {

            int length = recordDataList.size();
            int policyStatus = FTDBCachePolicy.get().optLogCachePolicy(length);
            if (policyStatus >= 0) {//execute sync policy
                boolean result = FTDBManager.get().insertFtOptList(recordDataList, false);
                FTDBCachePolicy.get().optLogCount(recordDataList.size());
                if (policyStatus == 0) {//not dropped
                    LogUtils.d(TAG, "judgeLogCachePolicy:insert-result=" + result);
                } else {//drop all or drop some old data, old data is dropped in optLogCachePolicy
                    int dropCount = Math.min(policyStatus, length);
                    LogUtils.d(TAG, "judgeLogCachePolicy:insert-result=" + result + ", drop cache count:" + dropCount);
                }
                if (!silence) {
                    SyncTaskManager.get().executeSyncPoll();
                }
            } else {
                int dropCount = Math.abs(policyStatus);
                if (dropCount == length) {//all new data dropped
                    LogUtils.e(TAG, "reach log limit, drop log count:" + dropCount);
                } else {//drop some new data
                    recordDataList.subList(length - dropCount, length).clear();
                    boolean result = FTDBManager.get().insertFtOptList(recordDataList, false);
                    FTDBCachePolicy.get().optLogCount(recordDataList.size());
                    LogUtils.d(TAG, "judgeLogCachePolicy:insert-result=" + result + ", drop log count:" + dropCount);
                }
            }
        }

    }

    SyncDataHelper getCurrentDataHelper() {
        return dataHelper;
    }


}
