package com.ft.sdk;


import android.content.SharedPreferences;

import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.RemoteConfigBean;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.FTDBCachePolicy;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.http.FTResponseData;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.NetCodeStatus;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.manager.RequestCallback;
import com.ft.sdk.garble.threadpool.DataProcessThreadPool;
import com.ft.sdk.garble.threadpool.DataUploadThreadPool;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.ID36Generator;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.PackageIdGenerator;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.internal.exception.FTNetworkNoAvailableException;
import com.ft.sdk.internal.exception.FTRetryLimitException;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * BY huangDianHua
 * DATE:2019-12-05 20:41
 * Description:数据同步管理，将数据存储
 */
public class SyncTaskManager {
    public final static String TAG = Constants.LOG_TAG_PREFIX + "SyncTaskManager";
    public final static int pid = android.os.Process.myPid();

    /**
     * 最大容忍错误次数
     */
    public static final int MAX_ERROR_COUNT = 5;

    /**
     * 最大同步休眠时间，ms
     */
    public static final int SYNC_SLEEP_MAX_TIME_MS = 5000;


    /**
     * 最小同步休眠时间
     */
    public static final int SYNC_SLEEP_MINI_TIME_MS = 0;


    /**
     * 传输间歇休眠时间
     */
    private static final int SLEEP_TIME = 10000;

    /**
     * 数据迁移一页面数量
     */
    private static final int OLD_CACHE_TRANSFORM_PAGE_SIZE = 100;


    /**
     * 重试等待时间
     */
    private static final int RETRY_DELAY_SLEEP_TIME = 500;
    /**
     * 统计一个周期内错误的次
     */
    private final AtomicInteger errorCount = new AtomicInteger(0);


    /**
     * RUM 数据同步包 id 标记
     */
    private final ID36Generator rumGenerator = new ID36Generator();
    /**
     * log 数据同步包 id 标记
     */
    private final ID36Generator logGenerator = new ID36Generator();


    /**
     * 是否正在在旧数据迁移
     */
    private boolean isOldCaching;

    /**
     * 是否停止
     */
    private boolean isStop = false;

    /**
     * 最大错误尝试次数，超出之后，队列数据将停止，等待下次同步触发
     */
    private int dataSyncMaxRetryCount;

    /**
     * 是否进行自动同步
     */
    private boolean autoSync;

    /**
     * 同步请求间歇时间
     */
    private int syncSleepTime;

    /**
     * 同步请求条目数量
     */
    private int pageSize = SyncPageSize.MEDIUM.getValue();

    /**
     * 旧数据迁移
     */
    private Runnable oldCacheRunner;

    /**
     * 发生错误的时间
     */
    private long errorTimeLine = -1;

    /**
     * 设置错误发生时间
     *
     * @param errorTimeLine
     */
    void setErrorTimeLine(long errorTimeLine, ViewBean activeView) {
        if (errorTimeLine > this.errorTimeLine) {
            this.errorTimeLine = errorTimeLine;
            if (activeView != null) {
                activeView.setLastErrorTime(errorTimeLine);
            }
            saveErrorWithFileCache(errorTimeLine);
        }
    }

    /**
     * 缓存错误发生时间
     *
     * @param errorTimeLine
     */
    void saveErrorWithFileCache(long errorTimeLine) {
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        sp.edit().putLong(Constants.FT_RUM_ERROR_TIMELINE, errorTimeLine).apply();
    }

    /**
     * 从缓存中获取错误发生时间
     *
     * @return
     */
    long getErrorTimeLineFromFileCache() {
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        return sp.getLong(Constants.FT_RUM_ERROR_TIMELINE, -1);
    }

    /**
     * 同步数据类型
     */
    private final static DataType[] SYNC_MAP = new DataType[]
            {
                    DataType.LOG,
                    DataType.RUM_APP,
                    DataType.RUM_WEBVIEW
            };
    /**
     * 错误采样同步类型
     */
    private final static DataType[] ERROR_SAMPLED_SYNC_MAP = new DataType[]
            {
                    DataType.RUM_APP_ERROR_SAMPLED,
                    DataType.RUM_WEBVIEW_ERROR_SAMPLED
            };

    /**
     * 注意 ：AndroidTest 会调用这个方法
     * {@link com.ft.test.base.FTBaseTest#stopSyncTask() }
     * {@link com.ft.test.base.FTBaseTest#resumeSyncTask() }
     *
     * @param running
     */
    private void setRunning(boolean running) {
        DataUploadThreadPool.get().setRunning(running);
    }

    private SyncTaskManager() {

    }

    private static class SingletonHolder {
        private static final SyncTaskManager INSTANCE = new SyncTaskManager();
    }

    public static SyncTaskManager get() {
        return SyncTaskManager.SingletonHolder.INSTANCE;
    }


    /**
     * 执行数据同步
     * <p>
     * 注意 ：AndroidTest 会调用这个方法 {@link com.ft.test.base.FTBaseTest#executeSyncTask()}
     */
    void executePoll() {
        executePoll(false);
    }

    /**
     * 执行数据同步
     *
     * @param withSleep 是否进行睡眠，{@link #SLEEP_TIME}
     */
    private void executePoll(final boolean withSleep) {
        DataUploadThreadPool.get().schedule(withSleep ? SLEEP_TIME : 0);
    }

    /**
     * 触发延迟轮询同步
     */
    void executeSyncPoll() {
        if (autoSync) {
            executePoll(true);
        }
    }

    /**
     * 过期时间
     */
    public static final long ONE_MINUTE_DURATION_NS = 60_000_000_000L;


    /**
     * 消费错误采样的缓存数据
     *
     * @param dataType
     */
    private synchronized void errorSampledConsume(DataType dataType) {
        if (errorTimeLine > 0) {
            int updateCount = FTDBManager.get().updateDataType(dataType, errorTimeLine);
            if (updateCount > 0) {
                LogUtils.d(TAG, "errorSampledConsume updateDataType:" + dataType + ","
                        + updateCount + ", before ns:" + errorTimeLine);
            }
        }
        long now = Utils.getCurrentNanoTime();
        int deleteCount = FTDBManager.get().deleteExpireCache(dataType, Utils.getCurrentNanoTime(), ONE_MINUTE_DURATION_NS);
        if (deleteCount > 0) {
            LogUtils.d(TAG, "errorSampledConsume deleteExpired:" + dataType + ","
                    + deleteCount + ", before ns:" + (now - ONE_MINUTE_DURATION_NS));
        }
    }

    /**
     * 执行存储数据同步操作
     */
    private synchronized void handleSyncOpt(final DataType dataType) throws
            FTNetworkNoAvailableException, FTRetryLimitException {
        final List<SyncData> requestDataList = new ArrayList<>();

        while (true) {
            List<SyncData> cacheDataList = queryFromData(dataType);

            requestDataList.addAll(cacheDataList);

            if (requestDataList.isEmpty()) {
                break;
            }

            int dataCount = requestDataList.size();
            LogUtils.d(TAG, "Sync Data Count:" + dataCount);

            StringBuilder sb = new StringBuilder();
            String seqNumber = "";
            if (dataType == DataType.LOG) {
                seqNumber = logGenerator.getCurrentId();
            } else if (dataType == DataType.RUM_APP || dataType == DataType.RUM_WEBVIEW) {
                seqNumber = rumGenerator.getCurrentId();
            }
            String pkgId = PackageIdGenerator.generatePackageId(seqNumber, pid, dataCount);
            for (SyncData data : cacheDataList) {
                sb.append(data.getLineProtocolDataWithPkgId(pkgId));
            }

            String body = sb.toString();
            requestNet(dataType, pkgId, body, new RequestCallback() {
                @Override
                public void onResponse(int code, String response, String errorCode) {
                    if (code >= 200 && code < 500) {
                        SyncTaskManager.this.deleteLastQuery(requestDataList);
                        if (dataType == DataType.LOG) {
                            FTDBCachePolicy.get().optLogCount(-requestDataList.size());
                        } else if (dataType == DataType.RUM_APP || dataType == DataType.RUM_WEBVIEW) {
                            FTDBCachePolicy.get().optRUMCount(-requestDataList.size());
                        }
                        errorCount.set(0);
                        if (code == 200) {
                            String innerLogFlag = "";
                            if (dataType == DataType.LOG) {
                                innerLogFlag = "log-" + logGenerator.getCurrentId();
                                logGenerator.next();
                            } else if (dataType == DataType.RUM_APP || dataType == DataType.RUM_WEBVIEW) {
                                innerLogFlag = "rum-" + rumGenerator.getCurrentId();
                                rumGenerator.next();
                            }
                            LogUtils.d(TAG, "pkg_id:" + innerLogFlag + " Sync Success-[code:" + code + ",response:" + response + "]");
                        } else {
                            LogUtils.e(TAG, "Sync Fail (Ignore)-[code:" + code + ",errorCode:" + errorCode + ",response:" + response + "]");
                        }
                    } else {
                        LogUtils.e(TAG, errorCount.incrementAndGet() + ":Sync Fail-[code:" + code + ",response:" + response + "]");

                        if (errorCount.get() > 0) {
                            try {
                                SyncTaskManager.this.reInsertData(requestDataList);
                                Thread.sleep((1L << (errorCount.get() - 1)) * RETRY_DELAY_SLEEP_TIME);
                            } catch (InterruptedException e) {
                                LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                            }
                        }
                    }
                }

            });
            requestDataList.clear();

            if (errorCount.get() == 0) {
                //当前缓存数据已获取完毕，等待下一次数据触发
                if (cacheDataList.size() < pageSize) {
                    break;
                }
                if (syncSleepTime > 0) {
                    try {
                        Thread.sleep(syncSleepTime);
                    } catch (InterruptedException e) {
                        LogUtils.d(TAG, LogUtils.getStackTraceString(e));
                    }
                }
            } else if (errorCount.get() > 0) {
                if (errorCount.get() > dataSyncMaxRetryCount) {
                    throw new FTRetryLimitException();
                } else if (dataSyncMaxRetryCount == 0) {
                    throw new FTRetryLimitException();
                }
            }
        }
    }

    /**
     * 查询对应数数据类型的数据
     *
     * @param dataType 数据类型
     * @return 同步数据
     */
    private List<SyncData> queryFromData(DataType dataType) {
        return FTDBManager.get().queryDataByDataByTypeLimit(pageSize, dataType);
    }

    /**
     * 删除已经上传的数据
     *
     * @param list
     */
    private void deleteLastQuery(List<SyncData> list) {
        deleteLastQuery(list, false);
    }

    /**
     * 删除已经上传的数据
     *
     * @param list
     */
    private void deleteLastQuery(List<SyncData> list, boolean oldCache) {
        List<String> ids = new ArrayList<>();
        for (SyncData r : list) {
            ids.add(String.valueOf(r.getId()));
        }
        FTDBManager.get().delete(ids, oldCache);
    }

    /**
     * 重新插入数据，并更改 uuid
     *
     * @param list
     */
    private void reInsertData(List<SyncData> list) {
        //删掉原先的数据
        deleteLastQuery(list);

        //重新插入数据，
        FTDBManager.get().insertFtOptList(list, true);
    }

    /**
     * 上传数据
     * <p>
     * 注意 ：AndroidTest 会调用这个方法 {@link com.ft.test.base.FTBaseTest#uploadData(DataType)}
     *
     * @param dataType     数据类型
     * @param body         数据行协议结果
     * @param pkgId        链路包 id
     * @param syncCallback 异步对象
     */
    private synchronized void requestNet(DataType dataType, String pkgId, String body,
                                         final RequestCallback syncCallback) throws FTNetworkNoAvailableException {
        String model;
        switch (dataType) {
//            case TRACE:
//                model = Constants.URL_MODEL_TRACING;
//                break;
            case LOG:
                model = Constants.URL_MODEL_LOG;
                break;
            case RUM_APP:
            case RUM_WEBVIEW:
            default:
                model = Constants.URL_MODEL_RUM;
                break;
        }
        LogUtils.d(TAG, body);
        FTResponseData result = HttpBuilder.Builder()
                .addHeadParam(Constants.SYNC_DATA_CONTENT_TYPE_HEADER, Constants.SYNC_DATA_CONTENT_TYPE_VALUE)
                .addHeadParam(Constants.SYNC_DATA_TRACE_HEADER,
                        String.format(Constants.SYNC_DATA_TRACE_HEADER_FORMAT, pkgId))
                .setModel(model)
                .setMethod(RequestMethod.POST)
                .setBodyString(body).executeSync();
        if (result.getCode() == NetCodeStatus.NETWORK_EXCEPTION_CODE) {
            throw new FTNetworkNoAvailableException();
        }

        try {
            syncCallback.onResponse(result.getCode(), result.getMessage(), result.getErrorCode());
        } catch (Exception e) {
            LogUtils.e(TAG, "requestNet：\n" + LogUtils.getStackTraceString(e));
            syncCallback.onResponse(NetCodeStatus.UNKNOWN_EXCEPTION_CODE, e.getLocalizedMessage(), "");
        }

    }

    /**
     * 旧数据迁移，1.5.0 版本以下的数据需要迁移
     */
    void oldDBDataTransform() {
        if (isOldCaching) return;

        boolean needTransform = FTDBManager.get().isOldCacheExist();

        if (needTransform) {
            LogUtils.d(TAG, "==> old cache need transform");
            isOldCaching = true;//不需要结束，一次出错等待下次启动

            DataProcessThreadPool.get().execute(new Runnable() {
                @Override
                public void run() {
                    LogUtils.d(TAG, "==> old cache transform start");
                    try {
                        SyncDataCompatHelper helper = FTTrackInner.getInstance()
                                .getCurrentDataHelper().getCompat();
                        while (true) {
                            List<SyncData> list = FTDBManager.get().queryDataByDescLimit(OLD_CACHE_TRANSFORM_PAGE_SIZE,
                                    true);
                            Iterator<SyncData> it = list.iterator();
                            while (it.hasNext()) {
                                SyncData data = it.next();
                                try {
                                    String oldFormatData = data.getDataString();//获取旧格式数据
                                    String uuid = Utils.getGUID_16();
                                    data.setUuid(uuid);//旧数据中没有 uuid
                                    data.setDataString(helper.getBodyContent(new JSONObject(oldFormatData),
                                            data.getDataType(),
                                            uuid,
                                            data.getTime()));//转化成新格式
                                } catch (Exception e) {
                                    it.remove();
                                    LogUtils.e(TAG, "==> old cache insert error");
                                }
                            }
                            FTDBManager.get().insertFtOptList(list, false);
                            deleteLastQuery(list, true);
                            if (list.size() < OLD_CACHE_TRANSFORM_PAGE_SIZE) {
                                LogUtils.d(TAG, "==> old cache transform end");
                                //不删除旧表避免 SDK 版本回退发生兼容问题
                                break;
                            }
                        }
                    } catch (Exception e) {
                        LogUtils.d(TAG, "==> oldDBDataTransform Failed:" + LogUtils.getStackTraceString(e));
                    }
                }
            });
        } else {
            LogUtils.d(TAG, "==> no old cache need transform");
        }


    }

    public void hotUpdate(RemoteConfigBean config) {
        if (config.getSyncPageSize() != null) {
            pageSize = config.getSyncPageSize();
        }
        if (config.getAutoSync() != null) {
            autoSync = config.getAutoSync();
        }
        if (config.getSyncSleepTime() != null) {
            syncSleepTime = config.getSyncSleepTime();
        }
    }

    public void init(FTSDKConfig config) {
        isStop = false;
        dataSyncMaxRetryCount = config.getDataSyncRetryCount();
        pageSize = config.getPageSize();
        autoSync = config.isAutoSync();
        syncSleepTime = config.getSyncSleepTime();
        if (config.isNeedTransformOldCache()) {
            oldCacheRunner = new Runnable() {
                @Override
                public void run() {
                    oldDBDataTransform();
                }
            };
        }

        DataProcessThreadPool.get().execute(new Runnable() {
            @Override
            public void run() {
                errorTimeLine = getErrorTimeLineFromFileCache();
//                appStartTime = Utils.getAppStartTimeNs();
            }
        });

        DataUploadThreadPool.get().initRunnable(new Runnable() {
            @Override
            public void run() {
                if (oldCacheRunner != null) {
                    oldCacheRunner.run();
                    oldCacheRunner = null;
                }
                if (isStop) {
                    return;
                }

                try {
                    for (DataType dataType : ERROR_SAMPLED_SYNC_MAP) {
                        SyncTaskManager.this.errorSampledConsume(dataType);
                    }

                    for (DataType dataType : SYNC_MAP) {
                        SyncTaskManager.this.handleSyncOpt(dataType);
                    }

                } catch (Exception e) {
                    if (e instanceof FTNetworkNoAvailableException) {
                        LogUtils.e(TAG, "Sync Fail-Network not available - Stop poll");
                    } else if (e instanceof FTRetryLimitException) {
                        if (dataSyncMaxRetryCount > 0) {
                            LogUtils.e(TAG, "Sync Fail: Reach retry limit count:" + dataSyncMaxRetryCount + "- Stop poll");
                        }
                    } else {
                        LogUtils.e(TAG, "Sync Fail:\n" + LogUtils.getStackTraceString(e));

                    }
                }

            }
        });
    }

    /**
     * 释放上传同步资源
     */
    public void release() {
        DataProcessThreadPool.get().shutDown();
        DataUploadThreadPool.get().shutDown();

        oldCacheRunner = null;
        isStop = true;
    }
}
