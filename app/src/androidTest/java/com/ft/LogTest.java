package com.ft;


import android.content.Context;
import android.os.Looper;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.manager.SyncTaskManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * author: huangDianHua
 * time: 2020/8/26 15:21:02
 * description:
 */
@RunWith(AndroidJUnit4.class)
public class LogTest {
    Context context;
    static boolean hasPrepare;
    FTSDKConfig ftsdkConfig;

    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        context = DemoApplication.getContext();
        ftsdkConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET))
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN))
                .setTraceConsoleLog(true)
                .setEventFlowLog(true)
                .setEnableTrackAppCrash(true);
    }

    @Test
    public void consoleLogTest() throws InterruptedException {
        FTSdk.install(ftsdkConfig);
        FTDBManager.get().delete();
        SyncTaskManager.get().setRunning(true);
        Log.d("TestLog", "控制台日志测试用例");
        Thread.sleep(4000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimitLog(10);
        int length = recordDataList.size();
        Assert.assertEquals(1, length);

    }

    /**
     * 测试大批量插入数据，是否触发丢弃策略
     *
     * @throws InterruptedException
     */
    @Test
    public void triggerPolicyTest() throws InterruptedException {
        SyncTaskManager.get().setRunning(true);
        FTSdk.install(ftsdkConfig);
        FTDBManager.get().delete();
        for (int i = 0; i < 5030; i++) {
            Log.d("TestLog", i + "-控制台日志测试用例");
            Thread.sleep(10);
        }
        Thread.sleep(40000);
        int size = FTDBManager.get().queryTotalCount(OP.LOG);
        Assert.assertTrue(5020 >= size && size >= 5000);

    }
}
