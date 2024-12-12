package com.ft.sdk;

import static com.ft.sdk.FTApplication.getApplication;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.ft.sdk.garble.FTDBCachePolicy;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.threadpool.RunnerCompleteCallBack;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.NetUtils;
import com.ft.sdk.garble.utils.PackageUtils;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.garble.utils.VersionUtils;
import com.ft.sdk.nativelib.CrashCallback;
import com.ft.sdk.nativelib.NativeEngineInit;
import com.ft.sdk.nativelib.NativeExtraLogCatSetting;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author Brandon
 */
public class FTRUMConfigManager {

    private static final String TAG = Constants.LOG_TAG_PREFIX + "FTRUMConfigManager";

    private static class SingletonHolder {
        private static final FTRUMConfigManager INSTANCE = new FTRUMConfigManager();
    }

    public static FTRUMConfigManager get() {
        return FTRUMConfigManager.SingletonHolder.INSTANCE;
    }

    private FTRUMConfig config;

    private volatile String randomUserId;

    /**
     * 绑定用户数据
     */
    private UserData mUserData;


    /**
     * 用户数据同步锁
     */
    private final Object mLock = new Object();


    /**
     * RUM 条件初始化
     *
     * @param config {@link FTRUMConfig} 配置信息
     */
    void initWithConfig(FTRUMConfig config) {
        this.config = config;

        FTDBCachePolicy.get().initRUMParam(config);
        FTRUMInnerManager.get().initParams(config);
        FTRUMGlobalManager.get().initConfig(config);
//        FTAutoTrackConfigManager.get().initParams();
        FTExceptionHandler.get().initConfig(config);
        initRandomUserId();
        FTMonitorManager.get().initWithConfig(config);
        FTUIBlockManager.get().start(config);
        FTANRDetector.get().init(config);
        initRUMGlobalContext(config);
        FTTrackInner.getInstance().initRUMConfig(config);
        if (config.isRumEnable() && config.isEnableTraceUserAction()) {
            //应对 flutter reactNative application 生命周期启动早于条件设置
            FTAppStartCounter.get().checkToReUpload();
        }

        initNativeDump(config);
    }

    /**
     * 初始化 Native Library 配置。
     * {@link FTExceptionHandler#NATIVE_CALLBACK_VERSION} 以下的版本，判断是否有dump 文件，如果有就上传。
     * {@link FTExceptionHandler#NATIVE_CALLBACK_VERSION} 以上的版本，设置 Native 版本回调，并对未成功回调的数据进行补充上传，
     * 并标记为 {@link FTExceptionHandler#IS_PRE_CRASH}
     */
    private void initNativeDump(FTRUMConfig config) {
        boolean isNativeLibSupport = PackageUtils.isNativeLibrarySupport();

        if (!config.isRumEnable()) {
            return;
        }

        boolean enableTrackAppCrash = config.isEnableTrackAppCrash();
        boolean enableTrackAppANR = config.isEnableTrackAppANR();
        if (enableTrackAppCrash || enableTrackAppANR) {
            if (!isNativeLibSupport) {
                LogUtils.e(TAG, "未启动 native 崩溃收集");
                return;
            }

            Application application = getApplication();
            File crashFilePath = new File(application.getFilesDir(), FTSdk.NATIVE_DUMP_PATH);
            if (!crashFilePath.exists()) {
                crashFilePath.mkdirs();
            }

            String filePath = crashFilePath.toString();

            if (VersionUtils.firstVerGreaterEqual(FTSdk.NATIVE_VERSION, FTExceptionHandler.NATIVE_CALLBACK_VERSION)) {
                final CrashCallback crashCallback = new CrashCallback() {
                    @Override
                    public void onCrash(String crashPath) {
                        //fixme 这里如果 native crash 文件过大可能存在性能问题
                        CountDownLatch latch = new CountDownLatch(1);
                        FTExceptionHandler.get().uploadNativeCrashBackground(new File(crashPath),
                                AppState.RUN, false, new RunnerCompleteCallBack() {
                                    @Override
                                    public void onComplete() {
                                        Utils.deleteFile(crashPath);
                                        latch.countDown();
                                    }
                                });

                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            LogUtils.e(TAG, LogUtils.getStackTraceString(e));
                        }
                    }
                };

                if (VersionUtils.firstVerGreaterEqual(FTSdk.NATIVE_VERSION, FTExceptionHandler.NATIVE_LOGCAT_SETTING_VERSION)) {
                    NativeExtraLogCatSetting nativeExtraLogCatSetting = null;
                    if (config.getExtraLogCatWithNativeCrash() != null) {
                        nativeExtraLogCatSetting = new NativeExtraLogCatSetting(
                                config.getExtraLogCatWithNativeCrash().getLogcatMainLines(),
                                config.getExtraLogCatWithNativeCrash().getLogcatSystemLines(),
                                config.getExtraLogCatWithNativeCrash().getLogcatEventsLines());
                    }
                    NativeExtraLogCatSetting anrExtraLogCatSetting = null;

                    if (config.getExtraLogCatWithANR() != null) {
                        anrExtraLogCatSetting = new NativeExtraLogCatSetting(
                                config.getExtraLogCatWithANR().getLogcatMainLines(),
                                config.getExtraLogCatWithANR().getLogcatSystemLines(),
                                config.getExtraLogCatWithANR().getLogcatEventsLines());
                    }

                    NativeEngineInit.init(application, filePath, enableTrackAppCrash, enableTrackAppANR,
                            crashCallback,
                            nativeExtraLogCatSetting,
                            anrExtraLogCatSetting);
                } else {
                    NativeEngineInit.init(application, filePath, enableTrackAppCrash, enableTrackAppANR,
                            crashCallback);
                }

                //补充上传没有成功上传的 Native Crash，上传 ANR Crash
                FTExceptionHandler.get().checkAndSyncPreDump(filePath, null);


            } else {
                NativeEngineInit.init(application, filePath, enableTrackAppCrash, enableTrackAppANR);
                FTExceptionHandler.get().checkAndSyncPreDump(filePath, null);
            }

        }

    }

    /**
     * RUM 是否开启
     *
     * @return
     */
    public boolean isRumEnable() {
        return config != null && config.getRumAppId() != null;
    }

    @Nullable
    public FTRUMConfig getConfig() {
        return config;
    }

    /**
     * 返回自定义覆盖全局的 {@link FTResourceEventListener.FTFactory}
     *
     * @return
     */
    FTResourceEventListener.FTFactory getOverrideEventListener() {
        if (config == null) return null;
        if (config.getOkHttpEventListenerHandler() == null) return null;
        return config.getOkHttpEventListenerHandler().getEventListenerFTFactory();
    }


    /**
     * userDataBinded
     * 用户数据是否已经绑定完成
     *
     * @return
     */
    boolean isUserDataBinded() {
        return getUserData() != null;
    }


    /**
     * 初始化随机 userid
     */
    void initRandomUserId() {
        if (Utils.isNullOrEmpty(getRandomUserId())) {
            createNewRandomUserId();
        }
    }


    public void createNewRandomUserId() {
        randomUserId = "ft.rd_" + Utils.randomUUID();
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        sp.edit().putString(Constants.FT_RANDOM_USER_ID, randomUserId).apply();
    }


    String getRandomUserId() {
        if (!Utils.isNullOrEmpty(randomUserId)) {
            return randomUserId;
        }
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        randomUserId = sp.getString(Constants.FT_RANDOM_USER_ID, null);
        return randomUserId;
    }

    /**
     * 获取绑定用户信息
     *
     * @return
     */
    UserData getUserData() {
        synchronized (mLock) {
            if (mUserData != null) {
                return mUserData;
            }

            SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
            String id = sp.getString(Constants.FT_USER_USER_ID, null);
            String userName = sp.getString(Constants.FT_USER_USER_NAME, null);
            String email = sp.getString(Constants.FT_USER_USER_EMAIL, null);
            String ext = sp.getString(Constants.FT_USER_USER_EXT, null);
            if (id == null) {
                return null;
            } else {
                UserData data = new UserData();
                data.setId(id);
                data.setEmail(email);
                data.setName(userName);
                if (ext != null) {
                    data.setExtsWithJsonString(ext);
                }
                return data;
            }
        }
    }

    /**
     * 绑定用户信息
     *
     * @param name
     * @param id
     * @param exts
     */
    void bindUserData(String id, String name, String email, HashMap<String, String> exts) {
        LogUtils.d(TAG, "bindUserData:id=" + id + ",name=" + name + ",email=" + email + ",exts=" + exts);
        //绑定用户信息
        synchronized (mLock) {
            SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
            sp.edit().putString(Constants.FT_USER_USER_ID, id).apply();
            sp.edit().putString(Constants.FT_USER_USER_NAME, name).apply();
            sp.edit().putString(Constants.FT_USER_USER_EMAIL, email).apply();
            sp.edit().putString(Constants.FT_USER_USER_EXT, exts != null ? Utils.hashMapObjectToJson(exts) : null).apply();
            UserData data = new UserData();
            data.setId(id);
            data.setName(name);
            data.setEmail(email);
            data.setExts(exts);
            mUserData = data;
        }
    }

    /**
     * 解绑用户信息
     */
    void unbindUserData() {
        LogUtils.d(TAG, "unbindUserData");

        synchronized (mLock) {
            SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
            sp.edit().remove(Constants.FT_USER_USER_ID).apply();
            sp.edit().remove(Constants.FT_USER_USER_NAME).apply();
            sp.edit().remove(Constants.FT_USER_USER_EXT).apply();
            mUserData = null;
        }
    }

    /**
     * 初始化 RUM GlobalContext
     *
     * @param config
     */
    void initRUMGlobalContext(FTRUMConfig config) {
        Context context = FTApplication.getApplication();
        HashMap<String, Object> rumGlobalContext = config.getGlobalContext();
        if (!rumGlobalContext.isEmpty()) {
            rumGlobalContext.put(Constants.KEY_RUM_CUSTOM_KEYS, Utils.setToJsonString(rumGlobalContext.keySet()));
        }
        rumGlobalContext.put(Constants.KEY_RUM_APP_ID, config.getRumAppId());
        rumGlobalContext.put(Constants.KEY_RUM_SESSION_TYPE, "user");
        rumGlobalContext.put(Constants.KEY_DEVICE_OS, DeviceUtils.getOSName());
        rumGlobalContext.put(Constants.KEY_DEVICE_DEVICE_BAND, DeviceUtils.getDeviceBand());
        rumGlobalContext.put(Constants.KEY_DEVICE_DEVICE_MODEL, DeviceUtils.getDeviceModel());
        rumGlobalContext.put(Constants.KEY_DEVICE_DEVICE_ARCH, DeviceUtils.getDeviceArch());
        rumGlobalContext.put(Constants.KEY_DEVICE_DISPLAY, DeviceUtils.getDisplay(context));
        rumGlobalContext.put(Constants.KEY_SERVICE, config.getServiceName());

        String osVersion = DeviceUtils.getOSVersion();
        rumGlobalContext.put(Constants.KEY_DEVICE_OS_VERSION, osVersion);
        String osVersionMajor = osVersion.contains(".") ? osVersion.split("\\.")[0] : osVersion;
        rumGlobalContext.put(Constants.KEY_DEVICE_OS_VERSION_MAJOR, osVersionMajor);
    }

    HashMap<String, Object> getRUMPublicDynamicTags() {
        return getRUMPublicDynamicTags(false);
    }

    /**
     * 获取变化的公用 tag
     *
     * @return
     */
    HashMap<String, Object> getRUMPublicDynamicTags(boolean includeRUMStatic) {
        HashMap<String, Object> tags = new HashMap<>();
        if (includeRUMStatic) {
            tags.putAll(FTTrackInner.getInstance().getCurrentDataHelper().getCurrentRumTags());
        }
        tags.put(Constants.KEY_RUM_NETWORK_TYPE, NetUtils.getNetWorkStateName());
        tags.put(Constants.KEY_RUM_IS_SIGN_IN, FTRUMConfigManager.get().isUserDataBinded() ? "T" : "F");
        if (FTRUMConfigManager.get().isUserDataBinded()) {
            UserData data = FTRUMConfigManager.get().getUserData();
            tags.put(Constants.KEY_RUM_USER_ID, FTRUMConfigManager.get().getUserData().getId());
            if (data.getName() != null) {
                tags.put(Constants.KEY_RUM_USER_NAME, data.getName());
            }

            if (data.getEmail() != null) {
                tags.put(Constants.KEY_RUM_USER_EMAIL, data.getEmail());
            }
            if (data.getExts() != null) {
                HashMap<String, String> ext = data.getExts();
                for (String key : ext.keySet()) {
                    tags.put(key, ext.get(key));
                }
            }
        } else {
            tags.put(Constants.KEY_RUM_USER_ID, getRandomUserId());
        }
        return tags;
    }

    void release() {
        config = null;
    }
}
