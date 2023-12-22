package com.ft.sdk;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTDBCachePolicy;
import com.ft.sdk.garble.bean.BaseContentBean;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.LineProtocolBean;
import com.ft.sdk.garble.bean.LogBean;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.threadpool.DataUploaderThreadPool;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * author: huangDianHua
 * time: 2020/8/7 16:37:09
 * description:内部使用的 Track 方法
 */
public class FTTrackInner {
    private final static String TAG = Constants.LOG_TAG_PREFIX + "FTTrackInner";
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
     * rum 事件数据
     *
     * @param time
     * @param measurement
     * @param tags
     * @param fields
     */
    void rum(long time, String measurement, final JSONObject tags, JSONObject fields) {
        String sessionId = tags.optString(Constants.KEY_RUM_SESSION_ID);
        if (FTRUMInnerManager.get().checkSessionWillCollect(sessionId)) {
            syncDataBackground(DataType.RUM_APP, time, measurement, tags, fields);
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
    void rumWebView(long time, String measurement, final JSONObject tags, JSONObject fields) {
        String sessionId = tags.optString(Constants.KEY_RUM_SESSION_ID);
        if (FTRUMInnerManager.get().checkSessionWillCollect(sessionId)) {
            syncDataBackground(DataType.RUM_WEBVIEW, time, measurement, tags, fields);
        }
    }

    private void syncDataBackground(final DataType dataType, final long time,
                                    final String measurement, final JSONObject tags, final JSONObject fields) {
        DataUploaderThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                try {

                    SyncJsonData recordData = SyncJsonData.getSyncJsonData(dataType,
                            new LineProtocolBean(measurement, tags, fields, time));
                    boolean result = FTDBManager.get().insertFtOperation(recordData);
                    LogUtils.d(TAG, "syncDataBackground:" + measurement + "," + dataType.toString() + ":insert=" + result);
                    SyncTaskManager.get().executeSyncPoll();
                } catch (Exception e) {
                    LogUtils.e(TAG, Log.getStackTraceString(e));
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
        ArrayList<BaseContentBean> list = new ArrayList<>();
        list.add(logBean);
        batchLogBeanBackground(list, isSilence);
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
     * 将多条日志数据存入本地同步(异步)
     *
     * @param logBeans
     */
    void batchLogBeanBackground(@NonNull List<BaseContentBean> logBeans, boolean isSilence) {
        try {
            FTLoggerConfig config = FTLoggerConfigManager.get().getConfig();
            if (config == null) return;
            JSONObject rumTags = null;
            if (config.isEnableLinkRumData()) {
                rumTags = FTRUMConfigManager.get().getRUMPublicDynamicTags(true);
                FTRUMInnerManager.get().attachRUMRelative(rumTags, false);
            }

            ArrayList<SyncJsonData> datas = new ArrayList<>();
            for (BaseContentBean logBean : logBeans) {
                try {
                    if (Utils.enableTraceSamplingRate(config.getSamplingRate())) {
                        if (rumTags != null) {
                            logBean.appendTags(rumTags);
                        }
                        datas.add(SyncJsonData.getFromLogBean(logBean, DataType.LOG));
                    } else {
                        LogUtils.d(TAG, "根据 FTLogConfig SampleRate 计算，将被丢弃=>" + logBean.getContent());
                    }
                } catch (Exception e) {
                    LogUtils.e(TAG, Log.getStackTraceString(e));
                }

            }
            judgeLogCachePolicy(datas, isSilence);
        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));
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
                for (int i = 0; i < policyStatus && i < length; i++) {
                    recordDataList.remove(0);
                }
            }
            boolean result = FTDBManager.get().insertFtOptList(recordDataList);
            LogUtils.d(TAG, "judgeLogCachePolicy:insert-result=" + result);
            if (!silence) {
                SyncTaskManager.get().executeSyncPoll();
            }
        }
    }


}
