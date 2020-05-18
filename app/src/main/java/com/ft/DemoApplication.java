package com.ft;

import android.app.Application;
import android.content.Context;

import com.ft.sdk.FTAutoTrackType;
import com.ft.sdk.FTMonitor;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.MonitorType;

import java.util.HashMap;
import java.util.Map;

/**
 * BY huangDianHua
 * DATE:2019-12-02 15:15
 * Description:
 */
public class DemoApplication extends Application {
    private static Context instance;

    public static Context getContext() {
        return instance;
    }

    public DemoApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(this, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(this, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(this, AccountUtils.ACCESS_KEY_SECRET))
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setUseOAID(true)//设置 OAID 是否可用
                .setDebug(true)//设置是否是 debug
                .setGeoKey(true, AccountUtils.getProperty(this, AccountUtils.GEO_KEY))
                .setNeedBindUser(false)//是否需要绑定用户信息
                .enableAutoTrack(true)//设置是否开启自动埋点
                .setEnableAutoTrackType(FTAutoTrackType.APP_CLICK.type |
                        FTAutoTrackType.APP_END.type |
                        FTAutoTrackType.APP_START.type)//设置埋点事件类型的白名单
                .addPageDesc(pageAliasMap())
                .addVtpDesc(eventAliasMap())
                .setOpenFlowChart(true)
                .setMonitorType(MonitorType.ALL)//设置监控项
                .setINetEngineClass(OkHttpEngine.class);
        FTSdk.install(ftSDKConfig);

        FTMonitor.get()
                .setMonitorType(MonitorType.ALL)
                .setPeriod(10)
                .start();
    }

    private Map<String,String> pageAliasMap(){
        Map<String,String> aliasMap =  new HashMap<String,String>();
        aliasMap.put("MainActivity","主页面");
        aliasMap.put("Tab1Fragment","子页面1");
        aliasMap.put("Main2Activity","第二个页面");
        return aliasMap;
    }

    private Map<String,String> eventAliasMap(){
        Map<String,String> aliasMap =  new HashMap<String,String>();
        aliasMap.put("ViewRootImpl/DecorView/LinearLayout/FrameLayout/ActionBarOverlayLayout/ContentFrameLayout/ScrollView/LinearLayout/AppCompatButton/#showKotlinActivity",
                "跳转到第二个页面");
        aliasMap.put("ViewRootImpl/DecorView/LinearLayout/FrameLayout/ActionBarOverlayLayout/ContentFrameLayout/ScrollView/LinearLayout/AppCompatButton/#btn_lam",
                "页面第一个按钮");
        aliasMap.put("ViewRootImpl/DecorView/LinearLayout/FrameLayout/ActionBarOverlayLayout/ContentFrameLayout/ScrollView/LinearLayout/AppCompatButton/#showDialog",
                "弹出弹框");
        return aliasMap;
    }
}
