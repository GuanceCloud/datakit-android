package com.ft.sdk.tests;

import static com.ft.sdk.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.sdk.EnvType;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTMonitorConfigManager;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.Constants;
import com.ft.test.base.FTBaseTest;
import com.ft.test.utils.CheckUtils;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;


/**
 * author: huangDianHua
 * time: 2020/8/26 10:32:36
 * description:监控类数据测试
 */
@RunWith(AndroidJUnit4.class)
public class MonitorConfigTest extends FTBaseTest {

    @BeforeClass
    public static void setUp() throws Exception {
        stopSyncTask();
        FTSDKConfig ftSDKConfig = FTSDKConfig
                .builder(TEST_FAKE_URL)
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.GRAY);
        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setRumAppId(TEST_FAKE_RUM_ID)
                .setExtraMonitorTypeWithError(MonitorType.ALL));
    }

    @Test
    public void monitorTest() throws Exception {
        FTRUMGlobalManager.get().addError("log", "message", ErrorType.JAVA, AppState.RUN);

        Thread.sleep(3000);
        Assert.assertTrue(CheckUtils.checkValue(DataType.RUM_APP, new String[]{
                Constants.KEY_BATTERY_USE,
                Constants.KEY_MEMORY_TOTAL,
                Constants.KEY_MEMORY_USE,
                Constants.KEY_CPU_USE
        }, 0));

    }


}

