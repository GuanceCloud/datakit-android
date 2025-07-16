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
 * description: Console log test case, discard policy test
 *
 * ft-plugin:1.2.0 and above are temporarily unavailable
 *
 * Currently adapted to AGP 8.0, after Gradle 8.0, it is found that ASM has been woven in
 * during the AndroidTest build process, but hook will not be performed at runtime
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
     * console data test
     * @throws Exception
     */
    @Test
    public void consoleLogTest() throws Exception {
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableConsoleLog(true));
        Log.d("TestLog", "Console log test case qaws");
        Thread.sleep(300);
        Assert.assertTrue(CheckUtils.checkValueInLineProtocol(DataType.LOG, "Console log test case qaws"));

    }

    /**
     * console prefix filter test
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
     * console {@link Status} level test
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
