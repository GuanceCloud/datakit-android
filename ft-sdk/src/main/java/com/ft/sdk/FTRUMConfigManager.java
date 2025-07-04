package com.ft.sdk;

import static com.ft.sdk.FTApplication.getApplication;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.db.FTDBCachePolicy;
import com.ft.sdk.garble.threadpool.RunnerCompleteCallBack;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.PackageUtils;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.garble.utils.VersionUtils;
import com.ft.sdk.nativelib.CrashCallback;
import com.ft.sdk.nativelib.NativeEngineInit;
import com.ft.sdk.nativelib.NativeExtraLogCatSetting;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    /**
     * Bind user data
     */
    private UserData mUserData;


    /**
     * User data synchronization lock
     */
    private final Object mLock = new Object();


    /**
     * RUM condition initialization
     *
     * @param config {@link FTRUMConfig} configuration information
     */
    void initWithConfig(FTRUMConfig config) {
        this.config = config;

        FTDBCachePolicy.get().initRUMParam(config);
        FTRUMInnerManager.get().initParams(config);
        FTRUMGlobalManager.get().initConfig(config);
        FTExceptionHandler.get().initConfig(config);
        FTMonitorManager.get().initWithConfig(config);
        FTUIBlockManager.get().start(config);
        FTANRDetector.get().init(config);
        initRUMGlobalContext(config);
        FTTrackInner.getInstance().initRUMConfig(config);
        if (config.isRumEnable() && config.isEnableTraceUserAction()) {
            //Handle flutter reactNative application lifecycle starting earlier than condition setting
            FTAppStartCounter.get().checkToReUpload();
        }

        initNativeDump(config);
    }

    /**
     * Initialize Native Library configuration.
     * For versions below {@link FTExceptionHandler#NATIVE_CALLBACK_VERSION}, check if there are dump files, upload if any.
     * For versions above {@link FTExceptionHandler#NATIVE_CALLBACK_VERSION}, set Native version callback, and supplement upload for data that failed to callback,
     * and mark as {@link FTExceptionHandler#IS_PRE_CRASH}
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
                LogUtils.e(TAG, "Native crash collection not started");
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
                        //fixme If native crash file is too large, there may be performance issues here
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
                            latch.await(800, TimeUnit.MILLISECONDS);
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

                //Supplement upload for Native Crash that failed to upload, upload ANR Crash
                FTExceptionHandler.get().checkAndSyncPreDump(filePath, null);


            } else {
                NativeEngineInit.init(application, filePath, enableTrackAppCrash, enableTrackAppANR);
                FTExceptionHandler.get().checkAndSyncPreDump(filePath, null);
            }

        }

    }

    /**
     * Whether RUM is enabled
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
     * Return custom override global {@link FTResourceEventListener.FTFactory}
     *
     * @return
     */
    FTResourceEventListener.FTFactory getOverrideEventListener() {
        if (config == null) return null;
        if (config.getOkHttpEventListenerHandler() == null) return null;
        return config.getOkHttpEventListenerHandler().getEventListenerFTFactory();
    }


    /**
     * Return custom override global {@link FTResourceInterceptor}
     *
     * @return
     */
    FTResourceInterceptor.ContentHandlerHelper getOverrideResourceContentHandler() {
        if (config == null) return null;
        if (config.getOkHttpResourceContentHandler() == null) return null;
        return config.getOkHttpResourceContentHandler();
    }

    /**
     * userDataBinded
     * Whether user data binding is completed
     *
     * @return
     */
    boolean isUserDataBinded() {
        return getUserData() != null;
    }


    /**
     * Get bound user information
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
     * Bind user information
     *
     * @param name
     * @param id
     * @param exts
     */
    void bindUserData(String id, String name, String email, HashMap<String, String> exts) {
        LogUtils.d(TAG, "bindUserData:id=" + id + ",name=" + name + ",email=" + email + ",exts=" + exts);
        //Bind user information
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
     * Unbind user information
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
     * Initialize RUM GlobalContext
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


    /**
     * Get changed public tags
     *
     * @return
     */
    HashMap<String, Object> getRUMPublicDynamicTags() {
        HashMap<String, Object> tags = new HashMap<>();
        tags.put(Constants.KEY_RUM_NETWORK_TYPE, FTNetworkListener.get().getNetworkStateBean().getNetworkType());
        tags.put(Constants.KEY_RUM_IS_SIGN_IN, isUserDataBinded() ? "T" : "F");
        if (isUserDataBinded()) {
            UserData data = getUserData();
            tags.put(Constants.KEY_RUM_USER_ID, data.getId());
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
            tags.put(Constants.KEY_RUM_USER_ID, LocalUUIDManager.get().getRandomUUID());
        }
        return tags;
    }

    void release() {
        config = null;
    }
}
