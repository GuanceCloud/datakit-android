package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTDBCachePolicy;
import com.ft.sdk.garble.bean.BaseContentBean;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.LineProtocolBean;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.http.FTResponseData;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.NetCodeStatus;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.manager.AsyncCallback;
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
     * 初始化 SDK Trace 配置
     *
     * @param config
     */
    void initTraceConfig(FTTraceConfig config) {
        dataHelper.initTraceConfig(config);
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
    void rum(long time, String measurement, final HashMap<String, Object> tags, HashMap<String, Object> fields, RunnerCompleteCallBack callBack) {
        String sessionId = HashMapUtils.getString(tags, Constants.KEY_RUM_SESSION_ID);
        if (FTRUMInnerManager.get().checkSessionWillCollect(sessionId)) {
            syncDataBackground(DataType.RUM_APP, time, measurement, tags, fields, callBack);
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
            syncDataBackground(DataType.RUM_WEBVIEW, time, measurement, tags, fields);
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
    private void syncDataBackground(final DataType dataType, final long time,
                                    final String measurement, final HashMap<String, Object> tags, final HashMap<String, Object> fields) {
        syncDataBackground(dataType, time, measurement, tags, fields, null);
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
    private void syncDataBackground(final DataType dataType, final long time,
                                    final String measurement, final HashMap<String, Object> tags, final HashMap<String, Object> fields, RunnerCompleteCallBack callBack) {
        DataUploaderThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                try {

                    SyncJsonData recordData = SyncJsonData.getSyncJsonData(dataHelper, dataType,
                            new LineProtocolBean(measurement, tags, fields, time));
                    boolean result = FTDBManager.get().insertFtOperation(recordData, false);
                    LogUtils.d(TAG, "syncDataBackground:" + measurement + " "
                            + dataType.toString() + ",uuid:" + recordData.getUuid() + ":insert=" + result);
                    if (callBack != null) {
                        callBack.onComplete();
                    }
                    SyncTaskManager.get().executeSyncPoll();
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
    void trackLogAsync(@NonNull final BaseContentBean bean, final AsyncCallback callback) {
        DataUploaderThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SyncJsonData recordData = SyncJsonData.getFromLogBean(dataHelper, bean);
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
            ArrayList<SyncJsonData> datas = new ArrayList<>();
            for (BaseContentBean logBean : logBeans) {
                try {
                    datas.add(SyncJsonData.getFromLogBean(dataHelper, logBean));
                } catch (Exception e) {
                    LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                }
            }
            judgeLogCachePolicy(datas, isSilence);
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }

    }

//    void batchTraceBeanBackground(@NonNull List<BaseContentBean> logBeans) {
//        ArrayList<SyncJsonData> datas = new ArrayList<>();
//        for (BaseContentBean logBean : logBeans) {
//            try {
//                datas.add(SyncJsonData.getFromLogBean(logBean, DataType.TRACE));
//            } catch (Exception e) {
//                LogUtils.e(TAG,Log.getStackTraceString(e));
//            }
//
//        }
//        boolean result = FTDBManager.get().insertFtOptList(datas);
//        LogUtils.d(TAG, "batchTraceBeanBackground:insert-result=" + result);
//
//        SyncTaskManager.get().executeSyncPoll();
//    }


    /**
     * 判断是否需要执行同步策略
     *
     * @param recordDataList {@link  SyncJsonData} 列表
     */
    private void judgeLogCachePolicy(@NonNull List<SyncJsonData> recordDataList, boolean silence) {
        //如果 OP 类型不等于 LOG 则直接进行数据库操作；否则执行同步策略，根据同步策略返回结果判断是否需要执行数据库操作
        int length = recordDataList.size();
        int policyStatus = FTDBCachePolicy.get().optLogCachePolicy(length);
        if (policyStatus >= 0) {//执行同步策略
            if (policyStatus > 0) {
                int dropCount = Math.min(policyStatus, length);
                recordDataList.subList(0, dropCount).clear();
                LogUtils.e(TAG, "reach log limit, drop log count:" + dropCount);
            }
            boolean result = FTDBManager.get().insertFtOptList(recordDataList, false);
            LogUtils.d(TAG, "judgeLogCachePolicy:insert-result=" + result);
            if (!silence) {
                SyncTaskManager.get().executeSyncPoll();
            }
        } else {
            LogUtils.e(TAG, "reach log limit, drop log count:" + length);
        }
    }

    SyncDataHelper getCurrentDataHelper() {
        return dataHelper;
    }


}
