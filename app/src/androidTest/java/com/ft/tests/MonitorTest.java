package com.ft.tests;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.application.MockApplication;
import com.ft.sdk.FTMonitor;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.MonitorType;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.Constants;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;

import static com.ft.AllTests.hasPrepare;

/**
 * author: huangDianHua
 * time: 2020/8/26 10:32:36
 * description:监控类数据测试
 */
@RunWith(AndroidJUnit4.class)
public class MonitorTest extends BaseTest {
    Context context;
    FTSDKConfig ftSDKConfig;

    @Before
    public void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        FTDBManager.get().delete();
        context = MockApplication.getContext();
        ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL),
                true,
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_ID),
                AccountUtils.getProperty(context, AccountUtils.ACCESS_KEY_SECRET))
                .setDataWayToken(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_TOKEN))
                .setXDataKitUUID("ft-dataKit-uuid-001")
                .setMonitorType(MonitorType.ALL);//设置监控项

    }

    @Test
    public void monitorBatteryTest() {
        monitorTest(MonitorType.BATTERY);
    }

    @Test
    public void monitorMemoryTest() {
        monitorTest(MonitorType.MEMORY);
    }

    @Test
    public void monitorCPUTest() {
        monitorTest(MonitorType.CPU);
    }
//
//    @Test
//    public void monitorGPUTest() {
//        monitorTest(MonitorType.GPU);
//    }
//
//    @Test
//    public void monitorNetworkTest() {
//        monitorTest(MonitorType.NETWORK);
//    }

//    @Test
//    public void monitorCameraTest() {
//        monitorTest(MonitorType.CAMERA);
//    }

//    @Test
//    public void monitorLocationTest() {
//        monitorTest(MonitorType.LOCATION);
//    }

    @Test
    public void monitorBlueToothTest() {
        monitorTest(MonitorType.BLUETOOTH);
    }

    @Test
    public void monitorSystemTest() {
        monitorTest(MonitorType.SYSTEM);
    }

    @Test
    public void monitorFpsTest() {
        monitorTest(MonitorType.FPS);
    }

    /**
     * 测试监控周期切换是否正常，该测试用例需要观察控制台的输出日志《轮训监控上报数据成功》来观察
     *
     * @throws InterruptedException
     */
    @Test
    public void monitorPeriodTest() throws InterruptedException {
        FTSdk.install(ftSDKConfig);
        FTMonitor.get().setPeriod(2).start();
        Thread.sleep(80000);
        FTMonitor.get().setPeriod(4).start();
        Thread.sleep(80000);
    }

    private void monitorTest(int monitorType) {
        ftSDKConfig.setMonitorType(monitorType);
        FTSdk.install(ftSDKConfig);

        stopSyncTask();
        try {
            simpleTrackData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDescLimitTrack(1);
        String data = recordDataList.get(0).getDataString();
        judge(data, monitorType);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void judge(String content, int monitorType) {
        HashMap<Integer, String> expects = createMonitorMap();
        boolean containExpect = content.contains(expects.get(monitorType));
        for (String value : expects.values()) {
            if (!value.equals(expects.get(monitorType))) {
                boolean noContainOther = !content.contains(value);
                containExpect = containExpect && noContainOther;
            }
        }
        Assert.assertTrue(containExpect);
    }

    /**
     * 构建一个监控类型-期望值的 map
     */
    private HashMap<Integer, String> createMonitorMap() {
        HashMap<Integer, String> expects = new HashMap<>();
        expects.put(MonitorType.BATTERY, Constants.KEY_BATTERY_USE);
        expects.put(MonitorType.MEMORY, Constants.KEY_MEMORY_USE);
        expects.put(MonitorType.CPU, Constants.KEY_CPU_HZ);
//        expects.put(MonitorType.GPU, Constants.KEY_GPU_RATE);
//        expects.put(MonitorType.NETWORK, Constants.KEY_NETWORK_PROXY);
//        expects.put(MonitorType.CAMERA, "camera_back_px");
//        expects.put(MonitorType.LOCATION, Constants.KEY_LOCATION_GPS_OPEN);
        expects.put(MonitorType.BLUETOOTH, Constants.KEY_BT_OPEN);
        expects.put(MonitorType.SYSTEM, Constants.KEY_DEVICE_NAME);
        expects.put(MonitorType.FPS, Constants.KEY_FPS);
        return expects;
    }



}

