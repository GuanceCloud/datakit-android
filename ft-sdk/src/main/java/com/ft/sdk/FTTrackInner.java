package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTDBCachePolicy;
import com.ft.sdk.garble.FTRUMConfig;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.LineProtocolBean;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.NetCodeStatus;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.http.ResponseData;
import com.ft.sdk.garble.manager.AsyncCallback;
import com.ft.sdk.garble.manager.FTManager;
import com.ft.sdk.garble.manager.SyncDataHelper;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;

import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * author: huangDianHua
 * time: 2020/8/7 16:37:09
 * description:内部使用的 Track 方法
 */
public class FTTrackInner {
    private final static String TAG = "FTTrackInner";
    private static FTTrackInner instance;

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

    void rumInflux(long time, String measurement, final JSONObject tags, JSONObject fields) {
        syncDataBackground(DataType.RUM_INFLUX, time, measurement, tags, fields);

    }

    void rumES(long time, String measurement, final JSONObject tags, JSONObject fields) {
        syncDataBackground(DataType.RUM_ES, time, measurement, tags, fields);
    }


    /**
     * 在子线程中将埋点数据同步（经过数据库）
     *
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     */
    void trackBackground(long time, String measurement, final JSONObject tags, JSONObject fields) {
        SyncDataHelper.addMonitorData(tags, fields);
        syncDataBackground(DataType.TRACK, time, measurement, tags, fields);
    }

    private void syncDataBackground(DataType dataType, long time,
                                    String measurement, final JSONObject tags, JSONObject fields) {
        ThreadPoolUtils.get().execute(() -> {
            try {
                SyncJsonData recordData = SyncJsonData.getSyncJsonData(dataType,
                        new LineProtocolBean(measurement, tags, fields, time));
                boolean result = FTManager.getFTDBManager().insertFTOperation(recordData);
                LogUtils.d(TAG, "trackBackground:insert-result=" + result);
                FTManager.getSyncTaskManager().executeSyncPoll();
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(TAG, e.getMessage());
            }
        });

    }


    /**
     * 在子线程中将埋点数据同步(不经过数据库)
     *
     * @param trackBeans
     * @param callback
     */
    void trackAsync(@NonNull List<LineProtocolBean> trackBeans, AsyncCallback callback) {
        ThreadPoolUtils.get().execute(() -> {
            List<SyncJsonData> recordDataList = new ArrayList<>();
            for (LineProtocolBean t : trackBeans) {
                try {
                    SyncJsonData recordData = SyncJsonData.getSyncJsonData(DataType.TRACK,
                            new LineProtocolBean(t.getMeasurement(), t.getTags(),
                                    t.getFields(), t.getTimeMillis()));
                    recordDataList.add(recordData);
                    uploadTrackOPData(recordDataList, callback);
                } catch (Exception e) {
                    if (callback != null) {
                        if (e instanceof InvalidParameterException) {
                            callback.onResponse(NetCodeStatus.INVALID_PARAMS_EXCEPTION_CODE, e.getMessage());
                        } else {
                            callback.onResponse(NetCodeStatus.UNKNOWN_EXCEPTION_CODE, e.getMessage());
                        }
                    }
                    LogUtils.e(TAG, e.getMessage());
                }
            }
        });
    }


    /**
     * 同步上传用户埋点信息，并返回上传结果
     *
     * @param recordDataList
     * @param callback
     */
    private void uploadTrackOPData(List<SyncJsonData> recordDataList, AsyncCallback callback) {
//        if (!TokenCheck.get().checkToken()) {
//            if (callback != null) {
//                callback.onResponse(NetCodeStatus.TOKEN_ERROR, TokenCheck.get().message);
//                return;
//            }
//        }
        if (recordDataList == null || recordDataList.isEmpty()) {
            return;
        }
        SyncDataHelper syncDataManager = new SyncDataHelper();
        String body = syncDataManager.getBodyContent(DataType.TRACK, recordDataList);
        String model = Constants.URL_MODEL_TRACK_INFLUX;
        String content_type = "text/plain";
        ResponseData result = HttpBuilder.Builder()
                .setModel(model)
                .addHeadParam("Content-Type", content_type)
                .setMethod(RequestMethod.POST)
                .setBodyString(body).executeSync(ResponseData.class);
        if (callback != null) {
            callback.onResponse(result.getHttpCode(), result.getData());
        }
    }

    /**
     * 判断是否是合法的Values
     *
     * @param jsonObject
     * @return
     */
    public boolean isLegalValues(JSONObject jsonObject) {
        if (jsonObject == null) {
            LogUtils.e(TAG, "参数 fields 不能为空");
            return false;
        }
        if (jsonObject.keys().hasNext()) {
            return true;
        } else {
            LogUtils.e(TAG, "参数 fields 不能为空");
            return false;
        }
    }


    /**
     * 将单条日志数据存入本地同步
     *
     * @param logBean
     */
    public void logBackground(@NonNull LogBean logBean) {
        ArrayList<LogBean> list = new ArrayList<>();
        list.add(logBean);
        logBackground(list);
    }

    /**
     * 将多条日志数据存入本地同步(异步)
     *
     * @param logBeans
     */
    public void logBackground(@NonNull List<LogBean> logBeans) {
        ArrayList<SyncJsonData> datas = new ArrayList<>();
        for (LogBean logBean : logBeans
        ) {
            try {
                datas.add(SyncJsonData.getFromLogBean(logBean));
            } catch (Exception e) {
                LogUtils.e(TAG, e.getMessage());
            }

        }
        judgeLogCachePolicy(datas);
    }


    /**
     * 判断是否需要执行同步策略
     *
     * @param data
     */
    private void judgeLogCachePolicy(SyncJsonData data) {
        List<SyncJsonData> list = new ArrayList<>();
        list.add(data);
        judgeLogCachePolicy(list);

    }

    /**
     * 判断是否需要执行同步策略
     *
     * @param recordDataList
     */
    private void judgeLogCachePolicy(@NonNull List<SyncJsonData> recordDataList) {
        //如果 OP 类型不等于 LOG 则直接进行数据库操作；否则执行同步策略，根据同步策略返回结果判断是否需要执行数据库操作
        int length = recordDataList.size();
        int policyStatus = FTDBCachePolicy.get().optLogCachePolicy(length);
        if (policyStatus >= 0) {//执行同步策略
            if (policyStatus > 0) {
                for (int i = 0; i < policyStatus && i < length; i++) {
                    recordDataList.remove(0);
                }
            }
            boolean result = FTManager.getFTDBManager().insertFtOptList(recordDataList);
            LogUtils.d(TAG, "judgeLogCachePolicy:insert-result=" + result);
            FTManager.getSyncTaskManager().executeSyncPoll();
        }
    }


}
