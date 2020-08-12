package com.ft.sdk.garble.http;

import com.ft.sdk.garble.utils.LogUtils;

/**
 * create: by huangDianHua
 * time: 2020/4/21 17:42:36
 * description:
 */
public class EngineFactory {
    public static final String TAG = "EngineFactory";
    private static boolean trackNetTime;

    public static void setTrackNetTime(boolean trackNetTime) {
        EngineFactory.trackNetTime = trackNetTime;
    }

    public static INetEngine createEngine(){
        if(trackNetTime){
            try {
                try {
                    Class.forName("okhttp3.OkHttpClient");
                } catch (ClassNotFoundException e) {
                    LogUtils.e(TAG,"检测到你开启了网络请求时长监控，但是你没有依赖 okHttp。使用该功能请先在项目中依赖 okHttp");
                    return new NativeNetEngine();
                }
                return new OkHttpEngine();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new NativeNetEngine();
    }
}
