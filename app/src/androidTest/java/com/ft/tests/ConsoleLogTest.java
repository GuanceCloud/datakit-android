package com.ft.tests;


import static com.ft.AllTests.hasPrepare;

import android.os.Looper;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.BaseTest;
import com.ft.BuildConfig;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.Status;
import com.ft.test.utils.CheckUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * author: huangDianHua
 * time: 2020/8/26 15:21:02
 * description:控制台日志测试用例、丢弃策略测试
 *
 * ft-plugin:1.2.0 以上版本暂不可用
 *
 * 目前适配 AGP 8.0，Gradle 8.0 之后，发现在 AndroidTest build 过程中 ASM 已经织入，但是在运行时不会进行 hook
 *
 */
@RunWith(AndroidJUnit4.class)
public class ConsoleLogTest extends BaseTest {

    @Before
    public void setUp() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        stopSyncTask();
        FTSDKConfig ftsdkConfig = FTSDKConfig
                .builder(BuildConfig.DATAKIT_URL);
        FTSdk.install(ftsdkConfig);

    }

    /**
     * console 数据测试
     * @throws Exception
     */
    @Test
    public void consoleLogTest() throws Exception {
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableConsoleLog(true));
        Log.d("TestLog", "控制台日志测试用例qaws");
        Thread.sleep(300);
        Assert.assertTrue(CheckUtils.checkValueInLineProtocol(DataType.LOG, "控制台日志测试用例qaws"));

    }

    /**
     * console prefix 过滤测试
     * @throws Exception
     */
    @Test
    public void consoleLogPrefixTest() throws Exception {
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableConsoleLog(true, "debug"));

        String logContent = "logTest";
        Log.d("TestLog", logContent);
        Thread.sleep(1000);
        Assert.assertFalse(CheckUtils.checkValueInLineProtocol(DataType.LOG, logContent));

        logContent = "debug log test";
        Log.d("TestLog", logContent);
        Thread.sleep(1000);
        Assert.assertTrue(CheckUtils.checkValueInLineProtocol(DataType.LOG, logContent));


    }

    /**
     * console {@link Status} 等级测试
     *
     * @throws Exception
     */
    @Test
    public void consoleLogLevelTest() throws Exception {
        FTSdk.initLogWithConfig(new FTLoggerConfig()
                .setEnableConsoleLog(true)
                .setLogLevelFilters(new Status[]{Status.ERROR}));

        String logContent = "logTest";
        Log.d("TestLog", logContent);

        Thread.sleep(1000);
        int except = CheckUtils.getCount(DataType.LOG, logContent, 10);
        Assert.assertEquals(0, except);

        Log.e("TestLog", logContent);

        Thread.sleep(1000);
        except = CheckUtils.getCount(DataType.LOG, logContent, 10);
        Assert.assertEquals(1, except);


    }
}
