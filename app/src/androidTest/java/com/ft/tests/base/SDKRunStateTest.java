package com.ft.tests.base;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.application.MockApplication;
import com.ft.sdk.EnvType;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTLoggerConfigManager;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMConfigManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.FTTraceConfigManager;
import com.ft.sdk.MonitorType;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.FTMonitorConfigManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.ft.AllTests.hasPrepare;

/**
 * author: huangDianHua
 * time: 2020/8/25 13:42:48
 * description:
 */
@RunWith(AndroidJUnit4.class)
public class SDKRunStateTest extends BaseTest {
    Context context;

    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        context = MockApplication.getContext();
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL))
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setUseOAID(true)//设置 OAID 是否可用
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.GRAY);

        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setExtraMonitorTypeWithError(MonitorType.ALL)
                .setEnableTrackAppANR(true)
                .setEnableTrackAppCrash(true)
                .setEnableTrackAppUIBlock(true)
                .setEnableTraceUserAction(true)
                .setEnableTraceUserView(true)
        );

        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableConsoleLog(true));

        FTSdk.initTraceWithConfig(new FTTraceConfig().setTraceType(TraceType.ZIPKIN));
    }

    @Test
    public void monitorTypeTest() {
        Assert.assertTrue(FTMonitorConfigManager.get().isMonitorType(MonitorType.ALL));
    }

    @Test
    public void networkTrackTest() {
        Assert.assertTrue(FTTraceConfigManager.get().isEnableAutoTrace());
    }

    @Test
    public void trackConsoleLogTest() {
        Assert.assertTrue(FTLoggerConfigManager.get().getConfig().isEnableConsoleLog());
    }

    @Test
    public void traceUserActionTest() {
        Assert.assertTrue(FTRUMConfigManager.get().getConfig().isEnableTraceUserAction());
    }

    @Test
    public void traceUserViewTest() {
        Assert.assertTrue(FTRUMConfigManager.get().getConfig().isEnableTraceUserView());
    }


    @Test
    public void showdownMonitorTypeTest() {
        FTSdk.shutDown();
        Assert.assertFalse(FTMonitorConfigManager.get().isMonitorType(MonitorType.ALL));
    }

    @Test
    public void showdownNetworkTrackTest() {
        FTSdk.shutDown();
        Assert.assertFalse(FTTraceConfigManager.get().isEnableAutoTrace());
    }

    @Test
    public void showdownTrackConsoleLogTest() {
        FTSdk.shutDown();
        Assert.assertNull(FTLoggerConfigManager.get().getConfig());
    }

    @Test
    public void showdownEventFlowLogTest() {
        FTSdk.shutDown();
        Assert.assertNull(FTRUMConfigManager.get().getConfig());
    }
}
