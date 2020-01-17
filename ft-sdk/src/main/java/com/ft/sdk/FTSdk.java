package com.ft.sdk;

import android.app.Application;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.ft.sdk.garble.FTActivityLifecycleCallbacks;
import com.ft.sdk.garble.FTAutoTrackConfig;
import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.FTMonitorConfig;
import com.ft.sdk.garble.FTUserConfig;
import com.ft.sdk.garble.utils.GpuUtils;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.RendererUtil;

import org.json.JSONObject;

import java.security.InvalidParameterException;


/**
 * BY huangDianHua
 * DATE:2019-11-29 17:15
 * Description:
 */
public class FTSdk {
    private static FTSdk FTSDK;
    private FTSDKConfig mFtSDKConfig;
    private FTSdk(FTSDKConfig ftSDKConfig){
        FTActivityLifecycleCallbacks life = new FTActivityLifecycleCallbacks();
        Application app = FTApplication.getApplication();
        app.registerActivityLifecycleCallbacks(life);
        this.mFtSDKConfig = ftSDKConfig;
        initFTConfig();
        trackStartApp();
    }

    /**
     * SDK 配置项入口
     * @param ftSDKConfig
     * @return
     */
    public static synchronized FTSdk install(FTSDKConfig ftSDKConfig){
        if (FTSDK == null) {
            FTSDK = new FTSdk(ftSDKConfig);
        }
        return FTSDK;
    }

    /**
     * SDK 初始化后，获得 SDK 对象
     * @return
     */
    public static synchronized FTSdk get(){
        if(FTSDK == null){
            throw new InvalidParameterException("请先安装SDK(在应用启动时调用FTSdk.install(FTSDKConfig ftSdkConfig))");
        }
        return FTSDK;
    }

    /**
     * 注销用户信息
     */
    public void unbindUserData(){
        if(mFtSDKConfig != null){
            if (mFtSDKConfig.isNeedBindUser()) {
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
     * @param name
     * @param id
     * @param exts
     */
    public void bindUserData(@NonNull String name,@NonNull String id, JSONObject exts){
        if(mFtSDKConfig != null){
            if(mFtSDKConfig.isNeedBindUser()){
                //如果本地的SessionID已经绑定了用于就重新生成sessionId进行绑定
                if(FTUserConfig.get().currentSessionHasUser()){
                    FTUserConfig.get().clearSessionId();
                }
                //初始化SessionId
                FTUserConfig.get().initSessionId();
                //绑定用户信息
                FTUserConfig.get().bindUserData(name,id,exts);
            }
        }
    }

    /**
     * 创建获取 GPU 信息的GLSurfaceView
     * @param root
     */
    public void setGpuRenderer(ViewGroup root){
        Context context =FTApplication.getApplication();
        final RendererUtil mRendererUtil = new RendererUtil();
        GLSurfaceView mGLSurfaceView = new GLSurfaceView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(1,1);
        mGLSurfaceView.setLayoutParams(layoutParams);
        root.addView(mGLSurfaceView);
        mGLSurfaceView.setEGLContextClientVersion(1);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        mGLSurfaceView.setRenderer(mRendererUtil);
        mGLSurfaceView.post(() -> {
            String gl_vendor = mRendererUtil.gl_vendor;
            String gl_renderer = mRendererUtil.gl_renderer;
            LogUtils.d("gl_vendor = "+gl_vendor+" ,gl_renderer = "+gl_renderer);
            GpuUtils.GPU_VENDOR_RENDERER = gl_vendor+"_"+gl_renderer;
            if(gl_renderer != null && gl_vendor != null){
                mGLSurfaceView.surfaceDestroyed(mGLSurfaceView.getHolder());
            }
        });
    }

    /**
     * 初始化SDK本地配置数据
     */
    private void initFTConfig(){
        if(mFtSDKConfig != null) {
            LogUtils.setDebug(mFtSDKConfig.isDebug());
            FTHttpConfig.get().initParams(mFtSDKConfig);
            FTAutoTrackConfig.get().initParams(mFtSDKConfig);
            FTMonitorConfig.get().initParams(mFtSDKConfig);
            FTUserConfig.get().setNeedBindUser(mFtSDKConfig.isNeedBindUser());
            if(mFtSDKConfig.isNeedBindUser()){
                FTUserConfig.get().initSessionId();
                FTUserConfig.get().initUserDataFromDB();
            }
        }
    }

    private void trackStartApp(){
        FTAutoTrack.startApp();
    }



}
