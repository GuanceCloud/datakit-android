package com.ft.sdk.tests;

import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.sdk.DeviceMetricsMonitorType;
import com.ft.sdk.EnvType;
import com.ft.sdk.ErrorMonitorType;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.utils.Constants;
import com.ft.test.base.FTBaseTest;
import com.ft.test.utils.CheckUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * author: huangDianHua
 * time: 2020/8/26 10:32:36
 * description:监控类数据测试
 */
@RunWith(AndroidJUnit4.class)
public class MonitorConfigTest extends FTBaseTest {

    @Before
    public void setUp() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        stopSyncTask();
        FTSDKConfig ftSDKConfig = FTSDKConfig
                .builder(TEST_FAKE_URL)
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.GRAY);
        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setRumAppId(TEST_FAKE_RUM_ID)
                .setExtraMonitorTypeWithError(ErrorMonitorType.ALL.getValue())
                .setDeviceMetricsMonitorType(DeviceMetricsMonitorType.ALL.getValue())
        );
    }

    /**
     * {@link FTRUMGlobalManager#addError(String, String, ErrorType, AppState)} 过程中检测是否正确
     * 附带 {@link Constants#KEY_BATTERY_USE}
     * {@link Constants#KEY_MEMORY_TOTAL}
     * {@link Constants#KEY_MEMORY_USE}
     * {@link Constants#KEY_CPU_USE}
     *
     * @throws Exception
     */
    @Test
    public void monitorErrorTest() throws Exception {
        FTRUMGlobalManager.get().addError("log", "message", ErrorType.JAVA, AppState.RUN);

        Thread.sleep(2000);
        Assert.assertTrue(CheckUtils.checkValueInLineProtocol(DataType.RUM_APP, new String[]{
                Constants.KEY_BATTERY_USE,
                Constants.KEY_MEMORY_TOTAL,
                Constants.KEY_MEMORY_USE,
                Constants.KEY_CPU_USE
        }));

    }
///**
// * 目前监控数值无法通过测试用例的方式来检验正确性，只能通过人为查看进行验证
// */
//
//
//    @Test
//    public void monitorDeviceMetrics() throws InterruptedException {
//        FTRUMGlobalManager.get().startView(ANY_VIEW);
//        Thread.sleep(1500);
//        FTRUMGlobalManager.get().stopView();
//        waitForInThreadPool();
//        Thread.sleep(3000L);
//        Assert.assertTrue(CheckUtils.checkValue(DataType.RUM_APP, new String[]{
//                Constants.KEY_FPS_AVG,
//                Constants.KEY_FPS_MINI,
//                Constants.KEY_MEMORY_AVG,
//                Constants.KEY_MEMORY_MAX,
//                Constants.KEY_CPU_TICK_COUNT,
//                Constants.KEY_CPU_TICK_COUNT_PER_SECOND,
//                Constants.KEY_FPS_MINI,
//                Constants.KEY_FPS_AVG,
//        }, 0));
//    }


}

