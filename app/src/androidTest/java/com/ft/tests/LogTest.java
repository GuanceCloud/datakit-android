package com.ft.tests;


import static com.ft.AllTests.hasPrepare;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.application.MockApplication;
import com.ft.sdk.FTLogger;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.FTDBCachePolicy;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.Status;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * author: huangDianHua
 * time: 2020/8/26 15:21:02
 * description:控制台日志测试用例、丢弃策略测试
 */
@RunWith(AndroidJUnit4.class)
public class LogTest extends BaseTest {
    Context context;

    @Before
    public void setUp() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        stopSyncTask();
        context = MockApplication.getContext();
        FTSDKConfig ftsdkConfig = FTSDKConfig
                .builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL));
        FTSdk.install(ftsdkConfig);

    }

    @Test
    public void consoleLogTest() throws InterruptedException {
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableConsoleLog(true));

        Log.d("TestLog", "控制台日志测试用例qaws");
        Thread.sleep(4000);
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(10, DataType.LOG);
        int except = 0;
        if (recordDataList != null) {
            for (SyncJsonData data : recordDataList) {
                if (data.getDataString().contains("控制台日志测试用例qaws")) {
                    except++;
                }
            }
        }
        Assert.assertEquals(1, except);

    }

    @Test
    public void consoleLogPrefixTest() throws InterruptedException {
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableConsoleLog(true, "debug"));

        Log.d("TestLog", "log test");

        Thread.sleep(1000);
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(10, DataType.LOG);
        int except = 0;
        if (recordDataList != null) {
            for (SyncJsonData data : recordDataList) {
                if (data.getDataString().contains("log test")) {
                    except++;
                }
            }
        }
        Assert.assertEquals(0, except);

        Log.d("TestLog", "debug log test");
        Thread.sleep(1000);
        recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(10, DataType.LOG);
        except = 0;
        if (recordDataList != null) {
            for (SyncJsonData data : recordDataList) {
                if (data.getDataString().contains("debug log test")) {
                    except++;
                }
            }
        }
        Assert.assertEquals(1, except);


    }

    @Test
    public void consoleLogLevelTest() throws InterruptedException {
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableCustomLog(true)
                .setEnableConsoleLog(true)
                .setLogLevelFilters(new Status[]{Status.ERROR}));

        Log.d("TestLog", "log test");
        FTLogger.getInstance().logBackground("log test", Status.INFO);

        Thread.sleep(1000);
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(10, DataType.LOG);
        int except = 0;
        if (recordDataList != null) {
            for (SyncJsonData data : recordDataList) {
                if (data.getDataString().contains("log test")) {
                    except++;
                }
            }
        }
        Assert.assertEquals(0, except);

        Log.e("TestLog", "log test");
        FTLogger.getInstance().logBackground("log test", Status.ERROR);

        Thread.sleep(1000);
        recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(10, DataType.LOG);
        except = 0;
        if (recordDataList != null) {
            for (SyncJsonData data : recordDataList) {
                if (data.getDataString().contains("log test")) {
                    except++;
                }
            }
        }
        Assert.assertEquals(2, except);


    }

    /**
     * 测试大批量插入数据，是否触发丢弃策略
     *
     * @throws InterruptedException
     */
    @Test
    public void triggerPolicyTest() throws InterruptedException {
        FTSdk.initLogWithConfig(new FTLoggerConfig().setEnableConsoleLog(true));

        FTDBCachePolicy.get().optCount(4990);
        for (int i = 0; i < 20; i++) {
            Log.d("TestLog", i + "-控制台日志测试用例");
            Thread.sleep(10);
        }
        Thread.sleep(2000);
        List<SyncJsonData> dataList = FTDBManager.get().queryDataByDataByTypeLimit(0, DataType.LOG);
        int count = 0;
        for (SyncJsonData recordData : dataList) {
            if (recordData.getDataString().contains("控制台日志测试用例")) {
                count++;
            }
        }
        System.out.println("count=" + count);
        //Thread.sleep(300000);
        Assert.assertTrue(10 >= count);
    }
}
