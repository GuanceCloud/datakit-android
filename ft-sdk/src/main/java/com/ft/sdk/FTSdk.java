package com.ft.sdk;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

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
     * Modified by Plugin ASM, writes the Plugin version number
     */
    public static String PLUGIN_VERSION = "";
    /**
     * Will only be assigned after integrating ft-native,
     * directly access {@link com.ft.sdk.nativelib.BuildConfig#VERSION_NAME} to get
     */
    public static String NATIVE_VERSION = PackageUtils.isNativeLibrarySupport() ? PackageUtils.getNativeLibVersion() : "";

    /**
     * After integrating ft-session-replay, it will be assigned, directly access {@link com.ft.sdk.nativelib.BuildConfig#VERSION_NAME} to get
     */
    public static String SESSION_REPLAY_VERSION = PackageUtils.isSessionReplay() ? PackageUtils.getPackageSessionReplay() : "";
    /**
     * After integrating ft-session-replay-material, it will be assigned, directly access {@link com.ft.sdk.sessionreplay.material.BuildConfig#VERSION_NAME} to get
     */
    public static String SESSION_REPLAY_MATERIAL_VERSION = PackageUtils.isSessionReplayMtr() ? PackageUtils.getPackageSessionReplayMtr() : "";

    private final static boolean isSessionReplaySupport = SESSION_REPLAY_VERSION.isEmpty();
    /**
     * Variable written by Plugin ASM, UUID is the same for the same compilation version
     */
    public static String PACKAGE_UUID = "";
    /**
     * The above two variables cannot be changed arbitrarily,
     * if changed please also change the corresponding values in the plugin
     */
    public static final String AGENT_VERSION = BuildConfig.FT_SDK_VERSION;// Current SDK version
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
     * SDK configuration entry point
     *
     * @param ftSDKConfig
     * @return
     */
    public static synchronized void install(@NonNull FTSDKConfig ftSDKConfig) {
        try {
            if (ftSDKConfig == null) {
                LogUtils.e(TAG, "Parameter ftSDKConfig cannot be null");
            } else {
                boolean onlyMain = ftSDKConfig.isOnlySupportMainProcess();
                boolean isMainProcess = false;
                if (onlyMain) {
                    Context context = FTApplication.getApplication();
                    String currentProcessName = Utils.getCurrentProcessName();
                    String packageName = context.getPackageName();
                    if (!TextUtils.isEmpty(packageName) && !TextUtils.equals(packageName, currentProcessName)) {
                        LogUtils.e(TAG, "Current SDK can only run in the main process, " +
                                "current process is " + currentProcessName + ", " +
                                "if you want to run in non-main process you can set " +
                                "FTSDKConfig.setOnlySupportMainProcess(false)");
                        return;
                    } else {
                    }
                } else {
                    isMainProcess = Utils.isMainProcess();
                }
                mFtSdk = new FTSdk(ftSDKConfig);
                ftSDKConfig.isMainProcess = isMainProcess;
                mFtSdk.initFTConfig(ftSDKConfig);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "initFTConfig fail:\n" + LogUtils.getStackTraceString(e));
        }
    }

    /**
     * After SDK initialization, get the SDK object
     *
     * @return
     */
    public static synchronized FTSdk get() {
        if (mFtSdk == null) {
            LogUtils.e(TAG, "Please install SDK first (call FTSdk.install(FTSDKConfig ftSdkConfig) when the application starts)");
        }
        return mFtSdk;
    }

    /**
     * Check installation status
     *
     * @return
     */
    static boolean checkInstallState() {
        return mFtSdk != null && mFtSdk.mFtSDKConfig != null;
    }

    /**
     * Shutdown running objects within SDK
     */
    public static void shutDown() {
        SyncTaskManager.get().release();
        FTRUMConfigManager.get().release();
        FTMonitorManager.release();
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
        LogUtils.w(TAG, "FT SDK has been shut down");
    }

    /**
     * Clear unreported cached data
     */
    public static void clearAllData() {
        FTDBManager.get().delete();
    }

    /**
     * Initialize SDK local configuration data
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
     * Actively update remote configuration, call frequency is affected by the time of
     * {@link FTSDKConfig#setRemoteConfigMiniUpdateInterval(int)} }
     */
    public static void updateRemoteConfig() {
        if (checkInstallState()) {
            if (mFtSdk.mRemoteConfigManager != null) {
                mFtSdk.mRemoteConfigManager.updateRemoteConfig();
            }
        }
    }

    /**
     * Actively update remote configuration, this method ignores
     * {@link FTSDKConfig#setRemoteConfigMiniUpdateInterval(int)} } configuration
     *
     * @param remoteConfigMiniUpdateInterval Remote configuration time interval, unit seconds [0,]
     * @param result                         Return update result
     */
    public static void updateRemoteConfig(int remoteConfigMiniUpdateInterval, FTRemoteConfigManager.FetchResult result) {
        if (checkInstallState()) {
            if (mFtSdk.mRemoteConfigManager != null) {
                mFtSdk.mRemoteConfigManager.updateRemoteConfig(remoteConfigMiniUpdateInterval, result);
            }
        }
    }


    /**
     * Set RUM configuration
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
     * Set Trace configuration
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
     * Set log configuration
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
     * Initialize the configuration of session replay
     *
     * @param config
     */
    public static void initSessionReplayConfig(FTSessionReplayConfig config) {
        try {
            if (get().mRemoteConfigManager != null) {
                get().mRemoteConfigManager.mergeSessionReplayConfigFromCache(config);
            }
            SessionReplay.enable(config, FTApplication.getApplication());
        } catch (Exception e) {
            LogUtils.e(TAG, "initSessionReplayConfig fail:\n" + LogUtils.getStackTraceString(e));
        }
    }

    /**
     * Bind user information, {@link Constants#KEY_RUM_IS_SIGN_IN}, after binding the field is T,
     * bind once, the field data will continue to retain data until calling
     * {@link #unbindRumUserData()}
     *
     * @param id
     */
    public static void bindRumUserData(@NonNull String id) {
        FTRUMConfigManager.get().bindUserData(id, null, null, null);
    }


    /**
     * Bind user information, {@link #bindRumUserData(String)}  }
     */
    public static void bindRumUserData(@NonNull UserData data) {
        FTRUMConfigManager.get().bindUserData(data.getId(), data.getName(), data.getEmail(), data.getExts());
    }

    /**
     * Unbind user data {@link Constants#KEY_RUM_IS_SIGN_IN}, after binding the field is F
     */
    public static void unbindRumUserData() {
        FTRUMConfigManager.get().unbindUserData();
    }

    /**
     * Get public tags
     *
     * @return Get basic Tags in key value form
     */
    HashMap<String, Object> getBasePublicTags() {
        return mFtSDKConfig.getGlobalContext();
    }


    /**
     * Dynamically control getting Android ID
     *
     * @param enableAccessAndroidID true for apply, false for not apply
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
     * Supplement global tags
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
     * Dynamically set global tag
     *
     * @param globalContext
     */
    public static void appendGlobalContext(HashMap<String, Object> globalContext) {
        if (checkInstallState()) {
            FTTrackInner.getInstance().appendGlobalContext(globalContext);
        }
    }

    /**
     * Dynamically set global tag
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
     * Dynamically set RUM global tag
     *
     * @param globalContext
     */
    public static void appendRUMGlobalContext(HashMap<String, Object> globalContext) {
        if (checkInstallState()) {
            FTTrackInner.getInstance().appendRUMGlobalContext(globalContext);
        }
    }

    /**
     * Dynamically set RUM global tag
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
     * Dynamically set log global tag
     *
     * @param globalContext
     */
    public static void appendLogGlobalContext(HashMap<String, Object> globalContext) {
        if (checkInstallState()) {
            FTTrackInner.getInstance().appendLogGlobalContext(globalContext);
        }
    }

    /**
     * Dynamically set log global tag
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
     * Actively sync data
     */
    public static void flushSyncData() {
        if (checkInstallState()) {
            SyncTaskManager.get().executePoll();
        }
    }


}
