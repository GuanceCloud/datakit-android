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
import com.ft.sdk.EnvType;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
import com.ft.test.utils.CheckUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * author: huangDianHua
 * time: 2020/8/27 11:21:02
 * description: UIBlock ANR nativeCrash 需要手动测试，测试用例无法模拟
 */
@RunWith(AndroidJUnit4.class)
public class ErrorTraceTest extends BaseTest {
    @Rule
    public ActivityScenarioRule<DebugMainActivity> rule = new ActivityScenarioRule<>(DebugMainActivity.class);


    @Before
    public void setUp() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        FTSDKConfig ftSDKConfig = FTSDKConfig
                .builder(BuildConfig.DATAKIT_URL)
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.GRAY);
        //关闭数据自动同步操作
        stopSyncTask();
        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setRumAppId(BuildConfig.RUM_APP_ID)
                .setEnableTrackAppCrash(true));
    }

    /**
     * 模拟崩溃，查看崩溃信息是否记录到数据库中
     *
     * @throws InterruptedException
     */
    @Test
    public void mockExceptionTest() throws InterruptedException {

        //阻止 application 崩溃，如果崩溃测试用例也会结束
        avoidCrash();
        //产生一个崩溃信息
        onView(ViewMatchers.withId(R.id.main_mock_crash_btn)).perform(ViewActions.scrollTo()).perform(click());
        //因为插入数据为异步操作，所以要设置一个间隔，以便能够查询到数据
        Thread.sleep(1000);
        Assert.assertTrue(CheckUtils.checkValueInLineProtocol(DataType.RUM_APP, "ArithmeticException"));
    }


}
