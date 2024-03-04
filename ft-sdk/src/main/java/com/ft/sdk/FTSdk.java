package com.ft.sdk;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTAutoTrackConfigManager;
import com.ft.sdk.garble.FTDBCachePolicy;
import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.threadpool.EventConsumerThreadPool;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.PackageUtils;
import com.ft.sdk.garble.utils.Utils;

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
    public static String NATIVE_VERSION = PackageUtils.isNativeLibrarySupport() ? com.ft.sdk.nativelib.BuildConfig.VERSION_NAME : "";
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
        if (ftSDKConfig == null) {
            LogUtils.e(TAG, "参数 ftSDKConfig 不能为 null");
            return;
        } else {
            mFtSdk = new FTSdk(ftSDKConfig);
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
        }
        mFtSdk.initFTConfig(ftSDKConfig);
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
        FTNetworkListener.get().release();
//        LocationUtils.get().stopListener();
        FTExceptionHandler.release();
        FTDBCachePolicy.release();
        FTUIBlockManager.get().release();
        FTTraceConfigManager.get().release();
        FTLoggerConfigManager.get().release();
        FTRUMGlobalManager.get().release();
        FTRUMInnerManager.get().release();
        EventConsumerThreadPool.get().shutDown();
        FTANRDetector.get().release();
        FTDBManager.release();
        mFtSdk = null;
        LogUtils.w(TAG, "FT SDK 已经被关闭");
    }

//    /**
//     * 开启定，并且获取定位结果
//     */
//    public static void startLocation(String geoKey, AsyncCallback syncCallback) {
//        if (!Utils.isNullOrEmpty(geoKey)) {
//            LocationUtils.get().setGeoKey(geoKey);
//            LocationUtils.get().setUseGeoKey(true);
//        }
//        LocationUtils.get().startLocationCallBack(syncCallback);
//    }

//    /**
//     * j
//     * 创建获取 GPU 信息的GLSurfaceView
//     *
//     * @param root
//     */
//    public void setGpuRenderer(ViewGroup root) {
//        try {
//            if (FTMonitorConfig.get().isMonitorType(MonitorType.GPU)) {
//                LogUtils.d(TAG, "绑定视图监听 GPU 信息");
//                Context context = getApplication();
//                final RendererUtil mRendererUtil = new RendererUtil();
//                GLSurfaceView mGLSurfaceView = new GLSurfaceView(context);
//                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(1, 1);
//                mGLSurfaceView.setLayoutParams(layoutParams);
//                root.addView(mGLSurfaceView);
//                mGLSurfaceView.setEGLContextClientVersion(1);
//                mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
//                mGLSurfaceView.setRenderer(mRendererUtil);
//                mGLSurfaceView.post(() -> {
//                    String gl_vendor = mRendererUtil.gl_vendor;
//                    String gl_renderer = mRendererUtil.gl_renderer;
//                    GpuUtils.GPU_VENDOR_RENDERER = gl_vendor + "_" + gl_renderer;
//                    if (gl_renderer != null && gl_vendor != null) {
//                        mGLSurfaceView.surfaceDestroyed(mGLSurfaceView.getHolder());
//                    }
//                });
//            }
//        } catch (Exception e) {
//        }
//    }

    /**
     * 初始化SDK本地配置数据
     */
    private void initFTConfig(FTSDKConfig config) {
        LogUtils.setDebug(config.isDebug());
        FTHttpConfigManager.get().initParams(config);
        FTNetworkListener.get().monitor();
        appendGlobalContext(config);
        SyncTaskManager.get().init(config);
        LogUtils.d(TAG,"initFTConfig complete");
//            LogUtils.setDescLogShow(mFtSDKConfig.isDescLog());
    }


    public FTSDKConfig getBaseConfig() {
        return mFtSDKConfig;
    }


    /**
     * 设置 RUM 配置
     *
     * @param config
     */
    public static void initRUMWithConfig(@NonNull FTRUMConfig config) {
        try {
            config.setServiceName(get().getBaseConfig().getServiceName());
            FTRUMConfigManager.get().initWithConfig(config);
            LogUtils.d(TAG,"initRUMWithConfig complete");

        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));
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
            FTTraceConfigManager.get().initWithConfig(config);
            LogUtils.d(TAG,"initTraceWithConfig complete");

        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));
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
            FTLoggerConfigManager.get().initWithConfig(config);
            LogUtils.d(TAG,"initLogWithConfig complete");

        } catch (Exception e) {
            LogUtils.e(TAG, Log.getStackTraceString(e));
        }
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
            currentConfig.getGlobalContext().put(Constants.KEY_DEVICE_UUID, uuid);

        }
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
        String uuid = config.isEnableAccessAndroidID() ? DeviceUtils.getUuid(FTApplication.getApplication()) : "";
        hashMap.put(Constants.KEY_DEVICE_UUID, uuid);
        hashMap.put(Constants.KEY_RUM_SDK_PACKAGE_AGENT, FTSdk.AGENT_VERSION);
        if (!FTSdk.PLUGIN_VERSION.isEmpty()) {
            hashMap.put(Constants.KEY_RUM_SDK_PACKAGE_TRACK, FTSdk.PLUGIN_VERSION);
        }
        if (!FTSdk.NATIVE_VERSION.isEmpty()) {
            hashMap.put(Constants.KEY_RUM_SDK_PACKAGE_NATIVE, FTSdk.NATIVE_VERSION);
        }
        hashMap.put(Constants.KEY_SDK_VERSION, FTSdk.AGENT_VERSION);
    }


    /**
     * 主动同步数据
     */
    public static void flushSyncData() {
        SyncTaskManager.get().executePoll();
    }


}
