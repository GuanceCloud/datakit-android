package com.ft;

import android.app.Application;
import android.content.Context;

import com.ft.sdk.FTAutoTrackType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;

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
                .setUseOAID(true)//设置 OAID 是否可用
                .setDebug(true)//设置是否是 debug
                .setNeedBindUser(true)//是否需要绑定用户信息
                .enableAutoTrack(true)//设置是否开启自动埋点
                .setEnableAutoTrackType(FTAutoTrackType.APP_CLICK.type |
                        FTAutoTrackType.APP_END.type |
                        FTAutoTrackType.APP_START.type)//设置埋点事件类型的白名单
                //.setWhiteActivityClasses(Arrays.asList(MainActivity.class, Main2Activity.class))//设置埋点页面的白名单
                //.setWhiteViewClasses(Arrays.asList(Button.class, RadioGroup.class))
                .setOpenFlowChart(true)
                .setFlowProduct("demo12");
        //.setMonitorType(MonitorType.ALL);//设置监控项
        FTSdk.install(ftSDKConfig, this);
    }
}
