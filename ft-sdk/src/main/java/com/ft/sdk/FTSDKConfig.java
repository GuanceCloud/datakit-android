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
     * datakit 数据写入地址
     */
    private final String datakitUrl;

    /**
     * dataway 数据写入地址
     */
    private final String datawayUrl;

    /**
     *
     */
    private final String clientToken;
    /**
     * 是否开启 Debug
     */
    private boolean isDebug;

    private SDKLogLevel sdkLogLevel;


    /**
     * db 缓存限制大小
     */
    private long dbCacheLimit = Constants.DEFAULT_DB_SIZE_LIMIT;

    /**
     * 是否开启使用 db 缓存
     */
    private boolean limitWithDbSize = false;

    /**
     *
     */
    private DBCacheDiscard dbCacheDiscard = DBCacheDiscard.DISCARD;

    /**
     * 设置日志等级，默认 {@link SDKLogLevel#V}
     *
     * @param logLevel
     * @return
     */
    public FTSDKConfig setSdkLogLevel(SDKLogLevel logLevel) {
        this.sdkLogLevel = logLevel;
        return this;
    }

    /**
     * 是否可访问 Android ID
     */
    private boolean enableAccessAndroidID = true;


    /**
     * 是否进行自动同步
     */
    private boolean autoSync = true;

    private int pageSize = SyncPageSize.MEDIUM.getValue();

    private int syncSleepTime = 0;
    /**
     * 对同步数据进行 deflate 压缩 ，默认关闭
     */
    private boolean compressIntakeRequests = false;

    /**
     * 服务名称 {@link Constants#KEY_SERVICE },默认为 {@link Constants#DEFAULT_SERVICE_NAME}
     */
    private String serviceName = Constants.DEFAULT_SERVICE_NAME;

    /**
     * 数据同步最大重复尝试次数
     */
    private int dataSyncRetryCount = SyncTaskManager.MAX_ERROR_COUNT;

    /**
     * 数据上传环境
     */
    private String env = EnvType.PROD.toString();

    /**
     * SDK 是否只支持在主进程中初始化
     * {@link FTSDKConfig#setOnlySupportMainProcess(boolean)}
     */
    private boolean onlySupportMainProcess = true;


    /**
     * 行协议兼容模式，integer 数据兼容模式，处理 web 数据数据类型冲突问题，默认开启。
     */
    private boolean enableDataIntegerCompatible = true;

    /**
     * 是否迁移旧数据，SDK 从旧版本迁移至 1.5.0 需要进行数据迁移
     */
    private boolean needTransformOldCache = false;


    public long getDbCacheLimit() {
        return dbCacheLimit;
    }

    /**
     * 开启使用 db 限制数据大小，默认 100MB ，{@link Constants#DEFAULT_DB_SIZE_LIMIT}
     */
    public FTSDKConfig enableLimitWithDbSize() {
        this.limitWithDbSize = true;
        return this;
    }

    /**
     * 开启使用 db 限制数据大小，默认 100MB ，单位 byte，{@link Constants#DEFAULT_DB_SIZE_LIMIT}
     *
     * @param dbSize 设置 db 限制上限，数据库越大，磁盘压力越大，[30MB,),默认 100 MB
     *               <p>
     *               开启 db 数据限制之后，{@link FTLoggerConfig#setLogCacheLimitCount(int)}
     *               及 {@link FTRUMConfig#setRumCacheLimitCount(int)} 将失效
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
     * 设置 db 缓存丢弃策略
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

    /**
     * 全局参数，例如 {@link Constants#KEY_APP_VERSION_NAME} 等固定配置参数，
     * 或通过 {@link FTSDKConfig#addGlobalContext(String, String)} 用户自定义添加的变量参数
     */
    private final HashMap<String, Object> globalContext = new HashMap<>();

    private final HashMap<String, String> pkgInfo = new HashMap<>();

    private Object dns;
    private Proxy proxy;
    private Object authenticator;

    /**
     * 构建 SDK 必要的配置参数
     *
     * @param datakitUrl datakit 上传地址
     * @return {@link FTRUMConfig} SDK 配置
     */
    public static FTSDKConfig builder(String datakitUrl) {
        return new FTSDKConfig(datakitUrl);
    }

    /**
     * 构建 SDK 必要的配置参数
     *
     * @param datawayUrl  dataway 上传地址
     * @param clientToken token
     * @return {@link FTRUMConfig} SDK 配置
     */
    public static FTSDKConfig builder(String datawayUrl, String clientToken) {
        return new FTSDKConfig(datawayUrl, clientToken);
    }

    /**
     * SDK 配置项构造方法
     *
     * @param datakitUrl datakit 上传地址
     */
    private FTSDKConfig(String datakitUrl) {
        this.datakitUrl = datakitUrl;
        this.datawayUrl = "";
        this.clientToken = "";
    }

    /**
     * SDK 配置项构造方法, 直传 dataway 配置
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
     * 获取 datakit 数据上报地址
     *
     * @return
     */
    public String getDatakitUrl() {
        return datakitUrl;
    }

    /**
     * 获取 dataway 上传地址
     *
     * @return
     */
    public String getDatawayUrl() {
        return datawayUrl;
    }

    /**
     * 获取 dataway 使用 token
     *
     * @return
     */
    public String getClientToken() {
        return clientToken;
    }


    /**
     * @return 是否处于 debug 状态
     */
    public boolean isDebug() {
        return isDebug;
    }

    public SDKLogLevel getSdkLogLevel() {
        return sdkLogLevel;
    }


    /**
     * @return 获取环境变量请问
     */
    public String getEnv() {
        return env;
    }

    /**
     * @return 是否只支持主进程
     */
    public boolean isOnlySupportMainProcess() {
        return onlySupportMainProcess;
    }


    /**
     * 是否开启Debug，开启后将显示 SDK 运行日志
     *
     * @param debug
     * @return
     */
    public FTSDKConfig setDebug(boolean debug) {
        isDebug = debug;
        return this;
    }


    /**
     * 设置数据传输的环境
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
     * 设置是否自动同步
     *
     * @return
     */
    public FTSDKConfig setAutoSync(boolean autoSync) {
        this.autoSync = autoSync;
        return this;
    }

    /**
     * 设置数据同步大小
     *
     * @param pageSize
     * @return
     */
    public FTSDKConfig setSyncPageSize(SyncPageSize pageSize) {
        this.pageSize = pageSize.getValue();
        return this;
    }

    /**
     * 自定义数据同步大小
     *
     * @param pageSize
     * @return
     */
    public FTSDKConfig setCustomSyncPageSize(int pageSize) {
        this.pageSize = Math.max(SyncPageSize.MINI.getValue(), pageSize);
        return this;
    }

    /**
     * 是否进行自动同步
     *
     * @return
     */
    public boolean isAutoSync() {
        return this.autoSync;
    }

    /**
     * 获取一次请求条目数量
     *
     * @return
     */
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * 设置数据传输环境
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
     * 设置是否获取 Android ID
     * <p>
     * 当为 false ，device_uuid 字段不再使用 {@link  android.provider.Settings.Secure#ANDROID_ID},
     * 使用 {@link LocalUUIDManager#getRandomUUID()} 做替代
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
     * 是否只支持在主进程中初始化 SDK
     *
     * @param onlySupportMainProcess
     * @return
     */
    public FTSDKConfig setOnlySupportMainProcess(boolean onlySupportMainProcess) {
        this.onlySupportMainProcess = onlySupportMainProcess;
        return this;
    }

    /**
     * 添加全局属性
     *
     * @param key   键名
     * @param value 键值
     * @return
     */
    public FTSDKConfig addGlobalContext(@NonNull String key, @NonNull String value) {
        this.globalContext.put(key, value);
        return this;
    }

    /**
     * 添加版本信息
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
     * 获取全局属性
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
     * 设置应用服务名
     *
     * @param serviceName 服务名
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
     * 设置最大同步重试次数，最小 0，最大 5, 0 则不尝试
     *
     * @param dataSyncRetryCount 重试次数，
     * @return
     */
    public FTSDKConfig setDataSyncRetryCount(int dataSyncRetryCount) {
        this.dataSyncRetryCount = Math.max(0, Math.min(SyncTaskManager.MAX_ERROR_COUNT, dataSyncRetryCount));
        return this;
    }

    /**
     * 对 Okhttp request 添加唯一 tag
     */
    private boolean enableOkhttpRequestTag;


    private DataModifier dataModifier;

    private LineDataModifier lineDataModifier;


    /**
     * 设置是否迁移
     *
     * @param needTransformOldCache 是否迁移旧数据，默认为 false
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
     * 设置每次同步的间歇时间, 休眠时间 [0,100] 之间
     *
     * @param sleepTimeMs 数据同步间歇时间
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
     * 对上传同步数据进行 deflate 压缩，默认关闭
     *
     * @param compressIntakeRequests
     * @return
     */
    public FTSDKConfig setCompressIntakeRequests(boolean compressIntakeRequests) {
        this.compressIntakeRequests = compressIntakeRequests;
        return this;
    }

    /**
     * {@link #enableDataIntegerCompatible} 设置为 true
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
     * 设置 Proxy 代理，仅支持依赖 okhttp3 库的时候
     *
     * @param proxy
     * @return
     */
    public FTSDKConfig setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * 设置 Proxy 代理 authenticator，仅支持依赖 okhttp3 库的时候
     *
     * @param authenticator
     * @return
     */
    public FTSDKConfig setProxyAuthenticator(okhttp3.Authenticator authenticator) {
        this.authenticator = authenticator;
        return this;
    }

    /**
     * 设置 Dns 传输规则，部署版本，或自部署 Datakit 可以使用 DNS 做 IP 地址轮询优化
     *
     * @param dns
     * @return
     */
    public FTSDKConfig setDns(okhttp3.Dns dns) {
        this.dns = dns;
        return this;
    }

    /**
     * 全局 Okhttp Request 自动添加，需要 ft-plugin 1.3.5 的支持
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
     * 对单个字段进行更改。
     *
     * @param dataModifier
     * @return
     */
    public FTSDKConfig setDataModifier(DataModifier dataModifier) {
        this.dataModifier = dataModifier;
        return this;
    }

    /**
     * 对单条数据数据进行更改
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
                '}';
    }
}
