package com.ft;


import android.content.Context;
import android.os.Looper;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.application.MockApplication;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.FTDBCachePolicy;
import com.ft.sdk.garble.bean.OP;
import com.ft.sdk.garble.bean.RecordData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.manager.SyncTaskManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.ft.TestEntrance.hasPrepare;

/**
 * author: huangDianHua
 * time: 2020/8/26 15:21:02
 * description:控制台日志测试用例、丢弃策略测试
 */
@RunWith(AndroidJUnit4.class)
public class LogTest extends BaseTest{
    Context context;
    FTSDKConfig ftsdkConfig;

    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        SyncTaskManager.get().setRunning(true);
        context = MockApplication.getContext();
        ftsdkConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET))
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN))
                .setTraceConsoleLog(true)
                .setEventFlowLog(true)
                .setEnableTrackAppCrash(true);
        FTSdk.install(ftsdkConfig);
    }

    @Test
    public void consoleLogTest() throws InterruptedException {
        Log.d("TestLog", "控制台日志测试用例qaws");
        Thread.sleep(4000);
        List<RecordData> recordDataList = FTDBManager.get().queryDataByDescLimitLog(10);
        int except = 0;
        if (recordDataList != null) {
            for (RecordData data : recordDataList) {
                if (data.getOpdata().contains("控制台日志测试用例qaws")) {
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
        List<RecordData> dataList = FTDBManager.get().queryDataByDescLimitLog(0);
        int count = 0;
        for (RecordData recordData : dataList) {
            if(recordData.getOpdata().contains("控制台日志测试用例")){
                count++;
            }
        }
        System.out.println("count="+count);
        //Thread.sleep(300000);
        Assert.assertTrue(10>=count);
    }
}
