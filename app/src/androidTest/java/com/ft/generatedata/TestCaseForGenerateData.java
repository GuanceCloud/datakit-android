package com.ft.generatedata;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;

import static com.ft.AllTests.hasPrepare;

import android.content.Context;
import android.os.Looper;
import android.view.View;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.DebugMainActivity;
import com.ft.R;
import com.ft.application.MockApplication;
import com.ft.sdk.EnvType;
import com.ft.sdk.FTLoggerConfig;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TestCaseForGenerateData extends BaseTest {

    @Rule
    public ActivityScenarioRule<DebugMainActivity> rule = new ActivityScenarioRule<>(DebugMainActivity.class);

    @BeforeClass
    public static void setUp() {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        Context context = MockApplication.getContext();
        FTSDKConfig ftSDKConfig = FTSDKConfig
                .builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL))
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.GRAY);
        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setEnableTrackAppCrash(true)
                .setRumAppId(AccountUtils.getProperty(context, AccountUtils.RUM_APP_ID))
                .setEnableTrackAppUIBlock(true)
                .setEnableTraceUserView(true)
                .setEnableTraceUserAction(true)
        );

        FTSdk.initLogWithConfig(new FTLoggerConfig()
                .setEnableCustomLog(true)
                .setEnableConsoleLog(true,"custom"));

        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setEnableAutoTrace(true)
                .setEnableLinkRUMData(true));

    }

    @Test
    public void generateData() throws InterruptedException {
        Thread.sleep(5000);//等待 emulator 开启 网络链接

        onView(ViewMatchers.withId(R.id.main_mock_ui_block_btn)).perform(ViewActions.scrollTo()).perform(click());

        Thread.sleep(1000);
        avoidCrash();
        onView(ViewMatchers.withId(R.id.main_mock_crash_btn)).perform(ViewActions.scrollTo()).perform(click());
        Thread.sleep(300);

        onView(ViewMatchers.withId(R.id.main_mock_log_btn)).perform(ViewActions.scrollTo()).perform(click());
        Thread.sleep(300);

        onView(ViewMatchers.withId(R.id.main_mock_okhttp_btn)).perform(ViewActions.scrollTo()).perform(click());

        Thread.sleep(300);

        onView(ViewMatchers.withId(R.id.main_view_loop_test)).perform(ViewActions.scrollTo()).perform(click());
        Thread.sleep(300);

        onView(ViewMatchers.withId(R.id.first_to_second_btn)).perform(click());
        Thread.sleep(300);

        onView(ViewMatchers.withId(R.id.second_to_third_btn)).perform(click());
        Thread.sleep(300);

        onView(ViewMatchers.withId(R.id.third_to_first_btn)).perform(click());

        Thread.sleep(20000);//等待 后台同步数据
    }


}
