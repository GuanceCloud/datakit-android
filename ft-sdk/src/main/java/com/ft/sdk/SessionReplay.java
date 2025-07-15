package com.ft.sdk;


import android.content.Context;

import com.ft.sdk.feature.FeatureSdkCore;
import com.ft.sdk.garble.utils.LogUtils;
import com.ft.sdk.garble.utils.VersionUtils;
import com.ft.sdk.sessionreplay.BuildConfig;
import com.ft.sdk.sessionreplay.FTSessionReplayConfig;
import com.ft.sdk.sessionreplay.SessionReplayFeature;

public class SessionReplay {
    private static final String TAG = "SessionReplay";

    /**
     * Enables a SessionReplay feature based on the configuration provided.
     *
     * @param ftSessionReplayConfig Configuration to use for the feature.
     */
    public static void enable(
            FTSessionReplayConfig ftSessionReplayConfig, Context context
    ) {
        FeatureSdkCore featureSdkCore = SessionReplayManager.get();
        if (!VersionUtils.firstVerGreaterEqual(BuildConfig.VERSION_NAME, "0.1.2-alpha01")) {
            featureSdkCore.getInternalLogger().e(TAG, "need install more than ft-session-replay:0.1.2-alpha01");
            return;
        }
        LogUtils.d(TAG, "init SR:" + ftSessionReplayConfig);
        featureSdkCore.init(context);
        SessionReplayFeature sessionReplayFeature = new SessionReplayFeature(
                featureSdkCore,
                ftSessionReplayConfig
        );

        featureSdkCore.registerFeature(sessionReplayFeature);
        
        // 自动配置Flutter UI数据模式 - 添加by zzq
        // 检测是否为Flutter环境并自动设置
        configureFlutterModeIfNeeded(sessionReplayFeature);
    }
    
    /**
     * 自动检测并配置Flutter模式 - 添加by zzq
     * 如果检测到Flutter环境，自动设置为使用Flutter UI数据
     */
    private static void configureFlutterModeIfNeeded(SessionReplayFeature sessionReplayFeature) {
        try {
            // 检测Flutter环境的方法：
            // 1. 检查是否存在Flutter相关的类
            // 2. 检查系统属性或其他标识
            boolean isFlutterEnvironment = isFlutterEnvironment();
            
            if (isFlutterEnvironment) {
                LogUtils.d(TAG, "Flutter environment detected, enabling Flutter UI data mode");
                sessionReplayFeature.setUseFlutterUIData(true);
            } else {
                LogUtils.d(TAG, "Native Android environment detected, using standard recording mode");
            }
        } catch (Exception e) {
            LogUtils.w(TAG, "Error detecting Flutter environment: " + e.getMessage());
            // 发生错误时默认不启用Flutter模式，保持向后兼容
        }
    }
    
    /**
     * 检测是否为Flutter环境 - 添加by zzq
     * @return true if Flutter environment is detected
     */
    private static boolean isFlutterEnvironment() {
        try {
            // 方法1: 检查Flutter相关类是否存在
            Class.forName("io.flutter.embedding.engine.FlutterEngine");
            return true;
        } catch (ClassNotFoundException e) {
            // Flutter类不存在，继续其他检测方法
        }
        
        try {
            // 方法2: 检查Flutter插件相关类
            Class.forName("io.flutter.plugin.common.MethodChannel");
            return true;
        } catch (ClassNotFoundException e) {
            // Flutter插件类不存在
        }
        
        // 方法3: 可以添加其他检测方法，比如检查包名、manifest等
        // 暂时返回false，表示非Flutter环境
        return false;
    }
}
