package com.ft.sdk;

import android.app.Application;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTAliasConfig;
import com.ft.sdk.garble.FTAutoTrackConfig;
import com.ft.sdk.garble.FTDBCachePolicy;
import com.ft.sdk.garble.FTUserActionConfig;
import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.FTMonitorConfig;
import com.ft.sdk.garble.FTRUMConfig;
import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.utils.LocationUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.PackageUtils;
import com.ft.sdk.garble.utils.Utils;
import com.ft.sdk.nativelib.NativeEngineInit;

import java.io.File;
import java.security.InvalidParameterException;


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
    public static final String PLUGIN_MIN_VERSION = BuildConfig.MIN_FT_PLUGIN_VERSION; //当前 SDK 支持的最小 Plugin 版本
    private static FTSdk mFtSdk;
    public FTSDKConfig mFtSDKConfig;
    private FTActivityLifecycleCallbacks life;

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
            throw new InvalidParameterException("参数 ftSDKConfig 不能为 null");
        } else {
            mFtSdk = new FTSdk(ftSDKConfig);
            boolean onlyMain = ftSDKConfig.isOnlySupportMainProcess();
            if (onlyMain && !Utils.isMainProcess()) {
                throw new InitSDKProcessException("当前 SDK 只能在主进程中运行，如果想要在非主进程中运行可以设置 FTSDKConfig.setOnlySupportMainProcess(false)");
            }
        }
        mFtSdk.registerActivityLifeCallback();
        mFtSdk.initFTConfig();
    }

    /**
     * SDK 初始化后，获得 SDK 对象
     *
     * @return
     */
    public static synchronized FTSdk get() throws InvalidParameterException {
        if (mFtSdk == null) {
            throw new InvalidParameterException("请先安装SDK(在应用启动时调用FTSdk.install(FTSDKConfig ftSdkConfig,Application application))");
        }
        return mFtSdk;
    }

    /**
     * 关闭 SDK 正在做的操作
     */
    public void shutDown() {
        SyncTaskManager.release();
        FTUserConfig.release();
        FTMonitorConfig.release();
        FTAutoTrackConfig.release();
        FTHttpConfig.release();
        FTNetworkListener.get().release();
        FTUserActionConfig.release();
        LocationUtils.get().stopListener();
        FTExceptionHandler.release();
        FTDBCachePolicy.release();
        unregisterActivityLifeCallback();
        LogUtils.w(TAG, "FT SDK 已经被关闭");
    }

    /**
     * 返回当前的 Application
     *
     * @return
     */
    public Application getApplication() {
        return FTApplication.getApplication();
    }

    /**
     * 注销用户信息
     */
    public void unbindUserData() {
        if (mFtSDKConfig != null) {
            LogUtils.d(TAG, "解绑用户信息");
            //解绑用户信息
            FTUserConfig.get().unbindUserData();
            //清除本地缓存的SessionId
        }
    }

    /**
     * 绑定用户信息
     *
     * @param id
     */
    public void bindUserData(@NonNull String id) {
        if (mFtSDKConfig != null) {
//            if (mFtSDKConfig.isNeedBindUser()) {
            LogUtils.d(TAG, "绑定用户信息");
//            //如果本地的SessionID已经绑定了用于就重新生成sessionId进行绑定
//            if (FTUserConfig.get().currentSessionHasUser()) {
//                FTUserConfig.get().clearSessionId();
//            }
//            //初始化SessionId
            FTUserConfig.get().initSessionId();
//            //绑定用户信息
            FTUserConfig.get().bindUserData("", id, null);
//            }
        }
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
    private void initFTConfig() {
        if (mFtSDKConfig != null) {
            LogUtils.setDebug(mFtSDKConfig.isDebug());
            RUMGlobalManager.getInstance().init();
//            LogUtils.setDescLogShow(mFtSDKConfig.isDescLog());
            FTAliasConfig.get().initParams(mFtSDKConfig);
            FTHttpConfig.get().initParams(mFtSDKConfig);
            FTAutoTrackConfig.get().initParams(mFtSDKConfig);
            FTDBCachePolicy.get().initParam(mFtSDKConfig);
            FTRUMConfig.get().initParam(mFtSDKConfig);
            FTUserConfig.get().initSessionId();
//            if (mFtSDKConfig.isNeedBindUser()) {
//                FTUserConfig.get().initUserDataFromDB();
//            }
            FTNetworkListener.get().monitor();
            if (mFtSDKConfig.isEnableTraceUserAction()) {
                FTUserActionConfig.get().initParams(mFtSDKConfig);
            }

            float rate = mFtSDKConfig.getSamplingRate();
            if (rate > 1 || rate < 0) {
                throw new
                        IllegalArgumentException("rate 值的范围应在[0,1]");
            }
            //设置采样率
            Utils.traceSamplingRate = rate;
            FTExceptionHandler.get().initParams(mFtSDKConfig);
            FTMonitorConfig.get().initParams(mFtSDKConfig);
            FTUIBlockManager.start(mFtSDKConfig);

            initNativeDump();

        }
    }

    /**
     * 初始化 Native 路径
     */
    private void initNativeDump() {
        boolean isNativeLibSupport = PackageUtils.isNativeLibrarySupport();

        if (isNativeLibSupport) {
            NATIVE_VERSION = com.ft.sdk.nativelib.BuildConfig.VERSION_NAME;
        }

        boolean enableTrackAppCrash = mFtSDKConfig.isEnableTrackAppCrash();
        boolean enableTrackAppANR = mFtSDKConfig.isEnableTrackAppANR();
        if (enableTrackAppCrash || enableTrackAppANR) {
            if (!isNativeLibSupport) {
                LogUtils.e(TAG, "未启动 native 崩溃收集");
                return;
            }

            Application application = FTApplication.getApplication();
            File crashFilePath = new File(application.getFilesDir(), NATIVE_DUMP_PATH);
            if (!crashFilePath.exists()) {
                crashFilePath.mkdirs();
            }

            String filePath = crashFilePath.toString();
            NativeEngineInit.init(application, filePath, enableTrackAppCrash, enableTrackAppANR);
            FTExceptionHandler.get().checkAndSyncPreDump(filePath);
        }

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
    private void unregisterActivityLifeCallback() {
        if (life != null) {
            getApplication().unregisterActivityLifecycleCallbacks(life);
            life = null;
        }
    }

    /**
     * 设置开启网络请求追踪
     *
     * @param networkTrace
     */
    public void setNetworkTrace(boolean networkTrace) {
        if (mFtSDKConfig == null) {
            throw new InvalidParameterException("需要预先调用 install ");
        }
        mFtSDKConfig.setNetworkTrace(networkTrace);
        FTHttpConfig.get().networkTrace = networkTrace;
    }


}
