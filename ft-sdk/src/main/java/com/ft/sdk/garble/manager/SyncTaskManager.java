package com.ft.sdk.garble.manager;

import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.SyncCallback;
import com.ft.sdk.garble.TokenCheck;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.http.FTResponseData;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.NetCodeStatus;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.ThreadPoolUtils;
import com.ft.sdk.garble.utils.Utils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * BY huangDianHua
 * DATE:2019-12-05 20:41
 * Description:同步
 */
public class SyncTaskManager {
    private static volatile SyncTaskManager instance;
    private final int CLOSE_TIME = 5;
    private final int SLEEP_TIME = 10 * 1000;
    private volatile AtomicInteger errorCount = new AtomicInteger(0);
    private volatile boolean running;
    private volatile boolean tokenAllowable;//记录token是否合法

    /**
     * 警告!!! 该方法仅用于测试使用!!!
     *
     * @param running
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    private SyncTaskManager() {

    }

    public synchronized static SyncTaskManager get() {
        if (instance == null) {
            instance = new SyncTaskManager();
        }
        return instance;
    }

    /**
     * 触发延迟轮询同步
     */
    public void executeSyncPoll() {
        if (running) {
            return;
        }
        running = true;
        errorCount.set(0);
        ThreadPoolUtils.get().execute(() -> {
            try {
                if(!TokenCheck.get().checkToken()){
                    running = false;
                    return;
                }
                Thread.sleep(10*1000);
                List<RecordData> trackDataList = queryFromData(DataType.TRACK);
                List<RecordData> objectDataList = queryFromData(DataType.OBJECT);
                List<RecordData> logDataList = queryFromData(DataType.LOG);
                List<RecordData> keyEventDataList = queryFromData(DataType.KEY_EVENT);
                //如果打开绑定用户开关，但是没有绑定用户信息，那么就不上传用户数据，直到绑了
                if(FTUserConfig.get().isNeedBindUser() && !FTUserConfig.get().isUserDataBinded()){
                    trackDataList.clear();
                    LogUtils.e("请先绑定用户信息");
                }
                while (!trackDataList.isEmpty() || !objectDataList.isEmpty() ||
                        !logDataList.isEmpty() || !keyEventDataList.isEmpty()) {
                    if (!Utils.isNetworkAvailable()) {
                        LogUtils.d(">>>网络未连接<<<");
                        break;
                    }
                    if (errorCount.get() >= CLOSE_TIME) {
                        LogUtils.d(">>>连续同步失败5次，停止当前轮询同步<<<");
                        break;
                    }
                    if(!trackDataList.isEmpty()) {
                        handleSyncOpt(DataType.TRACK,trackDataList);
                        trackDataList = queryFromData(DataType.TRACK);
                    }
                    if(!objectDataList.isEmpty()) {
                        handleSyncOpt(DataType.OBJECT,objectDataList);
                        objectDataList = queryFromData(DataType.OBJECT);
                    }
                    if(!logDataList.isEmpty()) {
                        handleSyncOpt(DataType.LOG,logDataList);
                        logDataList = queryFromData(DataType.LOG);
                    }
                    if(!keyEventDataList.isEmpty()) {
                        handleSyncOpt(DataType.KEY_EVENT,keyEventDataList);
                        keyEventDataList = queryFromData(DataType.KEY_EVENT);
                    }
                }
                running = false;
            } catch (Exception e) {
                e.printStackTrace();
                running = false;
            }
        });
    }

    /**
     * 执行同步操作
     */
    private void handleSyncOpt(final DataType dataType, final List<RecordData> requestDatas) {
        if (requestDatas == null || requestDatas.isEmpty()) {
            return;
        }
        SyncDataManager syncDataManager = new SyncDataManager();
        String body = syncDataManager.getBodyContent(dataType, requestDatas);
        SyncDataManager.printUpdateData(dataType == DataType.OBJECT,body);
        body = body.replaceAll(Constants.SEPARATION_PRINT, Constants.SEPARATION).replaceAll(Constants.SEPARATION_LINE_BREAK,Constants.SEPARATION_REALLY_LINE_BREAK);
        requestNet(dataType, body, (code, response) -> {
            if (code >= 200 && code < 500) {
                LogUtils.d("同步数据成功");
                deleteLastQuery(requestDatas);
                errorCount.set(0);
            } else {
                LogUtils.d("同步数据失败");
                errorCount.getAndIncrement();
            }
        });
        //LogUtils.d("同步后查询" + queryFromData());
    }

    private List<RecordData> queryFromData(DataType dataType) {
        switch (dataType) {
            case LOG:
                return FTDBManager.get().queryDataByDescLimitLog(10);
            case KEY_EVENT:
                return FTDBManager.get().queryDataByDescLimitKeyEvent(10);
            case OBJECT:
                return FTDBManager.get().queryDataByDescLimitObject(10);
            default:
                return FTDBManager.get().queryDataByDescLimitTrack(10);
        }
    }

    /**
     * 删除已经上传的数据
     *
     * @param list
     */
    private void deleteLastQuery(List<RecordData> list) {
        List<String> ids = new ArrayList<>();
        for (RecordData r : list) {
            ids.add(r.getId() + "");
        }
        FTDBManager.get().delete(ids);
    }

    /**
     * 上传数据
     *
     * @param dataType
     * @param body
     * @param syncCallback
     */
    private void requestNet(DataType dataType, String body, final SyncCallback syncCallback) {
        String model = Constants.URL_MODEL_TRACK;
        switch (dataType) {
            case KEY_EVENT:
                model = Constants.URL_MODEL_KEY_EVENT;
                break;
            case LOG:
                model = Constants.URL_MODEL_LOG;
                break;
            case OBJECT:
                model = Constants.URL_MODEL_OBJECT;
                break;
            case TRACK:
                model = Constants.URL_MODEL_TRACK;
        }
        String content_type = "text/plain";
        if(DataType.OBJECT == dataType){
            content_type = "application/json";
        }
        FTResponseData result = HttpBuilder.Builder()
                .addHeadParam("Content-Type", content_type)
                .setModel(model)
                .setMethod(RequestMethod.POST)
                .setBodyString(body).executeSync(FTResponseData.class);

        try {
            syncCallback.onResponse(result.getCode(), result.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            syncCallback.onResponse(NetCodeStatus.UNKNOWN_EXCEPTION_CODE, e.getLocalizedMessage());
            LogUtils.e("同步上传错误：" + e.getLocalizedMessage());
        }

    }

    public void release() {
        ThreadPoolUtils.get().shutDown();
        instance = null;
    }
}
