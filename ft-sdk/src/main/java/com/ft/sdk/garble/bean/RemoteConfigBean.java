package com.ft.sdk.garble.bean;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class RemoteConfigBean {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "RemoteConfigBean";
    public static final String KEY_SERVICE_NAME = "serviceName";
    public static final String KEY_AUTO_SYNC = "autoSync";
    public static final String KEY_COMPRESS_INTAKE_REQUESTS = "compressIntakeRequests";
    public static final String KEY_SYNC_PAGE_SIZE = "syncPageSize";
    public static final String KEY_SYNC_SLEEP_TIME = "syncSleepTime";
    public static final String KEY_RUM_SAMPLE_RATE = "rumSampleRate";
    public static final String KEY_RUM_SESSION_ON_ERROR_SAMPLE_RATE = "rumSessionOnErrorSampleRate";
    public static final String KEY_RUM_ENABLE_TRACE_USER_ACTION = "rumEnableTraceUserAction";
    public static final String KEY_RUM_ENABLE_TRACE_USER_VIEW = "rumEnableTraceUserView";
    public static final String KEY_RUM_ENABLE_TRACE_USER_RESOURCE = "rumEnableTraceUserResource";
    public static final String KEY_RUM_ENABLE_RESOURCE_HOST_IP = "rumEnableResourceHostIP";
    public static final String KEY_RUM_ENABLE_TRACK_APP_UIBLOCK = "rumEnableTrackAppUIBlock";
    public static final String KEY_RUM_BLOCK_DURATION_MS = "rumBlockDurationMs";
    public static final String KEY_RUM_ENABLE_TRACK_APP_CRASH = "rumEnableTrackAppCrash";
    public static final String KEY_RUM_ENABLE_TRACK_APP_ANR = "rumEnableTrackAppANR";
    public static final String KEY_RUM_ENABLE_TRACE_WEB_VIEW = "rumEnableTraceWebView";
    public static final String KEY_RUM_ALLOW_WEB_VIEW_HOST = "rumAllowWebViewHost";
    public static final String KEY_LOG_SAMPLE_RATE = "logSampleRate";
    public static final String KEY_LOG_LEVEL_FILTERS = "logLevelFilters";
    public static final String KEY_LOG_ENABLE_CUSTOM_LOG = "logEnableCustomLog";
    public static final String KEY_LOG_ENABLE_CONSOLE_LOG = "logEnableConsoleLog";
    public static final String KEY_TRACE_SAMPLE_RATE = "traceSampleRate";
    public static final String KEY_TRACE_ENABLE_AUTO_TRACE = "traceEnableAutoTrace";
    public static final String KEY_TRACE_TYPE = "traceType";
    public static final String KEY_SESSION_REPLAY_SAMPLE_RATE = "sessionReplaySampleRate";
    public static final String KEY_SESSION_REPLAY_ON_ERROR_SAMPLE_RATE = "sessionReplayOnErrorSampleRate";
    public static final String KEY_ENV = "env";
    public static final String KEY_MD5 = "md5";

    private String env;
    private String serviceName;
    private Boolean autoSync;
    private Boolean compressIntakeRequests;
    private Integer syncPageSize;
    private Integer syncSleepTime;

    private Float rumSampleRate;
    private Float rumSessionOnErrorSampleRate;
    private Boolean rumEnableTraceUserAction;
    private Boolean rumEnableTraceUserView;
    private Boolean rumEnableTraceUserResource;
    private Boolean rumEnableResourceHostIP;
    private Boolean rumEnableTrackAppUIBlock;
    private Long rumBlockDurationMs;
    private Boolean rumEnableTrackAppCrash;
    private Boolean rumEnableTrackAppANR;
    private Boolean rumEnableTraceWebView;
    private String[] rumAllowWebViewHost;

    private Float logSampleRate;
    private String[] logLevelFilters;
    private Boolean logEnableCustomLog;
    private Boolean logEnableConsoleLog;

    private Float traceSampleRate;
    private Boolean traceEnableAutoTrace;
    private String traceType;

    private Float sessionReplaySampleRate;
    private Float sessionReplayOnErrorSampleRate;
    private String md5;
    private boolean remoteConfigChanged;
    private boolean isValid = false;
    private String contentJsonString;

    private RemoteConfigBean() {
    }

    public static RemoteConfigBean buildFromConfigJson(String json) {
        RemoteConfigBean bean = new RemoteConfigBean();
        bean.parse(json);
        return bean;
    }

    /**
     * Build RemoteConfigBean from JSON string and set the specified md5 value
     *
     * @param json JSON string
     * @param md5  MD5 value
     * @return RemoteConfigBean object
     */
    public static RemoteConfigBean buildFromConfigJson(String json, String md5) {
        RemoteConfigBean bean = new RemoteConfigBean();
        bean.parse(json);
        if (md5 != null) {
            bean.md5 = md5;
        }
        return bean;
    }

    private boolean isBoolean(JSONObject obj, String key) {
        return obj.has(key) && obj.opt(key) instanceof Boolean;
    }

    private boolean isNumber(JSONObject obj, String key) {
        return obj.has(key) && obj.opt(key) instanceof Number;
    }

    private boolean isString(JSONObject obj, String key) {
        return obj.has(key) && obj.opt(key) instanceof String;
    }

    void parse(String data) {
        try {
            if (data == null) return;

            JSONObject json = new JSONObject(data);
            if (isString(json, KEY_MD5)) {
                this.md5 = json.optString(KEY_MD5);
            }

            JSONObject content = json.optJSONObject("content");
            if (content == null) return;
            this.contentJsonString = content.toString();

            if (isString(content, KEY_ENV)) env = content.optString(KEY_ENV);
            if (isString(content, KEY_SERVICE_NAME)) serviceName = content.optString(KEY_SERVICE_NAME);
            if (isBoolean(content, KEY_AUTO_SYNC)) autoSync = content.optBoolean(KEY_AUTO_SYNC);
            if (isBoolean(content, KEY_COMPRESS_INTAKE_REQUESTS)) compressIntakeRequests = content.optBoolean(KEY_COMPRESS_INTAKE_REQUESTS);
            if (isNumber(content, KEY_SYNC_PAGE_SIZE)) syncPageSize = content.optInt(KEY_SYNC_PAGE_SIZE);
            if (isNumber(content, KEY_SYNC_SLEEP_TIME)) syncSleepTime = content.optInt(KEY_SYNC_SLEEP_TIME);
            if (isNumber(content, KEY_RUM_SAMPLE_RATE)) rumSampleRate = (float) content.optDouble(KEY_RUM_SAMPLE_RATE);
            if (isNumber(content, KEY_RUM_SESSION_ON_ERROR_SAMPLE_RATE)) rumSessionOnErrorSampleRate = (float) content.optDouble(KEY_RUM_SESSION_ON_ERROR_SAMPLE_RATE);
            if (isBoolean(content, KEY_RUM_ENABLE_TRACE_USER_ACTION)) rumEnableTraceUserAction = content.optBoolean(KEY_RUM_ENABLE_TRACE_USER_ACTION);
            if (isBoolean(content, KEY_RUM_ENABLE_TRACE_USER_VIEW)) rumEnableTraceUserView = content.optBoolean(KEY_RUM_ENABLE_TRACE_USER_VIEW);
            if (isBoolean(content, KEY_RUM_ENABLE_TRACE_USER_RESOURCE)) rumEnableTraceUserResource = content.optBoolean(KEY_RUM_ENABLE_TRACE_USER_RESOURCE);
            if (isBoolean(content, KEY_RUM_ENABLE_RESOURCE_HOST_IP)) rumEnableResourceHostIP = content.optBoolean(KEY_RUM_ENABLE_RESOURCE_HOST_IP);
            if (isBoolean(content, KEY_RUM_ENABLE_TRACK_APP_UIBLOCK)) rumEnableTrackAppUIBlock = content.optBoolean(KEY_RUM_ENABLE_TRACK_APP_UIBLOCK);
            if (isNumber(content, KEY_RUM_BLOCK_DURATION_MS)) rumBlockDurationMs = content.optLong(KEY_RUM_BLOCK_DURATION_MS);
            if (isBoolean(content, KEY_RUM_ENABLE_TRACK_APP_CRASH)) rumEnableTrackAppCrash = content.optBoolean(KEY_RUM_ENABLE_TRACK_APP_CRASH);
            if (isBoolean(content, KEY_RUM_ENABLE_TRACK_APP_ANR)) rumEnableTrackAppANR = content.optBoolean(KEY_RUM_ENABLE_TRACK_APP_ANR);
            if (isBoolean(content, KEY_RUM_ENABLE_TRACE_WEB_VIEW)) rumEnableTraceWebView = content.optBoolean(KEY_RUM_ENABLE_TRACE_WEB_VIEW);

            Object webViewHostObj = content.opt(KEY_RUM_ALLOW_WEB_VIEW_HOST);
            if (webViewHostObj instanceof String) {
                try {
                    JSONArray array = new JSONArray((String) webViewHostObj);
                    rumAllowWebViewHost = new String[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        rumAllowWebViewHost[i] = array.optString(i);
                    }
                } catch (Exception e) {
                    LogUtils.e(TAG, KEY_RUM_ALLOW_WEB_VIEW_HOST + " parse error ignore," + webViewHostObj);
                }
            } else if (webViewHostObj instanceof JSONArray) {
                JSONArray array = (JSONArray) webViewHostObj;
                rumAllowWebViewHost = new String[array.length()];
                for (int i = 0; i < array.length(); i++) {
                    rumAllowWebViewHost[i] = array.optString(i);
                }
            } else if (webViewHostObj != null) {
                LogUtils.e(TAG, KEY_RUM_ALLOW_WEB_VIEW_HOST + " is not a valid JSONArray or String");
            }

            if (isNumber(content, KEY_LOG_SAMPLE_RATE)) logSampleRate = (float) content.optDouble(KEY_LOG_SAMPLE_RATE);

            Object logFilterObj = content.opt(KEY_LOG_LEVEL_FILTERS);
            if (logFilterObj instanceof String) {
                try {
                    JSONArray array = new JSONArray((String) logFilterObj);
                    logLevelFilters = new String[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        logLevelFilters[i] = array.optString(i);
                    }
                } catch (Exception e) {
                    LogUtils.e(TAG, KEY_LOG_LEVEL_FILTERS + " parse error ignore," + logFilterObj);
                }
            } else if (logFilterObj instanceof JSONArray) {
                JSONArray array = (JSONArray) logFilterObj;
                logLevelFilters = new String[array.length()];
                for (int i = 0; i < array.length(); i++) {
                    logLevelFilters[i] = array.optString(i);
                }
            } else if (logFilterObj != null) {
                LogUtils.e(TAG, KEY_LOG_LEVEL_FILTERS + " is not a valid JSONArray or String");
            }

            if (isBoolean(content, KEY_LOG_ENABLE_CUSTOM_LOG)) logEnableCustomLog = content.optBoolean(KEY_LOG_ENABLE_CUSTOM_LOG);
            if (isBoolean(content, KEY_LOG_ENABLE_CONSOLE_LOG)) logEnableConsoleLog = content.optBoolean(KEY_LOG_ENABLE_CONSOLE_LOG);
            if (isNumber(content, KEY_TRACE_SAMPLE_RATE)) traceSampleRate = (float) content.optDouble(KEY_TRACE_SAMPLE_RATE);
            if (isBoolean(content, KEY_TRACE_ENABLE_AUTO_TRACE)) traceEnableAutoTrace = content.optBoolean(KEY_TRACE_ENABLE_AUTO_TRACE);
            if (isString(content, KEY_TRACE_TYPE)) traceType = content.optString(KEY_TRACE_TYPE);
            if (isNumber(content, KEY_SESSION_REPLAY_SAMPLE_RATE)) sessionReplaySampleRate = (float) content.optDouble(KEY_SESSION_REPLAY_SAMPLE_RATE);
            if (isNumber(content, KEY_SESSION_REPLAY_ON_ERROR_SAMPLE_RATE)) sessionReplayOnErrorSampleRate = (float) content.optDouble(KEY_SESSION_REPLAY_ON_ERROR_SAMPLE_RATE);

            isValid = true;
        } catch (Exception e) {
            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
        }
    }

    public String getEnv() {
        return env;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Boolean getAutoSync() {
        return autoSync;
    }

    public Boolean getCompressIntakeRequests() {
        return compressIntakeRequests;
    }

    public Integer getSyncPageSize() {
        return syncPageSize;
    }

    public Integer getSyncSleepTime() {
        return syncSleepTime;
    }

    public Float getRumSampleRate() {
        return rumSampleRate;
    }

    public Float getRumSessionOnErrorSampleRate() {
        return rumSessionOnErrorSampleRate;
    }

    public Boolean getRumEnableTraceUserAction() {
        return rumEnableTraceUserAction;
    }

    public Boolean getRumEnableTraceUserView() {
        return rumEnableTraceUserView;
    }

    public Boolean getRumEnableResourceHostIP() {
        return rumEnableResourceHostIP;
    }

    public Boolean getRumEnableTrackAppUIBlock() {
        return rumEnableTrackAppUIBlock;
    }

    public Long getRumBlockDurationMs() {
        return rumBlockDurationMs;
    }

    public Boolean getRumEnableTrackAppCrash() {
        return rumEnableTrackAppCrash;
    }

    public Boolean getRumEnableTraceWebView() {
        return rumEnableTraceWebView;
    }

    public String[] getRumAllowWebViewHost() {
        return rumAllowWebViewHost;
    }

    public Float getLogSampleRate() {
        return logSampleRate;
    }

    public String[] getLogLevelFilters() {
        return logLevelFilters;
    }

    public Boolean getLogEnableCustomLog() {
        return logEnableCustomLog;
    }

    public Float getTraceSampleRate() {
        return traceSampleRate;
    }

    public Boolean getTraceEnableAutoTrace() {
        return traceEnableAutoTrace;
    }

    public String getTraceType() {
        return traceType;
    }

    public Float getSessionReplaySampleRate() {
        return sessionReplaySampleRate;
    }

    public Float getSessionReplayOnErrorSampleRate() {
        return sessionReplayOnErrorSampleRate;
    }

    public String getMd5() {
        return md5;
    }

    public String getContentJsonString() {
        return contentJsonString;
    }

    public void setSessionReplayOnErrorSampleRate(Float sessionReplayOnErrorSampleRate) {
        this.sessionReplayOnErrorSampleRate = sessionReplayOnErrorSampleRate;
    }

    public void setSessionReplaySampleRate(Float sessionReplaySampleRate) {
        this.sessionReplaySampleRate = sessionReplaySampleRate;
    }

    public void setTraceType(String traceType) {
        this.traceType = traceType;
    }

    public void setTraceEnableAutoTrace(Boolean traceEnableAutoTrace) {
        this.traceEnableAutoTrace = traceEnableAutoTrace;
    }

    public void setTraceSampleRate(Float traceSampleRate) {
        this.traceSampleRate = traceSampleRate;
    }

    public void setLogEnableCustomLog(Boolean logEnableCustomLog) {
        this.logEnableCustomLog = logEnableCustomLog;
    }

    public void setLogLevelFilters(String[] logLevelFilters) {
        this.logLevelFilters = logLevelFilters;
    }

    public void setLogSampleRate(Float logSampleRate) {
        this.logSampleRate = logSampleRate;
    }

    public void setRumAllowWebViewHost(String[] rumAllowWebViewHost) {
        this.rumAllowWebViewHost = rumAllowWebViewHost;
    }

    public void setRumEnableTraceWebView(Boolean rumEnableTraceWebView) {
        this.rumEnableTraceWebView = rumEnableTraceWebView;
    }

    public void setRumEnableTrackAppCrash(Boolean rumEnableTrackAppCrash) {
        this.rumEnableTrackAppCrash = rumEnableTrackAppCrash;
    }

    public void setRumBlockDurationMs(Long rumBlockDurationMs) {
        this.rumBlockDurationMs = rumBlockDurationMs;
    }

    public void setRumEnableTrackAppUIBlock(Boolean rumEnableTrackAppUIBlock) {
        this.rumEnableTrackAppUIBlock = rumEnableTrackAppUIBlock;
    }

    public void setRumEnableResourceHostIP(Boolean rumEnableResourceHostIP) {
        this.rumEnableResourceHostIP = rumEnableResourceHostIP;
    }

    public void setRumEnableTraceUserView(Boolean rumEnableTraceUserView) {
        this.rumEnableTraceUserView = rumEnableTraceUserView;
    }

    public void setRumEnableTraceUserAction(Boolean rumEnableTraceUserAction) {
        this.rumEnableTraceUserAction = rumEnableTraceUserAction;
    }

    public void setRumSessionOnErrorSampleRate(Float rumSessionOnErrorSampleRate) {
        this.rumSessionOnErrorSampleRate = rumSessionOnErrorSampleRate;
    }

    public void setRumSampleRate(Float rumSampleRate) {
        this.rumSampleRate = rumSampleRate;
    }

    public void setSyncSleepTime(Integer syncSleepTime) {
        this.syncSleepTime = syncSleepTime;
    }

    public void setSyncPageSize(Integer syncPageSize) {
        this.syncPageSize = syncPageSize;
    }

    public void setCompressIntakeRequests(Boolean compressIntakeRequests) {
        this.compressIntakeRequests = compressIntakeRequests;
    }

    public void setAutoSync(Boolean autoSync) {
        this.autoSync = autoSync;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RemoteConfigBean{");
        if (env != null) sb.append("env='").append(env).append("', ");
        if (serviceName != null) sb.append("serviceName='").append(serviceName).append("', ");
        if (autoSync != null) sb.append("autoSync='").append(autoSync).append("', ");
        if (compressIntakeRequests != null)
            sb.append("compressIntakeRequests='").append(compressIntakeRequests).append("', ");
        if (syncPageSize != null) sb.append("syncPageSize='").append(syncPageSize).append("', ");
        if (syncSleepTime != null) sb.append("syncSleepTime='").append(syncSleepTime).append("', ");
        if (rumSampleRate != null) sb.append("rumSampleRate='").append(rumSampleRate).append("', ");
        if (rumSessionOnErrorSampleRate != null)
            sb.append("rumSessionOnErrorSampleRate='").append(rumSessionOnErrorSampleRate).append("', ");
        if (rumEnableTraceUserAction != null)
            sb.append("rumEnableTraceUserAction='").append(rumEnableTraceUserAction).append("', ");
        if (rumEnableTraceUserView != null)
            sb.append("rumEnableTraceUserView='").append(rumEnableTraceUserView).append("', ");
        if (rumEnableTraceUserResource != null)
            sb.append("rumEnableTraceUserResource='").append(rumEnableTraceUserResource).append("', ");
        if (rumEnableResourceHostIP != null)
            sb.append("rumEnableResourceHostIP='").append(rumEnableResourceHostIP).append("', ");
        if (rumEnableTrackAppUIBlock != null)
            sb.append("rumEnableTrackAppUIBlock='").append(rumEnableTrackAppUIBlock).append("', ");
        if (rumBlockDurationMs != null)
            sb.append("rumBlockDurationMs='").append(rumBlockDurationMs).append("', ");
        if (rumEnableTrackAppCrash != null)
            sb.append("rumEnableTrackAppCrash='").append(rumEnableTrackAppCrash).append("', ");
        if (rumEnableTraceWebView != null)
            sb.append("rumEnableTraceWebView='").append(rumEnableTraceWebView).append("', ");
        if (rumAllowWebViewHost != null)
            sb.append("rumAllowWebViewHost='").append(rumAllowWebViewHost).append("', ");
        if (logSampleRate != null) sb.append("logSampleRate='").append(logSampleRate).append("', ");
        if (logLevelFilters != null)
            sb.append("logLevelFilters='").append(logLevelFilters).append("', ");
        if (logEnableCustomLog != null)
            sb.append("logEnableCustomLog='").append(logEnableCustomLog).append("', ");
        if (traceSampleRate != null)
            sb.append("traceSampleRate='").append(traceSampleRate).append("', ");
        if (traceEnableAutoTrace != null)
            sb.append("traceEnableAutoTrace='").append(traceEnableAutoTrace).append("', ");
        if (traceType != null) sb.append("traceType='").append(traceType).append("', ");
        if (sessionReplaySampleRate != null)
            sb.append("sessionReplaySampleRate='").append(sessionReplaySampleRate).append("', ");
        if (sessionReplayOnErrorSampleRate != null)
            sb.append("sessionReplayOnErrorSampleRate='").append(sessionReplayOnErrorSampleRate).append("', ");
        if (md5 != null) sb.append("md5='").append(md5);
        sb.append("}");
        return sb.toString();
    }


    public Boolean getRumEnableTraceUserResource() {
        return rumEnableTraceUserResource;
    }

    public void setRumEnableTraceUserResource(Boolean rumEnableTraceUserResource) {
        this.rumEnableTraceUserResource = rumEnableTraceUserResource;
    }

    public Boolean getRumEnableTrackAppANR() {
        return rumEnableTrackAppANR;
    }

    public void setRumEnableTrackAppANR(Boolean rumEnableTrackAppANR) {
        this.rumEnableTrackAppANR = rumEnableTrackAppANR;
    }

    public Boolean getLogEnableConsoleLog() {
        return logEnableConsoleLog;
    }

    public void setLogEnableConsoleLog(Boolean logEnableConsoleLog) {
        this.logEnableConsoleLog = logEnableConsoleLog;
    }

    public void setRemoteConfigChanged(boolean remoteConfigChanged) {
        this.remoteConfigChanged = remoteConfigChanged;
    }

    public boolean isRemoteConfigChanged() {
        return remoteConfigChanged;
    }

    public boolean isValid() {
        return isValid;
    }

    /**
     * Convert RemoteConfigBean object to JSON string
     * Preserves unknown fields from the original JSON (e.g., custom_userid)
     *
     * @return JSON string, returns null if conversion fails
     */
    public String toJsonString() {
        try {
            JSONObject json = new JSONObject();

            // Add content object - preserve original JSON structure if available
            JSONObject content;
            if (contentJsonString != null && !contentJsonString.isEmpty()) {
                try {
                    // Start with original content to preserve unknown fields
                    content = new JSONObject(contentJsonString);
                } catch (Exception e) {
                    // If parsing original content fails, start with empty object
                    content = new JSONObject();
                }
            } else {
                content = new JSONObject();
            }

            // Update known fields with current values (may override original values)
            if (env != null) {
                content.put(KEY_ENV, env);
            }
            if (serviceName != null) {
                content.put(KEY_SERVICE_NAME, serviceName);
            }
            if (autoSync != null) {
                content.put(KEY_AUTO_SYNC, autoSync);
            }
            if (compressIntakeRequests != null) {
                content.put(KEY_COMPRESS_INTAKE_REQUESTS, compressIntakeRequests);
            }
            if (syncPageSize != null) {
                content.put(KEY_SYNC_PAGE_SIZE, syncPageSize);
            }
            if (syncSleepTime != null) {
                content.put(KEY_SYNC_SLEEP_TIME, syncSleepTime);
            }
            if (rumSampleRate != null) {
                content.put(KEY_RUM_SAMPLE_RATE, rumSampleRate);
            }
            if (rumSessionOnErrorSampleRate != null) {
                content.put(KEY_RUM_SESSION_ON_ERROR_SAMPLE_RATE, rumSessionOnErrorSampleRate);
            }
            if (rumEnableTraceUserAction != null) {
                content.put(KEY_RUM_ENABLE_TRACE_USER_ACTION, rumEnableTraceUserAction);
            }
            if (rumEnableTraceUserView != null) {
                content.put(KEY_RUM_ENABLE_TRACE_USER_VIEW, rumEnableTraceUserView);
            }
            if (rumEnableTraceUserResource != null) {
                content.put(KEY_RUM_ENABLE_TRACE_USER_RESOURCE, rumEnableTraceUserResource);
            }
            if (rumEnableResourceHostIP != null) {
                content.put(KEY_RUM_ENABLE_RESOURCE_HOST_IP, rumEnableResourceHostIP);
            }
            if (rumEnableTrackAppUIBlock != null) {
                content.put(KEY_RUM_ENABLE_TRACK_APP_UIBLOCK, rumEnableTrackAppUIBlock);
            }
            if (rumBlockDurationMs != null) {
                content.put(KEY_RUM_BLOCK_DURATION_MS, rumBlockDurationMs);
            }
            if (rumEnableTrackAppCrash != null) {
                content.put(KEY_RUM_ENABLE_TRACK_APP_CRASH, rumEnableTrackAppCrash);
            }
            if (rumEnableTrackAppANR != null) {
                content.put(KEY_RUM_ENABLE_TRACK_APP_ANR, rumEnableTrackAppANR);
            }
            if (rumEnableTraceWebView != null) {
                content.put(KEY_RUM_ENABLE_TRACE_WEB_VIEW, rumEnableTraceWebView);
            }
            if (rumAllowWebViewHost != null) {
                JSONArray webViewHostArray = new JSONArray();
                for (String host : rumAllowWebViewHost) {
                    webViewHostArray.put(host);
                }
                content.put(KEY_RUM_ALLOW_WEB_VIEW_HOST, webViewHostArray);
            }
            if (logSampleRate != null) {
                content.put(KEY_LOG_SAMPLE_RATE, logSampleRate);
            }
            if (logLevelFilters != null) {
                JSONArray logFilterArray = new JSONArray();
                for (String filter : logLevelFilters) {
                    logFilterArray.put(filter);
                }
                content.put(KEY_LOG_LEVEL_FILTERS, logFilterArray);
            }
            if (logEnableCustomLog != null) {
                content.put(KEY_LOG_ENABLE_CUSTOM_LOG, logEnableCustomLog);
            }
            if (logEnableConsoleLog != null) {
                content.put(KEY_LOG_ENABLE_CONSOLE_LOG, logEnableConsoleLog);
            }
            if (traceSampleRate != null) {
                content.put(KEY_TRACE_SAMPLE_RATE, traceSampleRate);
            }
            if (traceEnableAutoTrace != null) {
                content.put(KEY_TRACE_ENABLE_AUTO_TRACE, traceEnableAutoTrace);
            }
            if (traceType != null) {
                content.put(KEY_TRACE_TYPE, traceType);
            }
            if (sessionReplaySampleRate != null) {
                content.put(KEY_SESSION_REPLAY_SAMPLE_RATE, sessionReplaySampleRate);
            }
            if (sessionReplayOnErrorSampleRate != null) {
                content.put(KEY_SESSION_REPLAY_ON_ERROR_SAMPLE_RATE, sessionReplayOnErrorSampleRate);
            }

            json.put("content", content);

            // Add md5 to root object if present
            if (md5 != null) {
                json.put(KEY_MD5, md5);
            }

            return json.toString();
        } catch (Exception e) {
            LogUtils.e(TAG, "toJsonString error: " + LogUtils.getStackTraceString(e));
            return null;
        }
    }

}
