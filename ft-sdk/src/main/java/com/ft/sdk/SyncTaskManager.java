package com.ft.sdk;


import android.content.SharedPreferences;

import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.RemoteConfigBean;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.FTDBCachePolicy;
import com.ft.sdk.garble.db.FTDataStoreManager;
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

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * BY huangDianHua
 * DATE:2019-12-05 20:41
 * Description: Data synchronization management, stores data
 */
public class SyncTaskManager {
    public final static String TAG = Constants.LOG_TAG_PREFIX + "SyncTaskManager";
    public final static int pid = android.os.Process.myPid();

    /**
     * Maximum tolerated error count
     */
    static final int MAX_ERROR_COUNT = 5;

    /**
     * Maximum sync sleep time, ms
     */
    public static final int SYNC_SLEEP_MAX_TIME_MS = 5000;

    /**
     * Minimum sync sleep time
     */
    public static final int SYNC_SLEEP_MINI_TIME_MS = 0;

    /**
     * Transmission interval sleep time
     */
    private static final int SLEEP_TIME = 10000;

    /**
     * Number of data items per page for migration
     */
    private static final int OLD_CACHE_TRANSFORM_PAGE_SIZE = 100;

    /**
     * Retry wait time
     */
    private static final int RETRY_DELAY_SLEEP_TIME = 500;

    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    /**
     * Count the number of errors per data type so one endpoint does not block others.
     */
    private final Map<DataType, AtomicInteger> errorCounts =
            new EnumMap<>(DataType.class);
    /**
     * Count consecutive ignored client errors per data type for backoff.
     */
    private final Map<DataType, AtomicInteger> ignoredClientErrorCounts =
            new EnumMap<>(DataType.class);

    /**
     * RUM data sync package id tag
     */
    private final ID36Generator rumGenerator = new ID36Generator();
    /**
     * log data sync package id tag
     */
    private final ID36Generator logGenerator = new ID36Generator();

    /**
     * Whether old data is being migrated
     */
    private boolean isOldCaching;

    /**
     * Whether to stop
     */
    private boolean isStop = false;

    /**
     * Maximum error retry count. After exceeding, queue data will stop and wait for the next sync trigger
     */
    private int dataSyncMaxRetryCount;

    /**
     * Whether to perform automatic synchronization
     */
    private boolean autoSync;

    private boolean isMainProcess;

    /**
     * Synchronization request interval time
     */
    private int syncSleepTime;

    /**
     * Synchronization request item count
     */
    private int pageSize = SyncPageSize.MEDIUM.getValue();

    /**
     * Old data migration
     */
    private Runnable oldCacheRunner;

    /**
     * Time of error occurrence
     */
    private long errorTimeLine = -1;

    /**
     * Set error occurrence time
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
     * Cache error occurrence time
     *
     * @param errorTimeLine
     */
    void saveErrorWithFileCache(long errorTimeLine) {
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        sp.edit().putLong(Constants.FT_RUM_ERROR_TIMELINE, errorTimeLine).apply();
    }

    /**
     * Get error occurrence time from cache
     *
     * @return
     */
    long getErrorTimeLineFromFileCache() {
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        return sp.getLong(Constants.FT_RUM_ERROR_TIMELINE, -1);
    }

    /**
     * Synchronization data type
     */
    private final static DataType[] SYNC_MAP = new DataType[]
            {
                    DataType.RUM_APP,
                    DataType.RUM_WEBVIEW,
                    DataType.LOG
            };

    private static final int RUM_APP_MAX_PAGES_PER_ROUND = 2;
    private static final int RUM_WEBVIEW_MAX_PAGES_PER_ROUND = 1;
    private static final int LOG_MAX_PAGES_PER_ROUND = 1;
    /**
     * Error sampled synchronization type
     */
    private final static DataType[] ERROR_SAMPLED_SYNC_MAP = new DataType[]
            {
                    DataType.RUM_APP_ERROR_SAMPLED,
                    DataType.RUM_WEBVIEW_ERROR_SAMPLED
            };

    /**
     * Note: AndroidTest will call this method
     * {@link com.ft.test.base.FTBaseTest#stopSyncTask() }
     * {@link com.ft.test.base.FTBaseTest#resumeSyncTask() }
     *
     * @param running
     */
    private void setRunning(boolean running) {
        DataUploadThreadPool.get().setRunning(running);
    }

    private SyncTaskManager() {
        initRetryStates();
    }

    private static class SingletonHolder {
        private static final SyncTaskManager INSTANCE = new SyncTaskManager();
    }

    public static SyncTaskManager get() {
        return SyncTaskManager.SingletonHolder.INSTANCE;
    }


    /**
     * Execute data synchronization
     * <p>
     * Note: AndroidTest will call this method {@link com.ft.test.base.FTBaseTest#executeSyncTask()}
     */
    void executePoll() {
        executePoll(false);
    }

    /**
     * Execute data synchronization
     *
     * @param withSleep Whether to sleep, {@link #SLEEP_TIME}
     */
    private void executePoll(final boolean withSleep) {
        DataUploadThreadPool.get().schedule(withSleep ? SLEEP_TIME : 0);
    }

    /**
     * Trigger delayed polling synchronization
     */
    void executeSyncPoll() {
        if (autoSync) {
            if (!isMainProcess) {
                LogUtils.wOnce(TAG, "Collect Data will sync on main process");
                return;
            }
            if (!FTHttpConfigManager.get().isUrlAvailable()) {
                LogUtils.wOnce(TAG, "Upload URL not configured, skipping sync");
                return;
            }
            executePoll(true);
        }
    }

    /**
     * Expiration time
     */
    public static final long ONE_MINUTE_DURATION_NS = 60_000_000_000L;


    /**
     * Consume cached data of error sampling
     *
     * @param dataType
     */
    private synchronized void errorSampledConsume(DataType dataType) {
        if (errorTimeLine > 0) {
            int updateCount = FTDataStoreManager.get().updateDataType(dataType, errorTimeLine);
            if (updateCount > 0) {
                LogUtils.d(TAG, "errorSampledConsume updateDataType:" + dataType + ","
                        + updateCount + ", before ns:" + errorTimeLine);
            }
        }
        long now = Utils.getCurrentNanoTime();
        int deleteCount = FTDataStoreManager.get().deleteExpireCache(dataType, now, ONE_MINUTE_DURATION_NS);
        if (deleteCount > 0) {
            LogUtils.d(TAG, "errorSampledConsume deleteExpired:" + dataType + ","
                    + deleteCount + ", before ns:" + (now - ONE_MINUTE_DURATION_NS));
        }
    }

    long getErrorTimeLine() {
        return errorTimeLine;
    }

    static boolean shouldBackoffIgnoredClientError(int code) {
        return code == HttpURLConnection.HTTP_FORBIDDEN || code == HTTP_TOO_MANY_REQUESTS;
    }

    static long getRetryBackoffTimeMs(int count) {
        int backoffCount = Math.max(1, count);
        return (1L << (backoffCount - 1)) * RETRY_DELAY_SLEEP_TIME;
    }

    static long getIgnoredClientErrorBackoffTimeMs(int count) {
        return getRetryBackoffTimeMs(Math.min(count, MAX_ERROR_COUNT));
    }

    private void sleepIgnoredClientErrorBackoff(DataType dataType) {
        int count = getIgnoredClientErrorCount(dataType).incrementAndGet();
        try {
            Thread.sleep(getIgnoredClientErrorBackoffTimeMs(count));
        } catch (InterruptedException e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
    }

    private void initRetryStates() {
        for (DataType dataType : DataType.values()) {
            errorCounts.put(dataType, new AtomicInteger(0));
            ignoredClientErrorCounts.put(dataType, new AtomicInteger(0));
        }
    }

    private AtomicInteger getErrorCount(DataType dataType) {
        AtomicInteger count = errorCounts.get(dataType);
        if (count == null) {
            count = new AtomicInteger(0);
            errorCounts.put(dataType, count);
        }
        return count;
    }

    private AtomicInteger getIgnoredClientErrorCount(DataType dataType) {
        AtomicInteger count = ignoredClientErrorCounts.get(dataType);
        if (count == null) {
            count = new AtomicInteger(0);
            ignoredClientErrorCounts.put(dataType, count);
        }
        return count;
    }

    static int getMaxPagesPerRound(DataType dataType) {
        switch (dataType) {
            case RUM_APP:
                return RUM_APP_MAX_PAGES_PER_ROUND;
            case RUM_WEBVIEW:
                return RUM_WEBVIEW_MAX_PAGES_PER_ROUND;
            case LOG:
            default:
                return LOG_MAX_PAGES_PER_ROUND;
        }
    }

    static DataType[] getSyncMap() {
        return SYNC_MAP.clone();
    }

    /**
     * Execute storage data synchronization operation
     */
    private synchronized SyncRoundResult handleSyncOpt(final DataType dataType,
                                                       int maxPagesPerRound) throws
            FTNetworkNoAvailableException {
        int syncPageCount = 0;
        boolean hasMoreData = false;
        AtomicInteger errorCount = getErrorCount(dataType);
        AtomicInteger ignoredClientErrorCount = getIgnoredClientErrorCount(dataType);

        while (syncPageCount < maxPagesPerRound) {
            List<SyncData> cacheDataList = queryFromData(dataType);
            final List<SyncData> requestDataList = new ArrayList<>(cacheDataList);

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
                            ignoredClientErrorCount.set(0);
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
                            if (shouldBackoffIgnoredClientError(code)) {
                                sleepIgnoredClientErrorBackoff(dataType);
                            } else {
                                ignoredClientErrorCount.set(0);
                            }
                        }
                    } else {
                        ignoredClientErrorCount.set(0);
                        LogUtils.e(TAG, errorCount.incrementAndGet() + ":Sync Fail-[code:" + code + ",response:" + response + "]");

                        if (errorCount.get() > 0) {
                            try {
                                SyncTaskManager.this.reInsertData(requestDataList);
                                Thread.sleep(getRetryBackoffTimeMs(errorCount.get()));
                            } catch (InterruptedException e) {
                                LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                            }
                        }
                    }
                }

            });

            if (errorCount.get() == 0) {
                syncPageCount++;
                //Current cache data has been obtained, waiting for next data trigger
                if (cacheDataList.size() < pageSize) {
                    hasMoreData = false;
                    break;
                }
                hasMoreData = true;
                if (syncPageCount < maxPagesPerRound && syncSleepTime > 0) {
                    try {
                        Thread.sleep(syncSleepTime);
                    } catch (InterruptedException e) {
                        LogUtils.d(TAG, LogUtils.getStackTraceString(e));
                    }
                }
            } else if (errorCount.get() > 0) {
                if (errorCount.get() > dataSyncMaxRetryCount || dataSyncMaxRetryCount == 0) {
                    if (dataSyncMaxRetryCount > 0) {
                        LogUtils.e(TAG, "Sync Fail:" + dataType + " reach retry limit count:"
                                + dataSyncMaxRetryCount + " - skip this type");
                    }
                    return new SyncRoundResult(false);
                }
            }
        }

        return new SyncRoundResult(hasMoreData);
    }

    /**
     * Query corresponding data type data
     *
     * @param dataType Data type
     * @return Synchronized data
     */
    private List<SyncData> queryFromData(DataType dataType) {
        return FTDataStoreManager.get().queryDataByDataByTypeLimit(pageSize, dataType);
    }

    /**
     * Delete already uploaded data
     *
     * @param list
     */
    private void deleteLastQuery(List<SyncData> list) {
        deleteLastQuery(list, false);
    }

    /**
     * Delete already uploaded data
     *
     * @param list
     */
    private void deleteLastQuery(List<SyncData> list, boolean oldCache) {
        List<Long> ids = new ArrayList<>();
        for (SyncData r : list) {
            ids.add(r.getId());
        }
        FTDataStoreManager.get().delete(ids, oldCache);
    }

    /**
     * Reinsert data and change uuid
     *
     * @param list
     */
    private void reInsertData(List<SyncData> list) {
        //Delete the original data
        deleteLastQuery(list);

        //Reinsert data,
        FTDataStoreManager.get().insertFtOptList(list, true);
    }

    /**
     * Upload data
     * <p>
     * Note: AndroidTest will call this method {@link com.ft.test.base.FTBaseTest#uploadData(DataType)}
     *
     * @param dataType     Data type
     * @param body         Data line protocol result
     * @param pkgId        Chain package id
     * @param syncCallback Asynchronous object
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
                .addHeadParam(Constants.SYNC_DATA_DEVICE_TIME, System.currentTimeMillis() + "")
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
     * Old data migration, data needs to be migrated for versions below 1.5.0
     */
    void oldDBDataTransform() {
        if (isOldCaching) return;

        boolean needTransform = FTDataStoreManager.get().isOldCacheExist();

        if (needTransform) {
            LogUtils.d(TAG, "==> old cache need transform");
            isOldCaching = true;//No need to end, waiting for next start after one error

            DataProcessThreadPool.get().execute(new Runnable() {
                @Override
                public void run() {
                    LogUtils.d(TAG, "==> old cache transform start");
                    try {
                        SyncDataCompatHelper helper = FTTrackInner.getInstance()
                                .getCurrentDataHelper().getCompat();
                        while (true) {
                            List<SyncData> list = FTDataStoreManager.get().queryDataByDescLimit(OLD_CACHE_TRANSFORM_PAGE_SIZE,
                                    true);
                            Iterator<SyncData> it = list.iterator();
                            while (it.hasNext()) {
                                SyncData data = it.next();
                                try {
                                    String oldFormatData = data.getDataString();//Get old format data
                                    String uuid = Utils.getGUID_16();
                                    data.setUuid(uuid);//Old data does not have uuid
                                    data.setDataString(helper.getBodyContent(new JSONObject(oldFormatData),
                                            data.getDataType(),
                                            uuid,
                                            data.getTime()));//Convert to new format
                                } catch (Exception e) {
                                    it.remove();
                                    LogUtils.e(TAG, "==> old cache insert error");
                                }
                            }
                            FTDataStoreManager.get().insertFtOptList(list, false);
                            deleteLastQuery(list, true);
                            if (list.size() < OLD_CACHE_TRANSFORM_PAGE_SIZE) {
                                LogUtils.d(TAG, "==> old cache transform end");
                                //Do not delete old table to avoid compatibility issues caused by SDK version downgrade
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
        isMainProcess = config.isMainProcess();
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

                    boolean needMoreSync = false;
                    for (DataType dataType : SYNC_MAP) {
                        SyncRoundResult result = SyncTaskManager.this.handleSyncOpt(dataType,
                                getMaxPagesPerRound(dataType));
                        needMoreSync = needMoreSync || result.hasMoreData;
                    }

                    if (needMoreSync) {
                        SyncTaskManager.this.executePoll(false);
                    }

                } catch (Exception e) {
                    if (e instanceof FTNetworkNoAvailableException) {
                        LogUtils.e(TAG, "Sync Fail-Network not available - Stop poll");
                    } else {
                        LogUtils.e(TAG, "Sync Fail:\n" + LogUtils.getStackTraceString(e));

                    }
                }

            }
        });
    }

    /**
     * Release upload synchronization resources
     */
    public void release() {
        DataProcessThreadPool.get().shutDown();
        DataUploadThreadPool.get().shutDown();

        oldCacheRunner = null;
        isStop = true;
    }

    private static class SyncRoundResult {
        final boolean hasMoreData;

        SyncRoundResult(boolean hasMoreData) {
            this.hasMoreData = hasMoreData;
        }
    }
}
