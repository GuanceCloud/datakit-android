package com.ft;

import android.content.Context;
import android.location.Address;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.sdk.FTAutoTrackType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.MonitorType;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.SyncCallback;
import com.ft.sdk.garble.manager.SyncTaskManager;
import com.ft.sdk.garble.utils.LocationUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

/**
 * author: huangDianHua
 * time: 2020/8/27 11:29:04
 * description:测试定位
 */
@RunWith(AndroidJUnit4.class)
public class LocationTest {
    Context context;
    static boolean hasPrepare;
    FTSDKConfig ftSDKConfig;

    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        context = DemoApplication.getContext();
        ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET))
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN))
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setUseOAID(true)//设置 OAID 是否可用
                .setDebug(true)//设置是否是 debug
                .setDescLog(true)
                .enableAutoTrack(true)//设置是否开启自动埋点
                .setEnableAutoTrackType(FTAutoTrackType.APP_CLICK.type |
                        FTAutoTrackType.APP_END.type |
                        FTAutoTrackType.APP_START.type)//设置埋点事件类型的白名单
                .setGeoKey(true, AccountUtils.getProperty(context, AccountUtils.GEO_KEY))
                .setNeedBindUser(false)//是否需要绑定用户信息
                .setPageVtpDescEnabled(true)
                .setMonitorType(MonitorType.ALL)//设置监控项
                .trackNetRequestTime(true)
                .setEnableTrackAppCrash(true)
                .setEnv("dev")
                .setTraceSamplingRate(0.5f)
                .setNetworkTrace(true)
                .setTraceConsoleLog(true)
                .setEventFlowLog(true)
                .setTraceType(TraceType.SKYWALKING_V2)
                .setOnlySupportMainProcess(true);
        //关闭数据自动同步操作
        SyncTaskManager.get().setRunning(true);
    }
    Address address;
    @Test
    public void locationTest() throws InterruptedException {
        FTSdk.install(ftSDKConfig);
        Thread.sleep(2000);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        address = null;
        LocationUtils.get().startLocationCallBack(new SyncCallback() {
            @Override
            public void onResponse(int code, String response) {
                address = LocationUtils.get().getCity();
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        Assert.assertNotNull(address);
    }
}