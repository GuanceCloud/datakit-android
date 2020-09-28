package com.ft.sdk;

import android.app.Application;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTActivityLifecycleCallbacks;
import com.ft.sdk.garble.FTAliasConfig;
import com.ft.sdk.garble.FTAutoTrackConfig;
import com.ft.sdk.garble.FTDBCachePolicy;
import com.ft.sdk.garble.FTExceptionHandler;
import com.ft.sdk.garble.FTFlowConfig;
import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.FTMonitorConfig;
import com.ft.sdk.garble.FTNetworkListener;
import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.SyncCallback;
import com.ft.sdk.garble.manager.FTUICatonManager;
import com.ft.sdk.garble.manager.SyncTaskManager;
import com.ft.sdk.garble.utils.GpuUtils;
import com.ft.sdk.garble.utils.LocationUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.RendererUtil;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONObject;

import java.security.InvalidParameterException;


/**
 * BY huangDianHua
 * DATE:2019-11-29 17:15
 * Description:
 */
public class FTSdk {
    public final static String TAG = "FTSdk";
    //该变量不能改动，其值由 Plugin 动态改写
    public static String PLUGIN_VERSION = "";
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
        FTFlowConfig.release();
        LocationUtils.get().stopListener();
        FTExceptionHandler.release();
        FTDBCachePolicy.release();
        FTUICatonManager.release();
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
            if (mFtSDKConfig.isNeedBindUser()) {
                LogUtils.d(TAG, "解绑用户信息");
                //解绑用户信息
                FTUserConfig.get().unbindUserData();
                //清除本地缓存的SessionId
                FTUserConfig.get().clearSessionId();
                //创建新的sessionId用于标记后续操作
                FTUserConfig.get().createNewSessionId();
            }
        }
    }

    /**
     * 绑定用户信息
     *
     * @param name
     * @param id
     * @param extras
     */
    public void bindUserData(@NonNull String name, @NonNull String id, JSONObject extras) {
        if (mFtSDKConfig != null) {
            if (mFtSDKConfig.isNeedBindUser()) {
                LogUtils.d(TAG, "绑定用户信息");
                //如果本地的SessionID已经绑定了用于就重新生成sessionId进行绑定
                if (FTUserConfig.get().currentSessionHasUser()) {
                    FTUserConfig.get().clearSessionId();
                }
                //初始化SessionId
                FTUserConfig.get().initSessionId();
                //绑定用户信息
                FTUserConfig.get().bindUserData(name, id, extras);
            }
        }
    }

    /**
     * 开启定，并且获取定位结果
     */
    public static void startLocation(String geoKey, SyncCallback syncCallback) {
        if (!Utils.isNullOrEmpty(geoKey)) {
            LocationUtils.get().setGeoKey(geoKey);
            LocationUtils.get().setUseGeoKey(true);
        }
        LocationUtils.get().startLocationCallBack(syncCallback);
    }

    /**
     * 创建获取 GPU 信息的GLSurfaceView
     *
     * @param root
     */
    public void setGpuRenderer(ViewGroup root) {
        try {
            if (FTMonitorConfig.get().isMonitorType(MonitorType.GPU)) {
                LogUtils.d(TAG, "绑定视图监听 GPU 信息");
                Context context = getApplication();
                final RendererUtil mRendererUtil = new RendererUtil();
                GLSurfaceView mGLSurfaceView = new GLSurfaceView(context);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(1, 1);
                mGLSurfaceView.setLayoutParams(layoutParams);
                root.addView(mGLSurfaceView);
                mGLSurfaceView.setEGLContextClientVersion(1);
                mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
                mGLSurfaceView.setRenderer(mRendererUtil);
                mGLSurfaceView.post(() -> {
                    String gl_vendor = mRendererUtil.gl_vendor;
                    String gl_renderer = mRendererUtil.gl_renderer;
                    GpuUtils.GPU_VENDOR_RENDERER = gl_vendor + "_" + gl_renderer;
                    if (gl_renderer != null && gl_vendor != null) {
                        mGLSurfaceView.surfaceDestroyed(mGLSurfaceView.getHolder());
                    }
                });
            }
        } catch (Exception e) {
        }
    }

    /**
     * 初始化SDK本地配置数据
     */
    private void initFTConfig() {
        if (mFtSDKConfig != null) {
            LogUtils.setDebug(mFtSDKConfig.isDebug());
            LogUtils.setDescLogShow(mFtSDKConfig.isDescLog());
            FTAliasConfig.get().initParams(mFtSDKConfig);
            FTHttpConfig.get().initParams(mFtSDKConfig);
            FTAutoTrackConfig.get().initParams(mFtSDKConfig);
            FTDBCachePolicy.get().initParam(mFtSDKConfig);
            FTUserConfig.get().setNeedBindUser(mFtSDKConfig.isNeedBindUser());
            FTUserConfig.get().initSessionId();
            if (mFtSDKConfig.isNeedBindUser()) {
                FTUserConfig.get().initUserDataFromDB();
            }
            FTNetworkListener.get().monitor();
            if (mFtSDKConfig.isAutoTrack()) {
                trackStartApp();
            }
            if (mFtSDKConfig.isEventFlowLog()) {
                FTFlowConfig.get().initParams(mFtSDKConfig);
            }
            float rate = mFtSDKConfig.getTraceSamplingRate();
            if (rate > 1 || rate < 0) {
                throw new IllegalArgumentException("rate 值的范围应在[0,1]");
            }
            //设置采样率
            Utils.traceSamplingRate = rate;
            FTExceptionHandler.get().initParams(mFtSDKConfig);
            FTMonitorConfig.get().initParams(mFtSDKConfig);
            FTUICatonManager.getInstance().startMonitor();
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

    private void trackStartApp() {
        FTAutoTrack.startApp();
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
