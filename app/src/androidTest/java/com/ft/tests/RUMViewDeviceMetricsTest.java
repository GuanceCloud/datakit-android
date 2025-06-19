package com.ft.tests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static com.ft.AllTests.hasPrepare;

import android.os.Looper;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.BaseTest;
import com.ft.BuildConfig;
import com.ft.DebugMainActivity;
import com.ft.R;
import com.ft.sdk.DeviceMetricsMonitorType;
import com.ft.sdk.EnvType;
import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.Constants;
import com.ft.test.utils.LineProtocolData;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * View 内 fps cpu memory battery 数据监测
 *
 * @author Brandon
 */
@RunWith(AndroidJUnit4.class)
public class RUMViewDeviceMetricsTest extends BaseTest {

    @Rule
    public ActivityScenarioRule<DebugMainActivity> rule = new ActivityScenarioRule<>(DebugMainActivity.class);


    @BeforeClass
    public static void settingBeforeLaunch() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }

        stopSyncTask();

        FTSDKConfig ftSDKConfig = FTSDKConfig
                .builder(BuildConfig.DATAKIT_URL)
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.GRAY);
        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setRumAppId(BuildConfig.RUM_APP_ID)
                .setDeviceMetricsMonitorType(DeviceMetricsMonitorType.ALL.getValue())
                .setEnableTraceUserView(true)
        );

        FTAutoTrack.startApp(null);

    }


    /**
     * 检验应用真实启动过程中，是否真确显示
     * {@link Constants#KEY_CPU_TICK_COUNT_PER_SECOND}
     * {@link Constants#KEY_CPU_TICK_COUNT}
     * {@link Constants#KEY_MEMORY_MAX}
     * {@link Constants#KEY_MEMORY_AVG}
     * {@link Constants#KEY_BATTERY_CURRENT_MAX}
     * {@link Constants#KEY_BATTERY_CURRENT_AVG}
     * 等指标数值
     * <p>
     * 目前
     * {@link Constants#KEY_FPS_MINI}
     * {@link Constants#KEY_FPS_AVG}
     * 无法通过测试用例检验，只能通过人为检验确认正确性
     *
     * @throws Exception
     */
    @Test
    public void viewDeviceMetricsTest() throws Exception {
        invokeGenerateRumData();
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.main_view_loop_test)).perform(ViewActions.scrollTo()).perform(click());
        Thread.sleep(2000);
        List<SyncData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0,
                DataType.RUM_APP);

        for (SyncData recordData : recordDataList) {
            LineProtocolData lineProtocolData = new LineProtocolData(recordData.getDataString());
            String measurement = lineProtocolData.getMeasurement();
            if (Constants.FT_MEASUREMENT_RUM_VIEW.equals(measurement)) {
                if (!lineProtocolData.getFieldAsBoolean(Constants.KEY_RUM_VIEW_IS_ACTIVE)) {
                    Assert.assertTrue(lineProtocolData.getFieldAsDouble(Constants.KEY_CPU_TICK_COUNT_PER_SECOND) > 0);
                    Assert.assertTrue(lineProtocolData.getFieldAsLong(Constants.KEY_CPU_TICK_COUNT) > 0);
                    Assert.assertTrue(lineProtocolData.getFieldAsLong(Constants.KEY_MEMORY_MAX) > 0);
                    Assert.assertTrue(lineProtocolData.getFieldAsLong(Constants.KEY_MEMORY_AVG) > 0);
                    Assert.assertTrue(lineProtocolData.getFieldAsLong(Constants.KEY_BATTERY_CURRENT_MAX) > 0);
                    Assert.assertTrue(lineProtocolData.getFieldAsLong(Constants.KEY_BATTERY_CURRENT_AVG) > 0);
                    break;
                }

            }
        }

    }
}
