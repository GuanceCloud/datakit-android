package com.ft;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.sdk.FTAutoTrackType;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.MonitorType;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.FTAutoTrackConfig;
import com.ft.sdk.garble.FTExceptionHandler;
import com.ft.sdk.garble.FTFlowConfig;
import com.ft.sdk.garble.FTHttpConfig;
import com.ft.sdk.garble.FTMonitorConfig;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.ft.TestEntrance.hasPrepare;

/**
 * author: huangDianHua
 * time: 2020/8/25 13:42:48
 * description:
 */
@RunWith(AndroidJUnit4.class)
public class SDKRunStateTest {
    Context context;

    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        context = DemoApplication.getContext();
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET))
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN))
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setUseOAID(true)//设置 OAID 是否可用
                .setDebug(true)//设置是否是 debug
                .setDescLog(true)
                .setGeoKey(true, AccountUtils.getProperty(context, AccountUtils.GEO_KEY))
                .setNeedBindUser(false)//是否需要绑定用户信息
                .enableAutoTrack(true)//设置是否开启自动埋点
                .setEnableAutoTrackType(FTAutoTrackType.APP_CLICK.type)//设置埋点事件类型的白名单
                //.addPageDesc(pageAliasMap())
                //.addVtpDesc(eventAliasMap())
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
        FTSdk.install(ftSDKConfig);
    }

    @After
    public void end() {
        FTSdk.get().shutDown();
    }

    @Test
    public void enableAutoTrackTest() {
        Assert.assertTrue(FTAutoTrackConfig.get().isAutoTrack());
    }

    @Test
    public void enableAutoTrackTypeTest() {
        Assert.assertTrue(FTAutoTrackConfig.get().enableAutoTrackType(FTAutoTrackType.APP_CLICK));
    }

    @Test
    public void monitorTypeTest() {
        Assert.assertTrue(FTMonitorConfig.get().isMonitorType(MonitorType.ALL));
    }

    @Test
    public void networkTrackTest() {
        Assert.assertTrue(FTHttpConfig.get().networkTrace);
    }

    @Test
    public void trackConsoleLogTest() {
        Assert.assertTrue(FTExceptionHandler.get().isTrackConsoleLog());
    }

    @Test
    public void eventFlowLogTest() {
        Assert.assertTrue(FTFlowConfig.get().isEventFlowLog());
    }

    @Test
    public void showdownEnableAutoTrackTest() {
        FTSdk.get().shutDown();
        Assert.assertFalse(FTAutoTrackConfig.get().isAutoTrack());
    }

    @Test
    public void showdownEnableAutoTrackTypeTest() {
        FTSdk.get().shutDown();
        Assert.assertTrue(FTAutoTrackConfig.get().enableAutoTrackType(FTAutoTrackType.APP_CLICK));
    }

    @Test
    public void showdownMonitorTypeTest() {
        FTSdk.get().shutDown();
        Assert.assertFalse(FTMonitorConfig.get().isMonitorType(MonitorType.ALL));
    }

    @Test
    public void showdownNetworkTrackTest() {
        FTSdk.get().shutDown();
        Assert.assertFalse(FTHttpConfig.get().networkTrace);
    }

    @Test
    public void showdownTrackConsoleLogTest() {
        FTSdk.get().shutDown();
        Assert.assertFalse(FTExceptionHandler.get().isTrackConsoleLog());
    }

    @Test
    public void showdownEventFlowLogTest() {
        FTSdk.get().shutDown();
        Assert.assertFalse(FTFlowConfig.get().isEventFlowLog());
    }
}
