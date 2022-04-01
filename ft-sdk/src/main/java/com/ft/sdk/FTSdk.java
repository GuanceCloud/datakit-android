package com.ft.sdk;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTAutoTrackConfigManager;
import com.ft.sdk.garble.FTDBCachePolicy;
import com.ft.sdk.garble.FTHttpConfigManager;
import com.ft.sdk.garble.threadpool.EventConsumerThreadPool;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.DeviceUtils;
import com.ft.sdk.garble.utils.LocationUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.OaidUtils;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.internal.exception.FTInitSDKProcessException;

import java.util.HashMap;


/**
 * BY huangDianHua
 * DATE:2019-11-29 17:15
 * Description:
 */
public class FTSdk {
    public final static String TAG = "FTSdk";
    public static final String NATIVE_DUMP_PATH = "ftCrashDmp";
    //该变量不能改动，其值由 Plugin 动态改写
    public static String PLUGIN_VERSION = "";
    public static String NATIVE_VERSION = "";
    //变量由 Plugin 写入，同一个编译版本，UUID 相同
    public static String PACKAGE_UUID = "";
    //下面两个变量也不能随便改动，改动请同时更改 plugin 中对应的值
    public static final String AGENT_VERSION = BuildConfig.FT_SDK_VERSION;//当前SDK 版本
    private static FTSdk mFtSdk;
    private final FTSDKConfig mFtSDKConfig;

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
            if (onlyMain && !Utils.isMainProcess()) {
                throw new FTInitSDKProcessException("当前 SDK 只能在主进程中运行，如果想要在非主进程中运行可以设置 FTSDKConfig.setOnlySupportMainProcess(false)");
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
     * @return
     */
    static boolean checkInstallState() {
        return mFtSdk != null && mFtSdk.mFtSDKConfig != null;
    }

    /**
     * 关闭 SDK 正在做的操作
     */
    public static void shutDown() {
        SyncTaskManager.release();
        FTRUMConfigManager.get().release();
        FTMonitorConfigManager.release();
        FTAutoTrackConfigManager.release();
        FTHttpConfigManager.release();
        FTNetworkListener.get().release();
        LocationUtils.get().stopListener();
        FTExceptionHandler.release();
        FTDBCachePolicy.release();
        FTUIBlockManager.release();
        FTTraceConfigManager.get().release();
        FTLoggerConfigManager.get().release();
        FTRUMGlobalManager.get().release();
        EventConsumerThreadPool.get().shutDown();
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
            FTRUMConfigManager.get().initWithConfig(config);
        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());
        }

    }

    /**
     * 设置 Trace 配置
     *
     * @param config
     */
    public static void initTraceWithConfig(@NonNull FTTraceConfig config) {
        try {
            FTTraceConfigManager.get().initWithConfig(config);
        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());
        }
    }

    /**
     * 设置 log 配置
     *
     * @param config
     */
    public static void initLogWithConfig(@NonNull FTLoggerConfig config) {
        try {
            FTLoggerConfigManager.get().initWithConfig(config);
        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());
        }
    }

    /**
     * 绑定用户信息
     *
     * @param id
     */
    public static void bindRumUserData(@NonNull String id) {
        FTRUMConfigManager.get().bindUserData(id);
    }

    /**
     * 解绑用户数据
     */
    public static void unbindRumUserData() {
        FTRUMConfigManager.get().unbindUserData();
    }

    /**
     * 获取公用 tags
     *
     * @return
     */
    HashMap<String, Object> getBasePublicTags() {
        return mFtSDKConfig.getGlobalContext();
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
        hashMap.put(Constants.KEY_ENV, config.getEnv().toString());
        String uuid = "";
        if (FTHttpConfigManager.get().useOaid) {
            String oaid = OaidUtils.getOAID(FTApplication.getApplication());
            if (oaid != null) {
                uuid = "oaid_" + oaid;
            }
        }
        if (uuid.isEmpty()) {
            uuid = DeviceUtils.getUuid(FTApplication.getApplication());
        }
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


}
