package com.ft.tests;


import android.content.Context;
import android.os.Looper;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.application.MockApplication;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTLoggerConfigManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.FTDBCachePolicy;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.ft.AllTests.hasPrepare;

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

        FTLoggerConfigManager.get().initWithConfig(new FTLoggerConfig().setEnableConsoleLog(true));
    }

    @Test
    public void consoleLogTest() throws InterruptedException {
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

    /**
     * 测试大批量插入数据，是否触发丢弃策略
     *
     * @throws InterruptedException
     */
    @Test
    public void triggerPolicyTest() throws InterruptedException {
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
