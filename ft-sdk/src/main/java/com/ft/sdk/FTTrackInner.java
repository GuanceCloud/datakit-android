package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.BaseContentBean;
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
import com.ft.sdk.garble.threadpool.DataUploaderThreadPool;
import com.ft.sdk.garble.threadpool.RunnerCompleteCallBack;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.HashMapUtils;
import com.ft.sdk.garble.utils.LogUtils;

import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * author: huangDianHua
 * time: 2020/8/7 16:37:09
 * description:内部使用的 Track 方法
 */
public class FTTrackInner {
    private final static String TAG = Constants.LOG_TAG_PREFIX + "FTTrackInner";
    private static volatile FTTrackInner instance;

    /**
     * 测试用例调用
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
     * 初始化基础 SDK 配置
     *
     * @param config
     */
    void initBaseConfig(FTSDKConfig config) {
        dataHelper.initBaseConfig(config);
    }

    /**
     * 初始化 SDK Log 配置
     *
     * @param config
     */
    void initLogConfig(FTLoggerConfig config) {
        dataHelper.initLogConfig(config);
    }

    /**
     * 初始化 SDK RUM 配置
     *
     * @param config
     */
    void initRUMConfig(FTRUMConfig config) {
        dataHelper.initRUMConfig(config);
    }

    /**
     * 动态设置全局 tag
     *
     * @param globalContext
     */
    void appendGlobalContext(HashMap<String, Object> globalContext) {
        dataHelper.appendGlobalContext(globalContext);
    }

    /**
     * 动态设置全局 tag
     *
     * @param key
     * @param value
     */
    void appendGlobalContext(String key, String value) {
        dataHelper.appendGlobalContext(key, value);
    }

    /**
     * 动态设置 RUM tag
     *
     * @param globalContext
     */
    void appendRUMGlobalContext(HashMap<String, Object> globalContext) {
        dataHelper.appendRUMGlobalContext(globalContext);
    }

    /**
     * 动态设置 RUM tag
     *
     * @param key
     * @param value
     */
    void appendRUMGlobalContext(String key, String value) {
        dataHelper.appendRUMGlobalContext(key, value);
    }

    /**
     * 动态设置 Log tag
     *
     * @param globalContext
     */
    void appendLogGlobalContext(HashMap<String, Object> globalContext) {
        dataHelper.appendLogGlobalContext(globalContext);
    }

    /**
     * 动态设置 Log tag
     *
     * @param key
     * @param value
     */
    void appendLogGlobalContext(String key, String value) {
        dataHelper.appendLogGlobalContext(key, value);
    }


    /**
     * rum 事件数据
     *
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     */
    void rum(long time, String measurement, final HashMap<String, Object> tags, HashMap<String, Object> fields,
             RunnerCompleteCallBack callBack) {
        String sessionId = HashMapUtils.getString(tags, Constants.KEY_RUM_SESSION_ID);
        if (FTRUMInnerManager.get().checkSessionWillCollect(sessionId)) {
            syncRUMDataBackground(DataType.RUM_APP, time, measurement, tags, fields, callBack);
        } else {
            if (FTRUMInnerManager.get().checkSessionErrorWillCollect(sessionId)) {
                if (measurement.equals(Constants.FT_MEASUREMENT_RUM_VIEW)) {
                    fields.put(Constants.KEY_SAMPLED_FOR_ERROR_SESSION, true);
                }
                syncRUMDataBackground(DataType.RUM_APP_NOT_SAMPLE, time, measurement, tags, fields, callBack);
            }
        }
    }

    /**
     * rum 同步 web 数据
     *
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     */
    void rumWebView(long time, String measurement, final HashMap<String, Object> tags, HashMap<String, Object> fields) {
        String sessionId = HashMapUtils.getString(tags, Constants.KEY_RUM_SESSION_ID);
        if (FTRUMInnerManager.get().checkSessionWillCollect(sessionId)) {
            syncRUMDataBackground(DataType.RUM_WEBVIEW, time, measurement, tags, fields);
        } else {
            if (FTRUMInnerManager.get().checkSessionErrorWillCollect(sessionId)) {
                syncRUMDataBackground(DataType.RUM_WEBVIEW_NOT_SAMPLE, time, measurement, tags, fields);
            }
        }
    }

    /**
     * 同步数据异步写入
     * <p>
     * AndroidTest 调用方法  {@link com.ft.test.base.FTBaseTest#invokeSyncData(DataType, String, JSONObject, JSONObject)}
     *
     * @param dataType
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     */
    private void syncRUMDataBackground(final DataType dataType, final long time,
                                       final String measurement, final HashMap<String, Object> tags, final HashMap<String, Object> fields) {
        syncRUMDataBackground(dataType, time, measurement, tags, fields, null);
    }

    /**
     * 同步数据异步写入
     *
     * @param dataType
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     * @param callBack
     */
    private void syncRUMDataBackground(final DataType dataType, final long time,
                                       final String measurement, final HashMap<String, Object> tags,
                                       final HashMap<String, Object> fields, RunnerCompleteCallBack callBack) {
        DataUploaderThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SyncData recordData = SyncData.getSyncData(dataHelper, dataType,
                            new LineProtocolBean(measurement, tags, fields, time));
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
     * 在子线程中将埋点数据同步(不经过数据库)
     * inovke by test Case
     *
     * @param callback
     */
    void trackLogAsync(@NonNull final BaseContentBean bean, final RequestCallback callback) {
        DataUploaderThreadPool.get().execute(new Runnable() {
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
     * 将单条日志数据存入本地同步
     *
     * @param logBean
     * @param isSilence 是否立即触发同步
     */
    void logBackground(@NonNull LogBean logBean, boolean isSilence) {
        TrackLogManager.get().trackLog(logBean, isSilence);
    }

    /**
     * 将单条日志数据存入本地同步
     *
     * @param logBean
     */
    void logBackground(@NonNull LogBean logBean) {
        logBackground(logBean, false);
    }

    /**
     * 将多条日志数据存入本地同步(同步)
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
     * 判断是否需要执行同步策略
     *
     * @param recordDataList {@link  SyncData} 列表
     */
    private void judgeLogCachePolicy(@NonNull List<SyncData> recordDataList, boolean silence) {
        //如果 OP 类型不等于 LOG 则直接进行数据库操作；否则执行同步策略，根据同步策略返回结果判断是否需要执行数据库操作
        synchronized (FTDBCachePolicy.get().getLogLock()) {

            int length = recordDataList.size();
            int policyStatus = FTDBCachePolicy.get().optLogCachePolicy(length);
            if (policyStatus >= 0) {//执行同步策略
                boolean result = FTDBManager.get().insertFtOptList(recordDataList, false);
                FTDBCachePolicy.get().optLogCount(recordDataList.size());
                if (policyStatus == 0) {//不丢弃
                    LogUtils.d(TAG, "judgeLogCachePolicy:insert-result=" + result);
                } else {//丢弃全部或丢弃一部分旧数据，旧数据在 optLogCachePolicy 中丢弃
                    int dropCount = Math.min(policyStatus, length);
                    LogUtils.d(TAG, "judgeLogCachePolicy:insert-result=" + result + ", drop cache count:" + dropCount);
                }
                if (!silence) {
                    SyncTaskManager.get().executeSyncPoll();
                }
            } else {
                int dropCount = Math.abs(policyStatus);
                if (dropCount == length) {//全丢新数据
                    LogUtils.e(TAG, "reach log limit, drop log count:" + dropCount);
                } else {//丢一部分新数据
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
