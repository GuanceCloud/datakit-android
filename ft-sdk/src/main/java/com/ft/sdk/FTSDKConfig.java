package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.Constants;

import java.net.Proxy;
import java.util.HashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-06 11:40
 * Description:
 */
public class FTSDKConfig {
    /**
     * datakit data write address
     */
    private final String datakitUrl;

    /**
     * dataway data write address
     */
    private final String datawayUrl;

    /**
     *
     */
    private final String clientToken;
    /**
     * Whether to enable Debug
     */
    private boolean isDebug;

    private SDKLogLevel sdkLogLevel;


    /**
     * db cache size limit
     */
    private long dbCacheLimit = Constants.DEFAULT_DB_SIZE_LIMIT;

    /**
     * Whether to enable db cache limit
     */
    private boolean limitWithDbSize = false;

    /**
     *
     */
    private DBCacheDiscard dbCacheDiscard = DBCacheDiscard.DISCARD;

    /**
     * Set log level, default {@link SDKLogLevel#V}
     *
     * @param logLevel
     * @return
     */
    public FTSDKConfig setSdkLogLevel(SDKLogLevel logLevel) {
        this.sdkLogLevel = logLevel;
        return this;
    }

    /**
     * Whether Android ID can be accessed
     */
    private boolean enableAccessAndroidID = true;


    /**
     * Whether to perform automatic synchronization
     */
    private boolean autoSync = true;

    private int pageSize = SyncPageSize.MEDIUM.getValue();

    private int syncSleepTime = 0;
    /**
     * Deflate compression for synchronized data, default is off
     */
    private boolean compressIntakeRequests = false;

    /**
     * Service name {@link Constants#KEY_SERVICE }, default is {@link Constants#DEFAULT_SERVICE_NAME}
     */
    private String serviceName = Constants.DEFAULT_SERVICE_NAME;

    /**
     * Maximum retry count for data synchronization
     */
    private int dataSyncRetryCount = SyncTaskManager.MAX_ERROR_COUNT;

    /**
     * Data upload environment
     */
    private String env = EnvType.PROD.toString();

    /**
     * Whether the SDK only supports initialization in the main process
     * {@link FTSDKConfig#setOnlySupportMainProcess(boolean)}
     */
    private boolean onlySupportMainProcess = true;


    /**
     * Line protocol compatibility mode, integer data compatibility mode, handles web data type conflicts, enabled by default.
     */
    private boolean enableDataIntegerCompatible = true;

    /**
     * Whether to migrate old data, SDK needs to migrate data when upgrading from an old version to 1.5.0
     */
    private boolean needTransformOldCache = false;


    public long getDbCacheLimit() {
        return dbCacheLimit;
    }

    /**
     * Enable db size limit, default 100MB, {@link Constants#DEFAULT_DB_SIZE_LIMIT}
     */
    public FTSDKConfig enableLimitWithDbSize() {
        this.limitWithDbSize = true;
        return this;
    }

    /**
     * Enable db size limit, default 100MB, unit byte, {@link Constants#DEFAULT_DB_SIZE_LIMIT}
     *
     * @param dbSize Set db size limit, the larger the database, the greater the disk pressure, [30MB,), default 100 MB
     *               <p>
     *               After enabling db data limit, {@link FTLoggerConfig#setLogCacheLimitCount(int)}
     *               and {@link FTRUMConfig#setRumCacheLimitCount(int)} will be invalid
     * @return
     */
    public FTSDKConfig enableLimitWithDbSize(long dbSize) {
        this.dbCacheLimit = Math.max(Constants.MINI_DB_SIZE_LIMIT, dbSize);
//        this.dbCacheLimit = dbSize;
        this.limitWithDbSize = true;
        return this;
    }

    public boolean isLimitWithDbSize() {
        return limitWithDbSize;
    }

    /**
     * Set db cache discard strategy
     *
     * @param dbCacheDiscard
     * @return
     */
    public FTSDKConfig setDbCacheDiscard(DBCacheDiscard dbCacheDiscard) {
        this.dbCacheDiscard = dbCacheDiscard;
        return this;
    }

    public DBCacheDiscard getDbCacheDiscard() {
        return dbCacheDiscard;
    }


    private boolean remoteConfiguration;

    private boolean getRemoteConfiguration() {
        return remoteConfiguration;
    }

    /**
     * Whether to enable remote configuration for data collection, default is off.
     * When enabled, SDK initialization or app hot start will trigger data update.
     * @param remoteConfiguration
     * @return
     */
    public FTSDKConfig setRemoteConfiguration(boolean remoteConfiguration) {
        this.remoteConfiguration = remoteConfiguration;
        return this;
    }

    private int remoteConfigMiniUpdateInterval = 43200;//12 hour

    /**
     * Set the minimum interval for data update, unit: seconds, default 12 hours
     * @param remoteConfigMiniUpdateInterval
     * @return
     */
    public FTSDKConfig setRemoteConfigMiniUpdateInterval(int remoteConfigMiniUpdateInterval) {
        this.remoteConfigMiniUpdateInterval = remoteConfigMiniUpdateInterval;
        return this;
    }

    public boolean isRemoteConfiguration() {
        return remoteConfiguration;
    }

    public int getRemoteConfigMiniUpdateInterval() {
        return remoteConfigMiniUpdateInterval;
    }

    /**
     * Global parameters, such as {@link Constants#KEY_APP_VERSION_NAME} and other fixed configuration parameters,
     * or user-defined variable parameters added via {@link FTSDKConfig#addGlobalContext(String, String)}
     */
    private final HashMap<String, Object> globalContext = new HashMap<>();

    private final HashMap<String, String> pkgInfo = new HashMap<>();

    private Object dns;
    private Proxy proxy;
    private Object authenticator;

    /**
     * Build necessary SDK configuration parameters
     *
     * @param datakitUrl datakit upload address
     * @return {@link FTRUMConfig} SDK configuration
     */
    public static FTSDKConfig builder(String datakitUrl) {
        return new FTSDKConfig(datakitUrl);
    }

    /**
     * Build necessary SDK configuration parameters
     *
     * @param datawayUrl  dataway upload address
     * @param clientToken token
     * @return {@link FTRUMConfig} SDK configuration
     */
    public static FTSDKConfig builder(String datawayUrl, String clientToken) {
        return new FTSDKConfig(datawayUrl, clientToken);
    }

    /**
     * SDK configuration constructor
     *
     * @param datakitUrl datakit upload address
     */
    private FTSDKConfig(String datakitUrl) {
        this.datakitUrl = datakitUrl;
        this.datawayUrl = "";
        this.clientToken = "";
    }

    /**
     * SDK configuration constructor, direct dataway configuration
     *
     * @param datawayUrl  dataway
     * @param clientToken data
     */
    private FTSDKConfig(String datawayUrl, String clientToken) {
        this.datawayUrl = datawayUrl;
        this.clientToken = clientToken;
        this.datakitUrl = "";
    }

    /**
     * Get datakit data report address
     *
     * @return
     */
    public String getDatakitUrl() {
        return datakitUrl;
    }

    /**
     * Get dataway upload address
     *
     * @return
     */
    public String getDatawayUrl() {
        return datawayUrl;
    }

    /**
     * Get dataway token
     *
     * @return
     */
    public String getClientToken() {
        return clientToken;
    }


    /**
     * @return Whether in debug state
     */
    public boolean isDebug() {
        return isDebug;
    }

    public SDKLogLevel getSdkLogLevel() {
        return sdkLogLevel;
    }


    /**
     * @return Get environment variable
     */
    public String getEnv() {
        return env;
    }

    /**
     * @return Whether only main process is supported
     */
    public boolean isOnlySupportMainProcess() {
        return onlySupportMainProcess;
    }


    /**
     * Whether to enable Debug, when enabled, SDK runtime logs will be displayed
     *
     * @param debug
     * @return
     */
    public FTSDKConfig setDebug(boolean debug) {
        isDebug = debug;
        return this;
    }


    /**
     * Set data transmission environment
     *
     * @param env
     * @return
     */
    public FTSDKConfig setEnv(EnvType env) {
        if (env != null) {
            this.env = env.toString();
        }
        return this;
    }

    /**
     * Set whether to auto sync
     *
     * @return
     */
    public FTSDKConfig setAutoSync(boolean autoSync) {
        this.autoSync = autoSync;
        return this;
    }

    /**
     * Set data sync size
     *
     * @param pageSize
     * @return
     */
    public FTSDKConfig setSyncPageSize(SyncPageSize pageSize) {
        this.pageSize = pageSize.getValue();
        return this;
    }

    /**
     * Custom data sync size
     *
     * @param pageSize
     * @return
     */
    public FTSDKConfig setCustomSyncPageSize(int pageSize) {
        this.pageSize = Math.max(SyncPageSize.MINI.getValue(), pageSize);
        return this;
    }

    /**
     * Whether to auto sync
     *
     * @return
     */
    public boolean isAutoSync() {
        return this.autoSync;
    }

    /**
     * Get the number of items per request
     *
     * @return
     */
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * Set data transmission environment
     *
     * @param env
     * @return
     */
    public FTSDKConfig setEnv(String env) {
        if (env != null && !env.isEmpty()) {
            this.env = env;
        }
        return this;
    }


    /**
     * Set whether to access Android ID
     * <p>
     * When false, the device_uuid field no longer uses {@link  android.provider.Settings.Secure#ANDROID_ID},
     * and uses {@link LocalUUIDManager#getRandomUUID()} instead
     *
     * @param enableAccessAndroidID
     * @return
     */
    public FTSDKConfig setEnableAccessAndroidID(boolean enableAccessAndroidID) {
        this.enableAccessAndroidID = enableAccessAndroidID;
        return this;
    }

    public boolean isEnableAccessAndroidID() {
        return enableAccessAndroidID;
    }

    /**
     * Whether the SDK only supports initialization in the main process
     *
     * @param onlySupportMainProcess
     * @return
     */
    public FTSDKConfig setOnlySupportMainProcess(boolean onlySupportMainProcess) {
        this.onlySupportMainProcess = onlySupportMainProcess;
        return this;
    }

    /**
     * Add global property
     *
     * @param key   key name
     * @param value key value
     * @return
     */
    public FTSDKConfig addGlobalContext(@NonNull String key, @NonNull String value) {
        this.globalContext.put(key, value);
        return this;
    }

    /**
     * Add version info
     *
     * @param key
     * @param value
     * @return
     */
    FTSDKConfig addPkgInfo(String key, String value) {
        this.pkgInfo.put(key, value);
        return this;
    }

    HashMap<String, String> getPkgInfo() {
        return pkgInfo;
    }

    /**
     * Get global properties
     *
     * @return
     */
    public HashMap<String, Object> getGlobalContext() {
        return this.globalContext;
    }

    public String getServiceName() {
        return serviceName;
    }

    /**
     * Set application service name
     *
     * @param serviceName service name
     * @return
     */
    public FTSDKConfig setServiceName(String serviceName) {
        if (serviceName != null && !serviceName.isEmpty()) {
            this.serviceName = serviceName;
        }
        return this;
    }

    public int getDataSyncRetryCount() {
        return dataSyncRetryCount;
    }

    /**
     * Set the maximum sync retry count, minimum 0, maximum 5, 0 means no retry
     *
     * @param dataSyncRetryCount retry count
     * @return
     */
    public FTSDKConfig setDataSyncRetryCount(int dataSyncRetryCount) {
        this.dataSyncRetryCount = Math.max(0, Math.min(SyncTaskManager.MAX_ERROR_COUNT, dataSyncRetryCount));
        return this;
    }

    /**
     * Add a unique tag to Okhttp request
     */
    private boolean enableOkhttpRequestTag;


    private DataModifier dataModifier;

    private LineDataModifier lineDataModifier;


    /**
     * Set whether to migrate
     *
     * @param needTransformOldCache Whether to migrate old data, default is false
     * @return
     */
    public FTSDKConfig setNeedTransformOldCache(boolean needTransformOldCache) {
        this.needTransformOldCache = needTransformOldCache;
        return this;
    }

    public boolean isNeedTransformOldCache() {
        return needTransformOldCache;
    }

    /**
     * Set the interval time for each sync, sleep time between [0,5000]，0 default
     *
     * @param sleepTimeMs Data sync interval time
     * @return
     */
    public FTSDKConfig setSyncSleepTime(int sleepTimeMs) {
        this.syncSleepTime = Math.max(SyncTaskManager.SYNC_SLEEP_MINI_TIME_MS,
                Math.min(sleepTimeMs, SyncTaskManager.SYNC_SLEEP_MAX_TIME_MS));
        return this;
    }

    public int getSyncSleepTime() {
        return syncSleepTime;
    }

    public boolean isEnableDataIntegerCompatible() {
        return enableDataIntegerCompatible;
    }

    /**
     * Deflate compression for uploaded sync data, default is off
     *
     * @param compressIntakeRequests
     * @return
     */
    public FTSDKConfig setCompressIntakeRequests(boolean compressIntakeRequests) {
        this.compressIntakeRequests = compressIntakeRequests;
        return this;
    }

    /**
     * {@link #enableDataIntegerCompatible} set to true
     */
    public FTSDKConfig enableDataIntegerCompatible() {
        this.enableDataIntegerCompatible = true;
        return this;
    }

    public Object getDns() {
        return dns;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public Object getAuthenticator() {
        return authenticator;
    }

    /**
     * Set Proxy, only supported when relying on okhttp3 library
     *
     * @param proxy
     * @return
     */
    public FTSDKConfig setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * Set Proxy authenticator, only supported when relying on okhttp3 library
     *
     * @param authenticator
     * @return
     */
    public FTSDKConfig setProxyAuthenticator(okhttp3.Authenticator authenticator) {
        this.authenticator = authenticator;
        return this;
    }

    /**
     * Set Dns transmission rule, deployment version, or self-deployed Datakit can use DNS for IP address polling optimization
     *
     * @param dns
     * @return
     */
    public FTSDKConfig setDns(okhttp3.Dns dns) {
        this.dns = dns;
        return this;
    }

    /**
     * Global Okhttp Request auto add, requires ft-plugin 1.3.5 support
     *
     * @param enableOkhttpRequestTag
     * @return
     */
    public FTSDKConfig setEnableOkhttpRequestTag(boolean enableOkhttpRequestTag) {
        this.enableOkhttpRequestTag = enableOkhttpRequestTag;
        return this;
    }

    public boolean isEnableOkhttpRequestTag() {
        return enableOkhttpRequestTag;
    }

    public boolean isCompressIntakeRequests() {
        return compressIntakeRequests;
    }

    /**
     * Modify a single field.
     *
     * @param dataModifier
     * @return
     */
    public FTSDKConfig setDataModifier(DataModifier dataModifier) {
        this.dataModifier = dataModifier;
        return this;
    }

    /**
     * Modify a single line of data
     *
     * @param dataModifier
     * @return
     */
    public FTSDKConfig setLineDataModifier(LineDataModifier dataModifier) {
        this.lineDataModifier = dataModifier;
        return this;
    }

    public LineDataModifier getLineDataModifier() {
        return lineDataModifier;
    }

    public DataModifier getDataModifier() {
        return dataModifier;
    }

    @Override
    public String toString() {
        return "FTSDKConfig{" +
                ", isDebug=" + isDebug +
                ", sdkLogLevel=" + sdkLogLevel +
                ", dbCacheLimit=" + dbCacheLimit +
                ", limitWithDbSize=" + limitWithDbSize +
                ", dbCacheDiscard=" + dbCacheDiscard +
                ", enableAccessAndroidID=" + enableAccessAndroidID +
                ", autoSync=" + autoSync +
                ", pageSize=" + pageSize +
                ", syncSleepTime=" + syncSleepTime +
                ", compressIntakeRequests=" + compressIntakeRequests +
                ", serviceName='" + serviceName + '\'' +
                ", dataSyncRetryCount=" + dataSyncRetryCount +
                ", env='" + env + '\'' +
                ", onlySupportMainProcess=" + onlySupportMainProcess +
                ", enableDataIntegerCompatible=" + enableDataIntegerCompatible +
                ", needTransformOldCache=" + needTransformOldCache +
                ", globalContext=" + globalContext +
                ", proxy=" + proxy +
                ", proxyAuthenticator=" + authenticator +
                ", dns=" + dns +
                ", enableOkhttpRequestTag=" + enableOkhttpRequestTag +
                ", dataModifier=" + dataModifier +
                ", lineDataModifier=" + lineDataModifier +
                '}';
    }
}
