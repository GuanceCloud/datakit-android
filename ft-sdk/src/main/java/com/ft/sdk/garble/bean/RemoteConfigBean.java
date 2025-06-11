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
    public static final String KEY_RUM_ENABLE_RESOURCE_HOST_IP = "rumEnableResourceHostIP";
    public static final String KEY_RUM_ENABLE_TRACK_APP_UIBLOCK = "rumEnableTrackAppUIBlock";
    public static final String KEY_RUM_BLOCK_DURATION_MS = "rumBlockDurationMs";
    public static final String KEY_RUM_ENABLE_TRACK_APP_CRASH = "rumEnableTrackAppCrash";
    public static final String KEY_RUM_ENABLE_TRACE_USER_RESOURCE = "rumEnableTraceUserResource";
    public static final String KEY_RUM_ENABLE_TRACK_APP_ANR = "rumEnableTrackAppANR";
    ;
    public static final String KEY_RUM_ENABLE_TRACE_WEB_VIEW = "rumEnableTraceWebView";
    public static final String KEY_RUM_ALLOW_WEB_VIEW_HOST = "rumAllowWebViewHost";
    public static final String KEY_LOG_SAMPLE_RATE = "logSampleRate";
    public static final String KEY_LOG_LEVEL_FILTERS = "logLevelFilters";
    public static final String KEY_LOG_ENABLE_CUSTOM_LOG = "logEnableCustomLog";
    public static final String KEY_TRACE_SAMPLE_RATE = "traceSampleRate";
    public static final String KEY_TRACE_ENABLE_AUTO_TRACE = "traceEnableAutoTrace";
    public static final String KEY_TRACE_TYPE = "traceType";
    public static final String KEY_SESSION_REPLAY_SAMPLE_RATE = "sessionReplaySampleRate";
    public static final String KEY_SESSION_SAMPLE_RATE = "sessionSampleRate";

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
    private boolean isValid = false;

    private RemoteConfigBean() {
    }

    public static RemoteConfigBean buildFromConfigJson(String json) {
        RemoteConfigBean bean = new RemoteConfigBean();
        bean.parse(json);
        return bean;
    }

    void parse(String data) {
        try {
            if (data != null) {
                JSONObject json = new JSONObject(data);
                JSONObject content = json.optJSONObject("content");
                if (json.has("md5")) {
                    this.md5 = json.optString("md5");
                }
                if (content != null) {
                    if (content.has("env")) {
                        env = content.optString("env");
                    }

                    if (content.has(KEY_SERVICE_NAME)) {
                        serviceName = content.optString(KEY_SERVICE_NAME);
                    }

                    if (content.has(KEY_AUTO_SYNC)) {
                        autoSync = content.optBoolean(KEY_AUTO_SYNC);
                    }

                    if (content.has(KEY_COMPRESS_INTAKE_REQUESTS)) {
                        compressIntakeRequests = content.optBoolean(KEY_COMPRESS_INTAKE_REQUESTS);
                    }

                    if (content.has(KEY_SYNC_PAGE_SIZE)) {
                        syncPageSize = content.optInt(KEY_SYNC_PAGE_SIZE);
                    }

                    if (content.has(KEY_SYNC_SLEEP_TIME)) {
                        syncSleepTime = content.optInt(KEY_SYNC_SLEEP_TIME);
                    }

                    if (content.has(KEY_RUM_SAMPLE_RATE)) {
                        rumSampleRate = (float) content.optDouble(KEY_RUM_SAMPLE_RATE);
                    }

                    if (content.has(KEY_RUM_SESSION_ON_ERROR_SAMPLE_RATE)) {
                        rumSessionOnErrorSampleRate = (float) content.optDouble(KEY_RUM_SESSION_ON_ERROR_SAMPLE_RATE);
                    }

                    if (content.has(KEY_RUM_ENABLE_TRACE_USER_ACTION)) {
                        rumEnableTraceUserAction = content.optBoolean(KEY_RUM_ENABLE_TRACE_USER_ACTION);
                    }

                    if (content.has(KEY_RUM_ENABLE_TRACE_USER_VIEW)) {
                        rumEnableTraceUserView = content.optBoolean(KEY_RUM_ENABLE_TRACE_USER_VIEW);
                    }

                    if (content.has(KEY_RUM_ENABLE_TRACE_USER_RESOURCE)) {
                        rumEnableTraceUserResource = content.optBoolean(KEY_RUM_ENABLE_TRACE_USER_RESOURCE);
                    }

                    if (content.has(KEY_RUM_ENABLE_RESOURCE_HOST_IP)) {
                        rumEnableResourceHostIP = content.optBoolean(KEY_RUM_ENABLE_RESOURCE_HOST_IP);
                    }

                    if (content.has(KEY_RUM_ENABLE_TRACK_APP_UIBLOCK)) {
                        rumEnableTrackAppUIBlock = content.optBoolean(KEY_RUM_ENABLE_TRACK_APP_UIBLOCK);
                    }

                    if (content.has(KEY_RUM_BLOCK_DURATION_MS)) {
                        rumBlockDurationMs = content.optLong(KEY_RUM_BLOCK_DURATION_MS);
                    }

                    if (content.has(KEY_RUM_ENABLE_TRACK_APP_CRASH)) {
                        rumEnableTrackAppCrash = content.optBoolean(KEY_RUM_ENABLE_TRACK_APP_CRASH);
                    }

                    if (content.has(KEY_RUM_ENABLE_TRACK_APP_ANR)) {
                        rumEnableTrackAppANR = content.optBoolean(KEY_RUM_ENABLE_TRACK_APP_ANR);
                    }

                    if (content.has(KEY_RUM_ENABLE_TRACE_WEB_VIEW)) {
                        rumEnableTraceWebView = content.optBoolean(KEY_RUM_ENABLE_TRACE_WEB_VIEW);
                    }

                    if (content.has(KEY_RUM_ALLOW_WEB_VIEW_HOST)) {
                        String arrayString = content.optString(KEY_RUM_ALLOW_WEB_VIEW_HOST);
                        try {
                            JSONArray array = new JSONArray(arrayString);
                            rumAllowWebViewHost = new String[array.length()];
                            for (int i = 0; i < array.length(); i++) {
                                rumAllowWebViewHost[i] = (array.optString(i));
                            }
                        } catch (Exception e) {
                            LogUtils.e(TAG, KEY_RUM_ALLOW_WEB_VIEW_HOST + " parse error ignore," + arrayString);

                        }
                    }

                    if (content.has(KEY_LOG_SAMPLE_RATE)) {
                        logSampleRate = (float) content.optDouble(KEY_LOG_SAMPLE_RATE);
                    }

                    if (content.has(KEY_LOG_LEVEL_FILTERS)) {
                        String arrayString = content.optString(KEY_LOG_LEVEL_FILTERS);
                        try {
                            JSONArray array = new JSONArray(arrayString);
                            logLevelFilters = new String[array.length()];
                            for (int i = 0; i < array.length(); i++) {
                                logLevelFilters[i] = array.optString(i);
                            }
                        } catch (Exception e) {
                            LogUtils.e(TAG, KEY_LOG_LEVEL_FILTERS + " parse error ignore," + arrayString);
                        }
                    }

                    if (content.has(KEY_LOG_ENABLE_CUSTOM_LOG)) {
                        logEnableCustomLog = content.optBoolean(KEY_LOG_ENABLE_CUSTOM_LOG);
                    }

                    if (content.has(KEY_TRACE_SAMPLE_RATE)) {
                        traceSampleRate = (float) content.optDouble(KEY_TRACE_SAMPLE_RATE);
                    }

                    if (content.has(KEY_TRACE_ENABLE_AUTO_TRACE)) {
                        traceEnableAutoTrace = content.optBoolean(KEY_TRACE_ENABLE_AUTO_TRACE);
                    }

                    if (content.has(KEY_TRACE_TYPE)) {
                        traceType = content.optString(KEY_TRACE_TYPE);
                    }

                    if (content.has(KEY_SESSION_REPLAY_SAMPLE_RATE)) {
                        sessionReplaySampleRate = (float) content.optDouble(KEY_SESSION_REPLAY_SAMPLE_RATE);
                    }

                    if (content.has(KEY_SESSION_SAMPLE_RATE)) {
                        sessionReplayOnErrorSampleRate = (float) content.optDouble(KEY_SESSION_SAMPLE_RATE);
                    }
                    isValid = true;
                }
            }
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

    public boolean isValid() {
        return isValid;
    }

}
