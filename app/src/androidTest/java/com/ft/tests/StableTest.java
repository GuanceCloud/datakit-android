package com.ft.tests;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static com.ft.AllTests.hasPrepare;

import android.content.Context;
import android.os.Looper;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ft.BaseTest;
import com.ft.BuildConfig;
import com.ft.DebugMainActivity;
import com.ft.R;
import com.ft.sdk.DeviceMetricsMonitorType;
import com.ft.sdk.EnvType;
import com.ft.sdk.ErrorMonitorType;
import com.ft.sdk.FTAutoTrack;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.bean.UserData;
import com.ft.sdk.garble.utils.LogUtils;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(AndroidJUnit4.class)
public class StableTest extends BaseTest {

    @Rule
    public ActivityScenarioRule<DebugMainActivity> rule = new ActivityScenarioRule<>(DebugMainActivity.class);

    @BeforeClass
    public static void settingBeforeLaunch() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }

        Context application = InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        LogUtils.registerInnerLogCacheToFile(new File(application.getFilesDir(), "InnerLog" + ".log"));


        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(BuildConfig.DATAKIT_URL)
                .setDebug(true)//设置是否是 debug
                .setAutoSync(true)
                .setCustomSyncPageSize(100)
                .setEnv(EnvType.valueOf(BuildConfig.ENV.toUpperCase()));
        FTSdk.install(ftSDKConfig);

        FTSdk.initLogWithConfig(new FTLoggerConfig()
                .setSamplingRate(1f)
                .setEnableCustomLog(true)
                .setEnableConsoleLog(true)
                .setLogCacheLimitCount(10000)
                .setPrintCustomLogToConsole(true)
                .setEnableLinkRumData(true)
        );

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setSamplingRate(1f)
                .setRumAppId(BuildConfig.RUM_APP_ID)
                .setEnableTraceUserAction(true)
                .setEnableTraceUserView(true)
                .setEnableTraceUserResource(true)
                .setEnableTrackAppANR(true)
                .setEnableTrackAppCrash(true)
                .setEnableTrackAppUIBlock(true)
                .setDeviceMetricsMonitorType(DeviceMetricsMonitorType.ALL.getValue())
                .setExtraMonitorTypeWithError(ErrorMonitorType.ALL.getValue()));


        UserData userData = new UserData();
        userData.setName("brandon");
        userData.setId("brandon.test.userid");
        FTSdk.bindRumUserData(userData);

        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setSamplingRate(1f)
                .setEnableAutoTrace(true)
                .setEnableLinkRUMData(true)
                .setTraceType(TraceType.DDTRACE));

        FTAutoTrack.startApp(null);

    }

    /**
     * 一小时左右高负载数据
     *
     * @throws Exception
     */
    @Test
    public void oneHourHighLoad() throws Exception {
        highLoadData(9000);
        waitEventConsumeInThreadPool();
    }

    /**
     * 高负载过程中发生 ANR
     *
     * @throws Exception
     */
    @Test
    public void highLoadWithANR() throws Exception {
        highLoadData(40);
        onView(withId(R.id.main_mock_ui_block_btn)).perform(ViewActions.scrollTo()).perform(click());
        waitEventConsumeInThreadPool();

    }

    /**
     * 高负载过程中发生 java crash
     *
     * @throws Exception
     */
    @Test
    public void highLoadWithCrash() throws Exception {
        //阻止 application 崩溃，如果崩溃测试用例也会结束
        highLoadData(40);
        onView(withId(R.id.main_mock_crash_btn)).perform(ViewActions.scrollTo()).perform(click());
        waitEventConsumeInThreadPool();
    }

    /**
     * 高负载过程中发生 native crash
     *
     * @throws Exception
     */
    @Test
    public void highLoadWIthNativeCrash() throws Exception {
        highLoadData(40);
        onView(withId(R.id.main_mock_crash_native_btn)).perform(ViewActions.scrollTo()).perform(click());
        waitEventConsumeInThreadPool();

    }

    /**
     * 高负载
     *
     * @throws Exception
     */
    private void highLoadData(int dataCount) throws Exception {
        onView(withId(R.id.main_high_load_btn)).perform(ViewActions.scrollTo()).perform(click());
        onView(withId(R.id.high_load_log_btn)).perform(click());
        onView(withId(R.id.high_load_http_request_btn)).perform(click());
        for (int i = 0; i < dataCount; i++) {
            onView(withId(R.id.high_load_to_repeat_view_btn)).perform(click());
            Thread.sleep(200);
            onView(withId(android.R.id.content)).perform(pressBack());
        }
        onView(withId(android.R.id.content)).perform(pressBack());
    }

    @Override
    public void tearDown() {
    }
}
