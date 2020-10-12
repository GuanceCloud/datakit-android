package com.ft.sdk.garble;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.OPData;
import com.ft.sdk.garble.bean.ObjectBean;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.bean.TrackBean;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.NetCodeStatus;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.http.ResponseData;
import com.ft.sdk.garble.manager.FTManager;
import com.ft.sdk.garble.manager.SyncDataHelper;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import static com.ft.sdk.garble.manager.SyncDataHelper.addMonitorData;

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
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        judgeLogCachePolicy(datas);
    }


    /**
     * 将单条对象数据直接同步
     *
     * @param objectBean
     */
    public void objectBackground(@NonNull ObjectBean objectBean) {
        boolean result = FTManager.getFTDBManager().insertFTOperation(SyncJsonData.getFromObjectData(objectBean));
        LogUtils.d(TAG, "objectBackground:insert-result=" + result);
        FTManager.getSyncTaskManager().executeSyncPoll();
    }

    /**
     * 在子线程中将埋点数据同步（经过数据库）
     *
     * @param op
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     */
    public void trackBackground(OP op, long time, String measurement, final JSONObject tags, JSONObject fields) {
        ThreadPoolUtils.get().execute(() -> {
            try {
                SyncJsonData recordData = SyncJsonData.getFromTrackBean(new TrackBean(measurement, tags, fields, time), op);
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

    /**
     * 在子线程中将埋点数据同步(不经过数据库)
     *
     * @param trackBeans
     * @param callback
     */
    public void trackAsync(OP op, @NonNull List<TrackBean> trackBeans, AsyncCallback callback) {
        ThreadPoolUtils.get().execute(() -> {
            List<SyncJsonData> recordDataList = new ArrayList<>();
            for (TrackBean t : trackBeans) {
                try {
                    SyncJsonData recordData = SyncJsonData.getFromTrackBean(new TrackBean(t.getMeasurement(),
                            t.getTags(), t.getFields(), t.getTimeMillis()), op);
                    recordDataList.add(recordData);
                    uploadTrackOPData(recordDataList, callback);
                } catch (Exception e) {
                    if (callback != null) {
                        if (e instanceof InvalidParameterException) {
                            callback.onResponse(NetCodeStatus.INVALID_PARAMS_EXCEPTION_CODE, e.getMessage());
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
        if (!TokenCheck.get().checkToken()) {
            if (callback != null) {
                callback.onResponse(NetCodeStatus.TOKEN_ERROR, TokenCheck.get().message);
                return;
            }
        }
        if (recordDataList == null || recordDataList.isEmpty()) {
            return;
        }
        SyncDataHelper syncDataManager = new SyncDataHelper();
        String body = syncDataManager.getBodyContent(DataType.TRACK, recordDataList);
        body = body.replaceAll(Constants.SEPARATION_PRINT, Constants.SEPARATION)
                .replaceAll(Constants.SEPARATION_LINE_BREAK, Constants.SEPARATION_REALLY_LINE_BREAK);
        String model = Constants.URL_MODEL_TRACK;
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
     * 将用户埋点的数据转换成RecordData
     *
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     * @return
     */
    private static SyncJsonData transBeanDataToSyncJsonData(DataType dataType, OP op, long time, String measurement,
                                                            final JSONObject tags, JSONObject fields)
            throws JSONException, InvalidParameterException {
        JSONObject tagsTemp = tags;
        SyncJsonData recordData = new SyncJsonData(dataType);
        recordData.setTime(time);
        JSONObject opDataJson = new JSONObject();

        if (measurement != null) {
            opDataJson.put(Constants.MEASUREMENT, measurement);
        } else {
            throw new InvalidParameterException("指标集 measurement 不能为空");
        }
        if (tagsTemp == null) {
            tagsTemp = new JSONObject();
        }
        opDataJson.put(Constants.TAGS, tagsTemp);
        if (fields != null) {
            opDataJson.put(Constants.FIELDS, fields);
        } else {
            throw new InvalidParameterException("指标集 fields 不能为空");
        }
        if (dataType == DataType.TRACK) {
            addMonitorData(tagsTemp, fields);
        }

        if (dataType == DataType.TRACK) {
            OPData opData = new OPData();
            opData.setOp(op.value);
            opData.setContent(opDataJson.toString());
            recordData.setDataString(opData.toJsonString());
        } else {
            recordData.setDataString(opDataJson.toString());

        }
        String sessionId = FTUserConfig.get().getSessionId();
        if (!Utils.isNullOrEmpty(sessionId)) {
            recordData.setSessionId(sessionId);
        }

        return recordData;
    }
}
