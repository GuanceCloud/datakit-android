package com.ft.sdk;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTAutoTrackConfigManager;
import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.db.FTDBCachePolicy;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.threadpool.EventConsumerThreadPool;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.PackageUtils;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.sessionreplay.FTSessionReplayConfig;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;


/**
 * BY huangDianHua
 * DATE:2019-11-29 17:15
 * Description:
 */
public class FTSdk {
    public final static String TAG = Constants.LOG_TAG_PREFIX + "FTSdk";
    public static final String NATIVE_DUMP_PATH = "ftCrashDmp";
    /**
     * 由 Plugin ASM 进行改写，写入的是 Plugin 的版本号
     */
    public static String PLUGIN_VERSION = "";
    /**
     * 集成后 ft-native 后才会被被赋值,直接访问 {@link com.ft.sdk.nativelib.BuildConfig#VERSION_NAME} 来获取
     */
    public static String NATIVE_VERSION = PackageUtils.isNativeLibrarySupport() ? PackageUtils.getNativeLibVersion() : "";

    /**
     * 集成后 ft-session-replay 后才会被被赋值,直接访问 {@link com.ft.sdk.nativelib.BuildConfig#VERSION_NAME} 来获取
     */
    public static String SESSION_REPLAY_VERSION = PackageUtils.isSessionReplay() ? PackageUtils.getPackageSessionReplay() : "";
    /**
     * 集成后 ft-session-replay-material 后才会被被赋值,直接访问 {@link com.ft.sdk.sessionreplay.material.BuildConfig#VERSION_NAME} 来获取
     */
    public static String SESSION_REPLAY_MATERIAL_VERSION = PackageUtils.isSessionReplayMtr() ? PackageUtils.getPackageSessionReplayMtr() : "";

    private final static boolean isSessionReplaySupport = SESSION_REPLAY_VERSION.isEmpty();
    /**
     * 变量由 Plugin ASM 写入，同一次编译版本 UUID 相同
     */
    public static String PACKAGE_UUID = "";
    /**
     * 面两个变量也不能随便改动，改动请同时更改 plugin 中对应的值
     */
    public static final String AGENT_VERSION = BuildConfig.FT_SDK_VERSION;//当前SDK 版本
    private static FTSdk mFtSdk;
    private final FTSDKConfig mFtSDKConfig;
    private FTRemoteConfigManager mRemoteConfigManager;

    /**
     * @param ftSDKConfig
     */
    private FTSdk(@NonNull FTSDKConfig ftSDKConfig) {
        this.mFtSDKConfig = ftSDKConfig;
    }

    /**
     * SDK 配置项入口
     *
     * @param ftSDKConfig
     * @return
     */
    public static synchronized void install(@NonNull FTSDKConfig ftSDKConfig) {
        try {
            if (ftSDKConfig == null) {
                LogUtils.e(TAG, "参数 ftSDKConfig 不能为 null");
            } else {
                boolean onlyMain = ftSDKConfig.isOnlySupportMainProcess();
                if (onlyMain) {
                    Context context = FTApplication.getApplication();
                    String currentProcessName = Utils.getCurrentProcessName();
                    String packageName = context.getPackageName();
                    if (!TextUtils.isEmpty(packageName) && !TextUtils.equals(packageName, currentProcessName)) {
                        LogUtils.e(TAG, "当前 SDK 只能在主进程中运行，当前进程为 " + currentProcessName + "，如果想要在非主进程中运行可以设置 FTSDKConfig.setOnlySupportMainProcess(false)");
                        return;
                    }
                }
                mFtSdk = new FTSdk(ftSDKConfig);
                mFtSdk.initFTConfig(ftSDKConfig);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "initFTConfig fail:\n" + LogUtils.getStackTraceString(e));
        }
    }

    /**
     * SDK 初始化后，获得 SDK 对象
     *
     * @return
     */
    public static synchronized FTSdk get() {
        if (mFtSdk == null) {
            LogUtils.e(TAG, "请先安装SDK(在应用启动时调用FTSdk.install(FTSDKConfig ftSdkConfig))");
        }
        return mFtSdk;
    }

    /**
     * 检查设置状态
     *
     * @return
     */
    static boolean checkInstallState() {
        return mFtSdk != null && mFtSdk.mFtSDKConfig != null;
    }

    /**
     * 关闭 SDK 内正在运行对象
     */
    public static void shutDown() {
        SyncTaskManager.get().release();
        FTRUMConfigManager.get().release();
        FTMonitorManager.release();
        FTAutoTrackConfigManager.release();
        FTHttpConfigManager.release();
        FTNetworkListener.release();
//        LocationUtils.get().stopListener();
        FTExceptionHandler.release();
        FTDBCachePolicy.release();
        FTUIBlockManager.get().release();
        FTTraceConfigManager.get().release();
        FTLoggerConfigManager.get().release();
        TrackLogManager.get().shutdown();
        FTRUMGlobalManager.get().release();
        FTRUMInnerManager.get().release();
        EventConsumerThreadPool.get().shutDown();
        FTANRDetector.get().release();
        FTDBManager.release();
        if (FTSdk.isSessionReplaySupport()) {
            SessionReplayManager.get().stop();
        }
        if (mFtSdk != null) {
            if (mFtSdk.mRemoteConfigManager != null) {
                mFtSdk.mRemoteConfigManager.close();
            }
            mFtSdk = null;
        }
        LogUtils.w(TAG, "FT SDK 已经被关闭");
    }

    /**
     * 清理未上报的缓存数据
     */
    public static void clearAllData() {
        FTDBManager.get().delete();
    }

    /**
     * 初始化SDK本地配置数据
     */
    private void initFTConfig(FTSDKConfig config) {
        LogUtils.setDebug(config.isDebug());
        if (config.isRemoteConfiguration()) {
            mRemoteConfigManager = new FTRemoteConfigManager(config.getRemoteConfigMiniUpdateInterval());
            mRemoteConfigManager.initFromLocalCache();
            mRemoteConfigManager.mergeSDKConfigFromCache(config);
        }
        LogUtils.setSDKLogLevel(config.getSdkLogLevel());
        LocalUUIDManager.get().initRandomUUID();
        FTDBCachePolicy.get().initSDKParams(config);
        FTHttpConfigManager.get().initParams(config);
        appendGlobalContext(config);
        SyncTaskManager.get().init(config);
        FTTrackInner.getInstance().initBaseConfig(config);
        FTNetworkListener.get().monitor();
        LogUtils.d(TAG, "initFTConfig complete:" + config);
    }


    public FTSDKConfig getBaseConfig() {
        return mFtSDKConfig;
    }


    /**
     * 主动更新远程远程配置，调用频次受 {@link FTSDKConfig#setRemoteConfigMiniUpdateInterval(int)} }的时间影响
     */
    public static void updateRemoteConfig() {
        if (checkInstallState()) {
            if (mFtSdk.mRemoteConfigManager != null) {
                mFtSdk.mRemoteConfigManager.updateRemoteConfig();
            }
        }
    }

    /**
     * 主动更新远程远程配置,这个方法无视 {@link FTSDKConfig#setRemoteConfigMiniUpdateInterval(int)} } 配置
     *
     * @param remoteConfigMiniUpdateInterval 远程配置时间间隔，单位秒 [0,]
     * @param result 返回更新结果
     */
    public static void updateRemoteConfig(int remoteConfigMiniUpdateInterval, FTRemoteConfigManager.FetchResult result) {
        if (checkInstallState()) {
            if (mFtSdk.mRemoteConfigManager != null) {
                mFtSdk.mRemoteConfigManager.updateRemoteConfig(remoteConfigMiniUpdateInterval, result);
            }
        }
    }


    /**
     * 设置 RUM 配置
     *
     * @param config
     */
    public static void initRUMWithConfig(@NonNull FTRUMConfig config) {
        try {
            config.setServiceName(get().getBaseConfig().getServiceName());
            if (get().mRemoteConfigManager != null) {
                get().mRemoteConfigManager.mergeRUMConfigFromCache(config);
                get().mRemoteConfigManager.initFromRemote(config.getRumAppId());
            }
            FTRUMConfigManager.get().initWithConfig(config);
            LogUtils.d(TAG, "initRUMWithConfig complete:" + config);

        } catch (Exception e) {
            LogUtils.e(TAG, "initRUMWithConfig fail:\n" + LogUtils.getStackTraceString(e));
        }

    }

    /**
     * 设置 Trace 配置
     *
     * @param config
     */
    public static void initTraceWithConfig(@NonNull FTTraceConfig config) {
        try {
            config.setServiceName(get().getBaseConfig().getServiceName());
            if (get().mRemoteConfigManager != null) {
                get().mRemoteConfigManager.mergeTraceConfigFromCache(config);
            }
            FTTraceConfigManager.get().initWithConfig(config);
            LogUtils.d(TAG, "initTraceWithConfig complete:" + config);

        } catch (Exception e) {
            LogUtils.e(TAG, "initTraceWithConfig fail:\n" + LogUtils.getStackTraceString(e));
        }
    }

    /**
     * 设置 log 配置
     *
     * @param config
     */
    public static void initLogWithConfig(@NonNull FTLoggerConfig config) {
        try {
            config.setServiceName(get().getBaseConfig().getServiceName());
            if (get().mRemoteConfigManager != null) {
                get().mRemoteConfigManager.mergeLogConfigFromCache(config);
            }
            FTLoggerConfigManager.get().initWithConfig(config);
            LogUtils.d(TAG, "initLogWithConfig complete:" + config);

        } catch (Exception e) {
            LogUtils.e(TAG, "initLogWithConfig fail:\n" + LogUtils.getStackTraceString(e));
        }
    }


    /**
     * 初始化 session replay 的配置
     *
     * @param config
     */
    public static void initSessionReplayConfig(FTSessionReplayConfig config) {
        SessionReplay.enable(config, FTApplication.getApplication());
    }

    /**
     * 绑定用户信息,{@link Constants#KEY_RUM_IS_SIGN_IN},绑定后字段为 T，绑定一次，字段数据会持续保留数据直到，调用
     * {@link #unbindRumUserData()}
     *
     * @param id
     */
    public static void bindRumUserData(@NonNull String id) {
        FTRUMConfigManager.get().bindUserData(id, null, null, null);
    }


    /**
     * 绑定用户信息,{@link #bindRumUserData(String)}  }
     */
    public static void bindRumUserData(@NonNull UserData data) {
        FTRUMConfigManager.get().bindUserData(data.getId(), data.getName(), data.getEmail(), data.getExts());
    }

    /**
     * 解绑用户数据 {@link Constants#KEY_RUM_IS_SIGN_IN},绑定后字段为 F
     */
    public static void unbindRumUserData() {
        FTRUMConfigManager.get().unbindUserData();
    }

    /**
     * 获取公用 tags
     *
     * @return 获取基础 Tags ，key value 形式
     */
    HashMap<String, Object> getBasePublicTags() {
        return mFtSDKConfig.getGlobalContext();
    }


    /**
     * 动态控制获取 Android ID
     *
     * @param enableAccessAndroidID 是为应用，否为不应用
     */
    public static void setEnableAccessAndroidID(boolean enableAccessAndroidID) {
        if (checkInstallState()) {
            FTSDKConfig currentConfig = mFtSdk.mFtSDKConfig;
            currentConfig.setEnableAccessAndroidID(enableAccessAndroidID);
            String uuid = enableAccessAndroidID ? DeviceUtils.getUuid(FTApplication.getApplication()) : "";

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(Constants.KEY_DEVICE_UUID, uuid);
            FTTrackInner.getInstance().appendGlobalContext(hashMap);
        }
    }

    public static boolean isSessionReplaySupport() {
        return isSessionReplaySupport;
    }


    /**
     * 补充全局 tags
     *
     * @param config
     */
    private void appendGlobalContext(FTSDKConfig config) {
        HashMap<String, Object> hashMap = config.getGlobalContext();
        hashMap.put(Constants.KEY_APP_VERSION_NAME, Utils.getAppVersionName());
        hashMap.put(Constants.KEY_SDK_NAME, Constants.SDK_NAME);
        hashMap.put(Constants.KEY_APPLICATION_UUID, FTSdk.PACKAGE_UUID);
        hashMap.put(Constants.KEY_ENV, config.getEnv());
        String uuid = config.isEnableAccessAndroidID() ? DeviceUtils.getUuid(FTApplication.getApplication())
                : LocalUUIDManager.get().getRandomUUID();
        hashMap.put(Constants.KEY_DEVICE_UUID, uuid);
        HashMap<String, String> pkgInfo = getStringStringHashMap();
        if (!pkgInfo.isEmpty()) {
            pkgInfo.putAll(config.getPkgInfo());
        }
        hashMap.put(Constants.KEY_RUM_SDK_PACKAGE_INFO, Utils.hashMapObjectToJson(pkgInfo));
        hashMap.put(Constants.KEY_SDK_VERSION, FTSdk.AGENT_VERSION);
    }

    private static @NotNull HashMap<String, String> getStringStringHashMap() {
        HashMap<String, String> pkgInfo = new HashMap<>();
        pkgInfo.put(Constants.KEY_RUM_SDK_PACKAGE_AGENT, FTSdk.AGENT_VERSION);
        if (!FTSdk.PLUGIN_VERSION.isEmpty()) {
            pkgInfo.put(Constants.KEY_RUM_SDK_PACKAGE_TRACK, FTSdk.PLUGIN_VERSION);
        }
        if (!FTSdk.NATIVE_VERSION.isEmpty()) {
            pkgInfo.put(Constants.KEY_RUM_SDK_PACKAGE_NATIVE, FTSdk.NATIVE_VERSION);
        }
        if (!FTSdk.SESSION_REPLAY_VERSION.isEmpty()) {
            pkgInfo.put(Constants.KEY_RUM_SDK_PACKAGE_REPLAY, FTSdk.SESSION_REPLAY_VERSION);
        }
        if (!FTSdk.SESSION_REPLAY_MATERIAL_VERSION.isEmpty()) {
            pkgInfo.put(Constants.KEY_RUM_SDK_PACKAGE_REPLAY_MATERIAL, FTSdk.SESSION_REPLAY_MATERIAL_VERSION);
        }
        return pkgInfo;
    }

    /**
     * 动态设置全局 tag
     *
     * @param globalContext
     */
    public static void appendGlobalContext(HashMap<String, Object> globalContext) {
        if (checkInstallState()) {
            FTTrackInner.getInstance().appendGlobalContext(globalContext);
        }
    }

    /**
     * 动态设置全局 tag
     *
     * @param key
     * @param value
     */
    public static void appendGlobalContext(String key, String value) {
        if (checkInstallState()) {
            FTTrackInner.getInstance().appendGlobalContext(key, value);
        }
    }

    /**
     * 动态设置 RUM 全局 tag
     *
     * @param globalContext
     */
    public static void appendRUMGlobalContext(HashMap<String, Object> globalContext) {
        if (checkInstallState()) {
            FTTrackInner.getInstance().appendRUMGlobalContext(globalContext);
        }
    }

    /**
     * 动态设置 RUM 全局 tag
     *
     * @param key
     * @param value
     */
    public static void appendRUMGlobalContext(String key, String value) {
        if (checkInstallState()) {
            FTTrackInner.getInstance().appendRUMGlobalContext(key, value);
        }
    }

    /**
     * 动态设置 log 全局 tag
     *
     * @param globalContext
     */
    public static void appendLogGlobalContext(HashMap<String, Object> globalContext) {
        if (checkInstallState()) {
            FTTrackInner.getInstance().appendLogGlobalContext(globalContext);
        }
    }

    /**
     * 动态设置 log 全局 tag
     *
     * @param key
     * @param value
     */
    public static void appendLogGlobalContext(String key, String value) {
        if (checkInstallState()) {
            FTTrackInner.getInstance().appendLogGlobalContext(key, value);
        }
    }


    /**
     * 主动同步数据
     */
    public static void flushSyncData() {
        if (checkInstallState()) {
            SyncTaskManager.get().executePoll();
        }
    }


}
