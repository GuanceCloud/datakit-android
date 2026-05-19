package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;

import java.net.Proxy;
import java.util.HashMap;

/**
 * BY huangDianHua
 * DATE:2019-12-06 11:40
 * Description:
 */
public class FTSDKConfig {
    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTSDKConfig";

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
     * Total cache size limit.
     */
    private long cacheLimit = Constants.DEFAULT_CACHE_SIZE_LIMIT;

    /**
     * Whether to enable total cache size limit.
     */
    private boolean limitWithCacheSize = false;

    boolean isMainProcess = false;

    /**
     * Returns whether the SDK was installed in the application's main process.
     */
    public boolean isMainProcess() {
        return isMainProcess;
    }

    /**
     *
     */
    private CacheDiscard cacheDiscard = CacheDiscard.DISCARD;

    /**
     * Set log level, default {@link SDKLogLevel#V}
     *
     * @param logLevel SDK internal log level
     * @return this config for chaining
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
     * Deflate compression for synchronized data, enabled by default
     */
    private boolean compressIntakeRequests = true;

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

    /**
     * Whether to use file-backed storage for cached data. Disabled by default for smooth upgrades.
     */
    private boolean useFileDataStore = false;

    /**
     * Whether to mirror DB writes to file-backed storage while still reading from DB.
     */
    private boolean fileDataStoreShadow = false;

    /**
     * Whether to enable DataKit-compatible data filters.
     */
    private boolean enableDataFilter = true;

    /**
     * Remote data filter update interval, unit seconds.
     */
    private int dataFilterUpdateInterval = 30 * 60;

    /**
     * Local DataKit-compatible filter rules. Local filters are applied together with remote filters.
     */
    private final HashMap<String, String[]> dataFilters = new HashMap<>();

    /**
     * Returns the total local cache size limit in bytes.
     */
    public long getCacheLimit() {
        return cacheLimit;
    }

    /**
     * Enable total cache size limit, default 100MB, {@link Constants#DEFAULT_CACHE_SIZE_LIMIT}.
     * After enabling cache size limit, {@link FTLoggerConfig#setLogCacheLimitCount(int)}
     * and {@link FTRUMConfig#setRumCacheLimitCount(int)} will be invalid.
     *
     * @return this config for chaining
     */
    public FTSDKConfig enableLimitWithCacheSize() {
        this.limitWithCacheSize = true;
        return this;
    }

    /**
     * Enable total cache size limit, default 100MB, unit byte, {@link Constants#DEFAULT_CACHE_SIZE_LIMIT}.
     *
     * @param cacheSize set cache size limit, [30MB,), default 100 MB
     * @return this config for chaining
     */
    public FTSDKConfig enableLimitWithCacheSize(long cacheSize) {
        this.cacheLimit = Math.max(Constants.MINI_CACHE_SIZE_LIMIT, cacheSize);
        this.limitWithCacheSize = true;
        return this;
    }
    /**
     * Returns whether the total cache size limit is enabled.
     */
    public boolean isLimitWithCacheSize() {
        return limitWithCacheSize;
    }

    /**
     * Set cache discard strategy.
     *
     * @param cacheDiscard cache discard strategy
     * @return this config for chaining
     */
    public FTSDKConfig setCacheDiscard(CacheDiscard cacheDiscard) {
        if (cacheDiscard != null) {
            this.cacheDiscard = cacheDiscard;
        }
        return this;
    }
    /**
     * Returns the cache discard strategy used when cache limits are reached.
     */
    public CacheDiscard getCacheDiscard() {
        return cacheDiscard;
    }

    /**
     * @deprecated Use {@link #getCacheLimit()}.
     */
    @Deprecated
    public long getDbCacheLimit() {
        return getCacheLimit();
    }

    /**
     * @deprecated Use {@link #enableLimitWithCacheSize()}.
     */
    @Deprecated
    public FTSDKConfig enableLimitWithDbSize() {
        return enableLimitWithCacheSize();
    }

    /**
     * @deprecated Use {@link #enableLimitWithCacheSize(long)}.
     */
    @Deprecated
    public FTSDKConfig enableLimitWithDbSize(long dbSize) {
        return enableLimitWithCacheSize(dbSize);
    }

    /**
     * @deprecated Use {@link #isLimitWithCacheSize()}.
     */
    @Deprecated
    public boolean isLimitWithDbSize() {
        return isLimitWithCacheSize();
    }

    /**
     * @deprecated Use {@link #setCacheDiscard(CacheDiscard)}.
     */
    @Deprecated
    public FTSDKConfig setDbCacheDiscard(DBCacheDiscard dbCacheDiscard) {
        if (dbCacheDiscard != null) {
            this.cacheDiscard = CacheDiscard.valueOf(dbCacheDiscard.name());
        }
        return this;
    }

    /**
     * @deprecated Use {@link #getCacheDiscard()}.
     */
    @Deprecated
    public DBCacheDiscard getDbCacheDiscard() {
        return DBCacheDiscard.valueOf(cacheDiscard.name());
    }


    private boolean remoteConfiguration;

    private boolean getRemoteConfiguration() {
        return remoteConfiguration;
    }

    private FTRemoteConfigManager.FetchResult remoteConfigFetchResult;

    /**
     * Whether to enable remote configuration for data collection, default is off.
     * When enabled, SDK initialization or app hot start will trigger data update.
     *
     * @param remoteConfiguration true to enable remote configuration
     * @return this config for chaining
     */
    public FTSDKConfig setRemoteConfiguration(boolean remoteConfiguration) {
        this.remoteConfiguration = remoteConfiguration;
        return this;
    }


    /**
     * Sets a callback for remote configuration fetch results.
     *
     * @param remoteConfigFetchResult callback invoked after a remote config fetch finishes
     * @return this config for chaining
     */
    public FTSDKConfig setRemoteConfigurationCallBack(FTRemoteConfigManager.FetchResult remoteConfigFetchResult) {
        this.remoteConfigFetchResult = remoteConfigFetchResult;
        return this;
    }

    /**
     * Returns the remote configuration fetch callback.
     */
    public FTRemoteConfigManager.FetchResult getRemoteConfigFetchResult() {
        return remoteConfigFetchResult;
    }

    private int remoteConfigMiniUpdateInterval = 43200;//12 hour

    /**
     * Set the minimum interval for data update, unit: seconds, default 12 hours
     *
     * @param remoteConfigMiniUpdateInterval minimum update interval in seconds
     * @return this config for chaining
     */
    public FTSDKConfig setRemoteConfigMiniUpdateInterval(int remoteConfigMiniUpdateInterval) {
        this.remoteConfigMiniUpdateInterval = remoteConfigMiniUpdateInterval;
        return this;
    }

    /**
     * Returns whether remote configuration is enabled.
     */
    public boolean isRemoteConfiguration() {
        return remoteConfiguration;
    }

    /**
     * Returns the minimum remote configuration update interval in seconds.
     */
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
     * Build necessary SDK configuration parameters without an initial upload URL.
     * In this mode, the SDK can collect data first, but it will not upload until
     * {@link FTSdk#setDatakitUrl(String)} or {@link FTSdk#setDatawayUrl(String, String)}
     * is called later to provide a valid endpoint.
     *
     * @return SDK configuration
     */
    public static FTSDKConfig builder() {
        return new FTSDKConfig();
    }

    /**
     * Build necessary SDK configuration parameters
     *
     * @param datakitUrl datakit upload address
     * @return SDK configuration
     */
    public static FTSDKConfig builder(String datakitUrl) {
        return new FTSDKConfig(datakitUrl);
    }

    /**
     * Build necessary SDK configuration parameters
     *
     * @param datawayUrl  dataway upload address
     * @param clientToken token
     * @return SDK configuration
     */
    public static FTSDKConfig builder(String datawayUrl, String clientToken) {
        return new FTSDKConfig(datawayUrl, clientToken);
    }

    /**
     * SDK configuration constructor (no URL required initially)
     */
    private FTSDKConfig() {
        this.datakitUrl = "";
        this.datawayUrl = "";
        this.clientToken = "";
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
     * @return Datakit upload URL
     */
    public String getDatakitUrl() {
        return datakitUrl;
    }

    /**
     * Get dataway upload address
     *
     * @return Dataway upload URL
     */
    public String getDatawayUrl() {
        return datawayUrl;
    }

    /**
     * Get dataway token
     *
     * @return Dataway client token
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

    /**
     * Returns the SDK internal log level.
     */
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
     * @param debug true to print SDK runtime logs
     * @return this config for chaining
     */
    public FTSDKConfig setDebug(boolean debug) {
        isDebug = debug;
        return this;
    }


    /**
     * Set data transmission environment
     *
     * @param env SDK environment value
     * @return this config for chaining
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
     * @param autoSync true to upload cached data automatically
     * @return this config for chaining
     */
    public FTSDKConfig setAutoSync(boolean autoSync) {
        this.autoSync = autoSync;
        return this;
    }

    /**
     * Set data sync size
     *
     * @param pageSize predefined number of rows uploaded per request
     * @return this config for chaining
     */
    public FTSDKConfig setSyncPageSize(SyncPageSize pageSize) {
        this.pageSize = pageSize.getValue();
        return this;
    }

    /**
     * Custom data sync size
     *
     * @param pageSize custom number of rows uploaded per request
     * @return this config for chaining
     */
    public FTSDKConfig setCustomSyncPageSize(int pageSize) {
        this.pageSize = Math.max(SyncPageSize.MINI.getValue(), pageSize);
        return this;
    }

    /**
     * Whether to auto sync
     *
     * @return true when automatic upload is enabled
     */
    public boolean isAutoSync() {
        return this.autoSync;
    }

    /**
     * Get the number of items per request
     *
     * @return number of rows uploaded per request
     */
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * Set data transmission environment
     *
     * @param env custom SDK environment value
     * @return this config for chaining
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
     * @param enableAccessAndroidID true to use Android ID as device_uuid when available
     * @return this config for chaining
     */
    public FTSDKConfig setEnableAccessAndroidID(boolean enableAccessAndroidID) {
        this.enableAccessAndroidID = enableAccessAndroidID;
        return this;
    }

    /**
     * Returns whether the SDK is allowed to read Android ID for device_uuid.
     */
    public boolean isEnableAccessAndroidID() {
        return enableAccessAndroidID;
    }

    /**
     * Whether the SDK only supports initialization in the main process
     *
     * @param onlySupportMainProcess true to install only in the main process
     * @return this config for chaining
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
     * @return this config for chaining
     */
    public FTSDKConfig addGlobalContext(@NonNull String key, @NonNull String value) {
        this.globalContext.put(key, value);
        return this;
    }

    /**
     * Add version info
     *
     * @param key   package information key
     * @param value package information value
     * @return this config for chaining
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
     * @return global attributes appended to all SDK data
     */
    public HashMap<String, Object> getGlobalContext() {
        return this.globalContext;
    }
    /**
     * Returns the application service name attached to SDK data.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Set application service name
     *
     * @param serviceName service name
     * @return this config for chaining
     */
    public FTSDKConfig setServiceName(String serviceName) {
        if (serviceName != null && !serviceName.isEmpty()) {
            this.serviceName = serviceName;
        }
        return this;
    }
    /**
     * Returns the maximum retry count used for data synchronization.
     */
    public int getDataSyncRetryCount() {
        return dataSyncRetryCount;
    }

    /**
     * Set the maximum sync retry count, minimum 0, maximum 5, 0 means no retry
     *
     * @param dataSyncRetryCount retry count
     * @return this config for chaining
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
     * @return this config for chaining
     */
    public FTSDKConfig setNeedTransformOldCache(boolean needTransformOldCache) {
        this.needTransformOldCache = needTransformOldCache;
        return this;
    }
    /**
     * Returns whether old SQLite cache migration is enabled.
     */
    public boolean isNeedTransformOldCache() {
        return needTransformOldCache;
    }

    /**
     * Use file-backed storage for sync cache and RUM aggregate data.
     * This is an opt-in migration path. SQLite-backed storage remains the default for smooth upgrades.
     *
     * @param useFileDataStore true to use file-backed storage, false to use SQLite storage
     * @return this config for chaining
     */
    public FTSDKConfig setUseFileDataStore(boolean useFileDataStore) {
        this.useFileDataStore = useFileDataStore;
        return this;
    }

    /**
     * Enable file-backed storage for sync cache and RUM aggregate data.
     *
     * @return this config for chaining
     */
    public FTSDKConfig enableFileDataStore() {
        return setUseFileDataStore(true);
    }
    /**
     * Returns whether file-backed storage is enabled for SDK cache data.
     */
    public boolean isUseFileDataStore() {
        return useFileDataStore;
    }

    /**
     * Use the default SQLite-backed storage path.
     * This requires {@code FTContentProvider}, which is declared by the SDK manifest by default.
     *
     * @param useDBDataStore true to use SQLite storage, false to use file-backed storage
     * @return this config for chaining
     */
    public FTSDKConfig setUseDBDataStore(boolean useDBDataStore) {
        this.useFileDataStore = !useDBDataStore;
        return this;
    }
    /**
     * Returns whether SQLite-backed storage is enabled for SDK cache data.
     */
    public boolean isUseDBDataStore() {
        return !useFileDataStore;
    }

    /**
     * Mirror DB writes to file-backed storage while keeping DB as the read path.
     * This takes precedence over file-backed storage when enabled.
     * This requires {@code FTContentProvider}, which is declared by the SDK manifest by default.
     *
     * @param fileDataStoreShadow true to enable shadow writes
     * @return this config for chaining
     */
    public FTSDKConfig setFileDataStoreShadow(boolean fileDataStoreShadow) {
        this.fileDataStoreShadow = fileDataStoreShadow;
        return this;
    }
    /**
     * Returns whether DB writes are mirrored to file-backed storage.
     */
    public boolean isFileDataStoreShadow() {
        return fileDataStoreShadow;
    }

    /**
     * Enable DataKit-compatible blacklist filters.
     * <p>
     * Local filters configured through {@link #setDataFilters(HashMap)} are applied together with remote filters.
     * The SDK pulls remote filters from Datakit/Dataway through {@code /v1/datakit/pull?filters=true}.
     * Enabled by default.
     *
     * @param enableDataFilter true to enable data filters
     * @return this config for chaining
     */
    public FTSDKConfig setEnableDataFilter(boolean enableDataFilter) {
        this.enableDataFilter = enableDataFilter;
        return this;
    }
    /**
     * Returns whether DataKit-compatible data filters are enabled.
     */
    public boolean isEnableDataFilter() {
        return enableDataFilter;
    }

    /**
     * Set local DataKit-compatible filter rules.
     * <p>
     * Supported categories in the Android SDK are {@code logging} and {@code rum}.
     * Local filters are applied after {@link LineDataModifier} and before local cache writes,
     * together with remote filters. A matched rule means the data point will be dropped.
     *
     * @param filters filter rules grouped by category
     * @return this config for chaining
     */
    public FTSDKConfig setDataFilters(HashMap<String, String[]> filters) {
        this.dataFilters.clear();
        if (filters != null) {
            this.dataFilters.putAll(filters);
        }
        return this;
    }
    /**
     * Returns local DataKit-compatible filter rules grouped by category.
     */
    public HashMap<String, String[]> getDataFilters() {
        return dataFilters;
    }

    /**
     * Set remote data filter update interval, default is 30 minutes.
     *
     * @param intervalSeconds interval seconds, minimum 1
     * @return this config for chaining
     */
    public FTSDKConfig setDataFilterUpdateInterval(int intervalSeconds) {
        this.dataFilterUpdateInterval = Math.max(1, intervalSeconds);
        return this;
    }
    /**
     * Returns the remote data filter update interval in seconds.
     */
    public int getDataFilterUpdateInterval() {
        return dataFilterUpdateInterval;
    }

    /**
     * Set the interval time for each sync, sleep time between [0,5000]，0 default
     *
     * @param sleepTimeMs Data sync interval time
     * @return this config for chaining
     */
    public FTSDKConfig setSyncSleepTime(int sleepTimeMs) {
        this.syncSleepTime = Math.max(SyncTaskManager.SYNC_SLEEP_MINI_TIME_MS,
                Math.min(sleepTimeMs, SyncTaskManager.SYNC_SLEEP_MAX_TIME_MS));
        return this;
    }
    /**
     * Returns the sleep time between synchronization requests in milliseconds.
     */
    public int getSyncSleepTime() {
        return syncSleepTime;
    }

    /**
     * Returns whether integer compatibility mode is enabled for line protocol data.
     */
    public boolean isEnableDataIntegerCompatible() {
        return enableDataIntegerCompatible;
    }

    /**
     * Deflate compression for uploaded sync data, enabled by default
     *
     * @param compressIntakeRequests Whether to enable deflate compression for uploaded sync data
     * @return this config for chaining
     */
    public FTSDKConfig setCompressIntakeRequests(boolean compressIntakeRequests) {
        this.compressIntakeRequests = compressIntakeRequests;
        return this;
    }

    /**
     * {@link #enableDataIntegerCompatible} set to true
     *
     * @return this config for chaining
     */
    public FTSDKConfig enableDataIntegerCompatible() {
        this.enableDataIntegerCompatible = true;
        return this;
    }

    /**
     * Returns the custom OkHttp DNS implementation, or null when none is set.
     */
    public Object getDns() {
        return dns;
    }

    /**
     * Returns the custom network proxy, or null when none is set.
     */
    public Proxy getProxy() {
        return proxy;
    }

    /**
     * Returns the custom proxy authenticator, or null when none is set.
     */
    public Object getAuthenticator() {
        return authenticator;
    }

    /**
     * Set Proxy, only supported when relying on okhttp3 library
     *
     * @param proxy custom proxy used by SDK upload requests
     * @return this config for chaining
     */
    public FTSDKConfig setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * Set Proxy authenticator, only supported when relying on okhttp3 library
     *
     * @param authenticator proxy authenticator used by SDK upload requests
     * @return this config for chaining
     */
    public FTSDKConfig setProxyAuthenticator(okhttp3.Authenticator authenticator) {
        this.authenticator = authenticator;
        return this;
    }

    /**
     * Set Dns transmission rule, deployment version, or self-deployed Datakit can use DNS for IP address polling optimization
     *
     * @param dns custom DNS resolver used by SDK upload requests
     * @return this config for chaining
     */
    public FTSDKConfig setDns(okhttp3.Dns dns) {
        this.dns = dns;
        return this;
    }

    /**
     * Global Okhttp Request auto add, requires ft-plugin 1.3.5 support
     *
     * @param enableOkhttpRequestTag true to add SDK request tags to OkHttp requests automatically
     * @return this config for chaining
     */
    public FTSDKConfig setEnableOkhttpRequestTag(boolean enableOkhttpRequestTag) {
        this.enableOkhttpRequestTag = enableOkhttpRequestTag;
        return this;
    }

    /**
     * Returns whether SDK request tags are added to OkHttp requests automatically.
     */
    public boolean isEnableOkhttpRequestTag() {
        return enableOkhttpRequestTag;
    }

    /**
     * Returns whether uploaded sync data is compressed.
     */
    public boolean isCompressIntakeRequests() {
        return compressIntakeRequests;
    }

    /**
     * Modify a single field.
     *
     * @param dataModifier field-level data modifier
     * @return this config for chaining
     */
    public FTSDKConfig setDataModifier(DataModifier dataModifier) {
        this.dataModifier = dataModifier;
        return this;
    }

    /**
     * Modify a single line of data
     *
     * @param dataModifier line-level data modifier
     * @return this config for chaining
     */
    public FTSDKConfig setLineDataModifier(LineDataModifier dataModifier) {
        this.lineDataModifier = dataModifier;
        return this;
    }

    /**
     * Returns the line-level data modifier, or null when none is set.
     */
    public LineDataModifier getLineDataModifier() {
        return lineDataModifier;
    }

    /**
     * Returns the field-level data modifier, or null when none is set.
     */
    public DataModifier getDataModifier() {
        return dataModifier;
    }

    @Override
    public String toString() {
        return "FTSDKConfig{" +
                ", isDebug=" + isDebug +
                ", sdkLogLevel=" + sdkLogLevel +
                ", cacheLimit=" + cacheLimit +
                ", limitWithCacheSize=" + limitWithCacheSize +
                ", cacheDiscard=" + cacheDiscard +
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
                ", useFileDataStore=" + useFileDataStore +
                ", fileDataStoreShadow=" + fileDataStoreShadow +
                ", enableDataFilter=" + enableDataFilter +
                ", dataFilterUpdateInterval=" + dataFilterUpdateInterval +
                ", dataFilters=" + dataFilters.keySet() +
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
