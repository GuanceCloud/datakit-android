package com.ft.sdk;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTMonitorConfigManager;
import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.PackageUtils;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.nativelib.NativeEngineInit;

import org.json.JSONObject;

import java.io.File;
import java.util.UUID;

import static com.ft.sdk.FTApplication.getApplication;

public class FTRUMConfigManager {

    private static final String TAG = "FTRUMConfigManager";

    private static class SingletonHolder {
        private static final FTRUMConfigManager INSTANCE = new FTRUMConfigManager();
    }

    public static FTRUMConfigManager get() {
        return FTRUMConfigManager.SingletonHolder.INSTANCE;
    }

    private FTRUMConfig config;

    private FTActivityLifecycleCallbacks life;


    private volatile String randomUserId;
    private UserData mUserData;

    private final Object mLock = new Object();


    public void initWithConfig(FTRUMConfig config) {
        this.config = config;
        registerActivityLifeCallback();
        FTRUMGlobalManager.get().initParams(config);
//        FTAutoTrackConfigManager.get().initParams();
        FTExceptionHandler.get().initConfig(config);
        initRandomUserId();
        FTMonitorConfigManager.get().initWithConfig(config);
        FTUIBlockManager.start(config);

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
     * 添加 Activity 生命周期监控
     */
    private void registerActivityLifeCallback() {
        life = new FTActivityLifecycleCallbacks();
        getApplication().registerActivityLifecycleCallbacks(life);
    }

    /**
     * 解绑 Activity 生命周期监控
     */
    void unregisterActivityLifeCallback() {
        if (life != null) {
            getApplication().unregisterActivityLifecycleCallbacks(life);
            life = null;
        }
    }


    /**
     * userDataBinded
     * 用户数据是否已经绑定完成
     *
     * @return
     */
    public boolean isUserDataBinded() {
        return getUserData() != null;
    }


    public void initRandomUserId() {
        if (Utils.isNullOrEmpty(getRandomUserId())) {
            createNewRandomUserId();
        }
    }


    public void createNewRandomUserId() {
        randomUserId = "ft.rd_" + UUID.randomUUID().toString();
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        sp.edit().putString(Constants.FT_RANDOM_USER_ID, randomUserId).apply();
    }


    public String getRandomUserId() {
        if (!Utils.isNullOrEmpty(randomUserId)) {
            return randomUserId;
        }
        SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
        randomUserId = sp.getString(Constants.FT_RANDOM_USER_ID, null);
        return randomUserId;
    }


    public UserData getUserData() {
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
    public void bindUserData(@NonNull String id) {
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
    public void unbindUserData() {
        LogUtils.d(TAG, "解绑用户信息");

        synchronized (mLock) {
            SharedPreferences sp = Utils.getSharedPreferences(FTApplication.getApplication());
            sp.edit().remove(Constants.FT_USER_USER_ID).apply();
            sp.edit().remove(Constants.FT_USER_USER_NAME).apply();
            sp.edit().remove(Constants.FT_USER_USER_EXT).apply();
            mUserData = null;
        }
    }


    void release() {
        config = null;
    }
}
