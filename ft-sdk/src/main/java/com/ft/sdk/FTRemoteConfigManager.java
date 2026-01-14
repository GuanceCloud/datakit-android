package com.ft.sdk;

import android.content.SharedPreferences;

import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.bean.RemoteConfigBean;
import com.ft.sdk.garble.http.FTResponseData;
import com.ft.sdk.garble.http.HttpBuilder;
import com.ft.sdk.garble.http.RequestMethod;
import com.ft.sdk.garble.threadpool.RemoteConfigThreadPool;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.Utils;

import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;

public class FTRemoteConfigManager {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "RemoteConfigManager";

    private final int remoteConfigMiniUpdateInterval;
    private String appId;
    private long lastUpdateTime;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private RemoteConfigBean mRemoteConfig;
    private final FetchResult mFetchResult;

    public FTRemoteConfigManager(int remoteConfigMiniUpdateInterval, FTRemoteConfigManager.FetchResult result) {
        this.remoteConfigMiniUpdateInterval = remoteConfigMiniUpdateInterval;
        this.mFetchResult = result;
    }

    /**
     * Returns remote configuration update result
     */
    public static abstract class FetchResult {
        /**
         * Called when remote configuration is fetched and before it's applied.
         * You can modify the configBean to override remote configuration settings.
         *
         * @param configBean The remote configuration bean, can be modified to override settings
         * @param jsonConfig config JSON string
         * @return Modified RemoteConfigBean to use, or null to use the original configBean
         */
        public RemoteConfigBean onConfigSuccessFetched(RemoteConfigBean configBean, String jsonConfig) {
            return null;
        }

        /**
         * Called after configuration is applied (for backward compatibility)
         *
         * @param success true for successful update or no data change, false for update failure
         */
        public abstract void onResult(boolean success);
    }

    public void initFromLocalCache() {
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        String configString = sp.getString(Constants.FT_REMOTE_CONFIG, null);
        if (configString != null) {
            mRemoteConfig = RemoteConfigBean.buildFromConfigJson(configString);
            LogUtils.d(TAG, "local config:" + mRemoteConfig);
        }
        lastUpdateTime = sp.getLong(Constants.FT_REMOTE_CONFIG_FETCH_TIME, 0);

    }

    private void saveRemoteConfigToLocCache(String jsonString) {
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        sp.edit().putString(Constants.FT_REMOTE_CONFIG, jsonString).apply();
    }

    private void saveLastFetchDateline(long fetchDateLine) {
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        sp.edit().putLong(Constants.FT_REMOTE_CONFIG_FETCH_TIME, fetchDateLine).apply();

        lastUpdateTime = getCurrentTimeLineInSeconds();
    }

    void mergeSDKConfigFromCache(FTSDKConfig config) {
        if (mRemoteConfig != null) {
            if (mRemoteConfig.getAutoSync() != null) {
                config.setAutoSync(mRemoteConfig.getAutoSync());
            }

            if (mRemoteConfig.getEnv() != null) {
                config.setEnv(mRemoteConfig.getEnv());
            }

            if (mRemoteConfig.getServiceName() != null) {
                config.setServiceName(mRemoteConfig.getServiceName());
            }

            if (mRemoteConfig.getCompressIntakeRequests() != null) {
                config.setCompressIntakeRequests(mRemoteConfig.getCompressIntakeRequests());
            }

            if (mRemoteConfig.getSyncPageSize() != null) {
                config.setCustomSyncPageSize(mRemoteConfig.getSyncPageSize());
            }

            if (mRemoteConfig.getSyncSleepTime() != null) {
                config.setSyncSleepTime(mRemoteConfig.getSyncSleepTime());
            }

            if (mRemoteConfig.getSyncSleepTime() != null) {
                config.setSyncSleepTime(mRemoteConfig.getSyncSleepTime());
            }
        }
    }

    void mergeRUMConfigFromCache(FTRUMConfig config) {
        if (mRemoteConfig != null) {
            if (mRemoteConfig.getRumSampleRate() != null) {
                config.setSamplingRate(mRemoteConfig.getRumSampleRate());
            }
            if (mRemoteConfig.getRumSessionOnErrorSampleRate() != null) {
                config.setSessionErrorSampleRate(mRemoteConfig.getRumSessionOnErrorSampleRate());
            }
            if (mRemoteConfig.getRumEnableTraceUserAction() != null) {
                config.setEnableTraceUserAction(mRemoteConfig.getRumEnableTraceUserAction());
            }
            if (mRemoteConfig.getRumEnableTraceUserView() != null) {
                config.setEnableTraceUserView(mRemoteConfig.getRumEnableTraceUserView());
            }
            if (mRemoteConfig.getRumEnableTraceUserResource() != null) {
                config.setEnableTraceUserResource(mRemoteConfig.getRumEnableTraceUserResource());
            }
            if (mRemoteConfig.getRumEnableResourceHostIP() != null) {
                config.setEnableResourceHostIP(mRemoteConfig.getRumEnableResourceHostIP());
            }

            if (mRemoteConfig.getRumEnableTrackAppUIBlock() != null) {
                if (mRemoteConfig.getRumBlockDurationMs() != null) {
                    config.setEnableTrackAppUIBlock(mRemoteConfig.getRumEnableTrackAppUIBlock(), mRemoteConfig.getRumBlockDurationMs());
                } else {
                    config.setEnableTrackAppUIBlock(mRemoteConfig.getRumEnableTrackAppUIBlock());
                }
            }

            if (mRemoteConfig.getRumEnableTrackAppCrash() != null) {
                config.setEnableTrackAppCrash(mRemoteConfig.getRumEnableTrackAppCrash());
            }

            if (mRemoteConfig.getRumEnableTrackAppANR() != null) {
                config.setEnableTrackAppANR(mRemoteConfig.getRumEnableTrackAppANR());
            }

            if (mRemoteConfig.getRumEnableTraceWebView() != null) {
                config.setEnableTraceWebView(mRemoteConfig.getRumEnableTraceWebView());
            }

            if (mRemoteConfig.getRumAllowWebViewHost() != null) {
                config.setAllowWebViewHost(mRemoteConfig.getRumAllowWebViewHost());
            }

        }
    }

    void mergeLogConfigFromCache(FTLoggerConfig config) {
        if (mRemoteConfig != null) {
            if (mRemoteConfig.getLogSampleRate() != null) {
                config.setSamplingRate(mRemoteConfig.getLogSampleRate());
            }

            if (mRemoteConfig.getLogLevelFilters() != null) {
                config.setLogLevelFilters(mRemoteConfig.getLogLevelFilters());
            }

            if (mRemoteConfig.getLogEnableConsoleLog() != null) {
                config.setEnableConsoleLog(mRemoteConfig.getLogEnableConsoleLog());
            }

            if (mRemoteConfig.getLogEnableCustomLog() != null) {
                config.setEnableCustomLog(mRemoteConfig.getLogEnableCustomLog());
            }
        }
    }

    void mergeTraceConfigFromCache(FTTraceConfig config) {
        if (mRemoteConfig != null) {
            if (mRemoteConfig.getTraceSampleRate() != null) {
                config.setSamplingRate(mRemoteConfig.getTraceSampleRate());
            }
            if (mRemoteConfig.getTraceEnableAutoTrace() != null) {
                config.setEnableAutoTrace(mRemoteConfig.getTraceEnableAutoTrace());
            }

            if (mRemoteConfig.getTraceType() != null) {
                for (TraceType value : TraceType.values()) {
                    if (value.value().equals(mRemoteConfig.getTraceType())) {
                        config.setTraceType(value);
                        break;
                    }
                }
            }
        }
    }


    void initFromRemote(String appId) {
        this.appId = appId;
        updateRemoteConfig();
    }


    void updateRemoteConfig() {
        updateRemoteConfig(remoteConfigMiniUpdateInterval, null);
    }

    void updateRemoteConfig(int remoteConfigMiniUpdateInterval, FetchResult result) {
        if (!running.get()) {
            if (Utils.isNullOrEmpty(appId)) {
                applyFetchResultOverride(null, result, false);
                return;
            }
            long elapsedTime = getCurrentTimeLineInSeconds() - lastUpdateTime;
            if (elapsedTime >= remoteConfigMiniUpdateInterval) {
                RemoteConfigThreadPool.get().execute(new Runnable() {
                    @Override
                    public void run() {
                        running.set(true);
                        try {
                            FTResponseData data = HttpBuilder.Builder()
                                    .addHeadParam(Constants.SYNC_DATA_CONTENT_TYPE_HEADER,
                                            Constants.SYNC_DATA_CONTENT_TYPE_VALUE)
                                    .setModel(Constants.URL_ENV_VARIABLE)
                                    .addParam(Constants.KEY_RUM_APP_ID, appId)
                                    .setMethod(RequestMethod.GET).executeSync();

                            boolean requestResult = false;
                            if (data.getCode() == HttpURLConnection.HTTP_OK) {
                                String json = data.getMessage();
                                if (!Utils.isNullOrEmpty(json)) {
                                    String md5 = Utils.toMD5(json);
                                    if (mRemoteConfig != null && md5.equals(mRemoteConfig.getMd5())) {
                                        LogUtils.d(TAG, "remote config no change");
                                        // Even if config hasn't changed, allow FetchResult to override
                                        applyFetchResultOverride(mRemoteConfig, result, true);
                                        requestResult = true;
                                    } else {
                                        String saveJson = json.replaceAll("R\\.[^.]+\\.", "");
                                        LogUtils.d(TAG, "remote config:" + saveJson);
                                        RemoteConfigBean configBean = RemoteConfigBean.buildFromConfigJson(saveJson, md5);
                                        LogUtils.d(TAG, "RemoteConfigBean config:" + configBean);
                                        if (configBean.isValid()) {
                                            mRemoteConfig.setRemoteConfigChanged(false);
                                            // Allow FetchResult to override configBean settings
                                            RemoteConfigBean finalConfigBean = applyFetchResultOverride(configBean, result,
                                                    true);
                                            saveRemoteConfigToLocCache(finalConfigBean.toJsonString());
                                            // Apply the final config
                                            notifyHotUpdate(finalConfigBean);
                                            mRemoteConfig = finalConfigBean;
                                            requestResult = true;
                                        }
                                    }
                                    saveLastFetchDateline(lastUpdateTime);
                                    running.set(false);
                                }

                            } else {
                                LogUtils.w(TAG, data.getMessage());
                            }
                            if (!requestResult) {
                                applyFetchResultOverride(null, result, false
                                );
                            }
                        } catch (Exception e) {
                            LogUtils.e(TAG, "remote config load error:" + LogUtils.getStackTraceString(e));
                        } finally {
                            running.set(false);
                        }

                    }
                });
            } else {
                long remainingTime = remoteConfigMiniUpdateInterval - elapsedTime;
                String timeFormat = formatTime(remainingTime);
                LogUtils.w(TAG, "Remote config update skipped, need to wait " + timeFormat);
            }
        }
    }

    /**
     * Apply FetchResult override to RemoteConfigBean
     * Allows FetchResult to modify the configuration before it's applied
     *
     * @param configBean  Original RemoteConfigBean from remote
     * @param fetchResult FetchResult callback (can be null)
     * @return Modified RemoteConfigBean or original if no override
     */
    private RemoteConfigBean applyFetchResultOverride(RemoteConfigBean configBean, FetchResult fetchResult,
                                                      boolean requestResult) {
        RemoteConfigBean finalConfig = configBean;

        if (fetchResult == null) {
            fetchResult = mFetchResult;
        }

        // First apply the passed-in FetchResult (if any)
        if (fetchResult != null) {
            fetchResult.onResult(requestResult);
            if (requestResult) {
                RemoteConfigBean overridden = fetchResult.onConfigSuccessFetched(configBean,
                        configBean == null ? null : configBean.getContentJsonString());
                if (overridden != null) {
                    finalConfig = overridden;
                    LogUtils.d(TAG, "FetchResult override applied from passed-in callback:" + finalConfig);
                }
            }
        }
        return finalConfig;
    }

    private long getCurrentTimeLineInSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Format seconds to human-readable time string (h m s)
     *
     * @param seconds Total seconds
     * @return Formatted time string like "1h 2m 3s" or "5m 30s" or "45s"
     */
    private String formatTime(long seconds) {
        if (seconds <= 0) {
            return "0s";
        }
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h");
        }
        if (minutes > 0) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(minutes).append("m");
        }
        if (secs > 0 || sb.length() == 0) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(secs).append("s");
        }
        return sb.toString();
    }

    private void notifyHotUpdate(RemoteConfigBean bean) {
        SyncTaskManager.get().hotUpdate(bean);
        if (bean.getCompressIntakeRequests() != null) {
            FTHttpConfigManager.get().setCompressIntakeRequests(bean.getCompressIntakeRequests());
        }
        FTLoggerConfig loggerConfig = FTLoggerConfigManager.get().getConfig();
        if (loggerConfig != null) {
            if (bean.getLogLevelFilters() != null) {
                loggerConfig.setLogLevelFilters(bean.getLogLevelFilters());
            }
            if (bean.getLogEnableCustomLog() != null) {
                loggerConfig.setEnableCustomLog(bean.getLogEnableCustomLog());
            }
            if (bean.getLogEnableCustomLog() != null) {
                loggerConfig.setEnableConsoleLog(bean.getLogEnableConsoleLog());
            }
            if (bean.getLogSampleRate() != null) {
                loggerConfig.setSamplingRate(bean.getLogSampleRate());
            }
        }

        FTTraceConfig traceConfig = FTTraceConfigManager.get().getConfig();
        if (traceConfig != null) {
            if (bean.getTraceSampleRate() != null) {
                traceConfig.setSamplingRate(bean.getTraceSampleRate());
            }
        }

        FTRUMConfig rumConfig = FTRUMConfigManager.get().getConfig();
        if (rumConfig.isRumEnable()) {
            if (bean.getRumSampleRate() != null) {
                rumConfig.setSamplingRate(bean.getRumSampleRate());
            }
            if (bean.getRumSessionOnErrorSampleRate() != null) {
                rumConfig.setSessionErrorSampleRate(bean.getSessionReplaySampleRate());
            }

            FTRUMInnerManager.get().hotUpdate(bean.getRumSampleRate(), bean.getRumSessionOnErrorSampleRate());
        }
    }

    void close() {
        RemoteConfigThreadPool.get().shutDown();
        appId = null;
        running.set(false);
    }
}
