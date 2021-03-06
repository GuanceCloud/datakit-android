package com.ft.sdk;

import static com.ft.sdk.FTApplication.getApplication;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.NetUtils;
import com.ft.sdk.garble.utils.PackageUtils;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.nativelib.NativeEngineInit;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FTRUMConfigManager {

    private static final String TAG = "FTRUMConfigManager";

    private static class SingletonHolder {
        private static final FTRUMConfigManager INSTANCE = new FTRUMConfigManager();
    }

    public static FTRUMConfigManager get() {
        return FTRUMConfigManager.SingletonHolder.INSTANCE;
    }

    private FTRUMConfig config;

    private volatile String randomUserId;
    private UserData mUserData;

    private final Object mLock = new Object();


    void initWithConfig(FTRUMConfig config) {
        this.config = config;
        FTRUMGlobalManager.get().initParams(config);
//        FTAutoTrackConfigManager.get().initParams();
        FTExceptionHandler.get().initConfig(config);
        initRandomUserId();
        FTMonitorConfigManager.get().initWithConfig(config);
        FTUIBlockManager.get().start(config);
        initRUMGlobalContext(config);
        if (config.isRumEnable() && config.isEnableTraceUserAction()) {
            FTAppStartCounter.get().checkToReUpload();
        }

        initNativeDump();
    }

    /**
     * 初始化 Native 路径
     */
    private void initNativeDump() {
        boolean isNativeLibSupport = PackageUtils.isNativeLibrarySupport();

        if (isNativeLibSupport) {
            FTSdk.NATIVE_VERSION = com.ft.sdk.nativelib.BuildConfig.VERSION_NAME;
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
            NativeEngineInit.init(application, filePath, enableTrackAppCrash, enableTrackAppANR);
            FTExceptionHandler.get().checkAndSyncPreDump(filePath);
        }

    }

    public boolean isRumEnable() {
        return config != null && config.getRumAppId() != null;
    }

    public FTRUMConfig getConfig() {
        return config;
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


    void initRandomUserId() {
        if (Utils.isNullOrEmpty(getRandomUserId())) {
            createNewRandomUserId();
        }
    }


    public void createNewRandomUserId() {
        randomUserId = "ft.rd_" + UUID.randomUUID().toString();
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


    UserData getUserData() {
        synchronized (mLock) {
            if (mUserData != null) {
                return mUserData;
            }

            SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
            String id = sp.getString(Constants.FT_USER_USER_ID, null);
            String userName = sp.getString(Constants.FT_USER_USER_NAME, null);
            String ext = sp.getString(Constants.FT_USER_USER_EXT, null);
            if (id == null) {
                return null;
            } else {
                UserData data = new UserData();
                data.setId(id);
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
     * @param id
     */
    void bindUserData(@NonNull String id) {
        LogUtils.d(TAG, "绑定用户信息");
        //初始化SessionId
        initRandomUserId();
        //绑定用户信息
        bindUserData("", id, null);
    }

    /**
     * 绑定用户信息
     *
     * @param name
     * @param id
     * @param exts
     */
    private void bindUserData(String name, String id, JSONObject exts) {
        synchronized (mLock) {
            SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
            sp.edit().putString(Constants.FT_USER_USER_ID, id).apply();
            sp.edit().putString(Constants.FT_USER_USER_NAME, name).apply();
            sp.edit().putString(Constants.FT_USER_USER_EXT, exts != null ? exts.toString() : null).apply();
            UserData data = new UserData();
            data.setId(id);
            data.setName(name);
            data.setExts(exts);
            mUserData = data;
        }
    }

    /**
     * 解绑用户信息
     */
    void unbindUserData() {
        LogUtils.d(TAG, "解绑用户信息");

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
        ArrayList<String> customKeys = new ArrayList<>();
        for (Map.Entry<String, Object> entry : rumGlobalContext.entrySet()) {
            String key = entry.getKey();
            customKeys.add(key);
            Object value = entry.getValue();
            rumGlobalContext.put(key, value.toString());
        }
//        if(config.isBackendSample()){
        //sample
//            rumGlobalContext.put(Constants.KEY_BACKENDSAMPLE,"");
//        }
        rumGlobalContext.put(Constants.KEY_RUM_CUSTOM_KEYS, new Gson().toJson(customKeys));
        rumGlobalContext.put(Constants.KEY_RUM_APP_ID, config.getRumAppId());
        rumGlobalContext.put(Constants.KEY_RUM_SESSION_TYPE, "user");
        rumGlobalContext.put(Constants.KEY_DEVICE_OS, DeviceUtils.getOSName());
        rumGlobalContext.put(Constants.KEY_DEVICE_DEVICE_BAND, DeviceUtils.getDeviceBand());
        rumGlobalContext.put(Constants.KEY_DEVICE_DEVICE_MODEL, DeviceUtils.getDeviceModel());
        rumGlobalContext.put(Constants.KEY_DEVICE_DISPLAY, DeviceUtils.getDisplay(context));

        String osVersion = DeviceUtils.getOSVersion();
        rumGlobalContext.put(Constants.KEY_DEVICE_OS_VERSION, osVersion);
        String osVersionMajor = osVersion.contains(".") ? osVersion.split("\\.")[0] : osVersion;
        rumGlobalContext.put(Constants.KEY_DEVICE_OS_VERSION_MAJOR, osVersionMajor);
    }

    JSONObject getRUMPublicDynamicTags() throws Exception {
        return getRUMPublicDynamicTags(false);
    }

    /**
     * 获取变化的公用 tag
     *
     * @return
     */
    JSONObject getRUMPublicDynamicTags(boolean includeRUMStatic) throws Exception {
        JSONObject tags = new JSONObject();
        if (includeRUMStatic) {
            HashMap<String, Object> rumGlobalContext = config.getGlobalContext();
            for (Map.Entry<String, Object> entry : rumGlobalContext.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                tags.put(key, value.toString());
            }
        }
        tags.put(Constants.KEY_RUM_NETWORK_TYPE, NetUtils.get().getNetWorkStateName());
        tags.put(Constants.KEY_RUM_IS_SIGN_IN, FTRUMConfigManager.get().isUserDataBinded() ? "T" : "F");
        if (FTRUMConfigManager.get().isUserDataBinded()) {
            tags.put(Constants.KEY_RUM_USER_ID, FTRUMConfigManager.get().getUserData().getId());
        } else {
            tags.put(Constants.KEY_RUM_USER_ID, FTRUMGlobalManager.get().getSessionId());
        }
        return tags;
    }

    void release() {
        config = null;
    }
}
