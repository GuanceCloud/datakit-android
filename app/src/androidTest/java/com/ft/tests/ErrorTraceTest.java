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
 * description: UIBlock, ANR, nativeCrash need manual testing, test cases cannot simulate
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
                .setDebug(true)//Set whether it is debug
                .setEnv(EnvType.GRAY);
        //Close automatic data synchronization operation
        stopSyncTask();
        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setRumAppId(BuildConfig.RUM_APP_ID)
                .setEnableTrackAppCrash(true));
    }

    /**
     * Simulate crash, check if crash information is recorded in the database
     *
     * @throws InterruptedException
     */
    @Test
    public void mockExceptionTest() throws InterruptedException {

        //Prevent application crash, if it crashes the test case will also end
        avoidCrash();
        //Generate a crash message
        onView(ViewMatchers.withId(R.id.main_mock_crash_btn)).perform(ViewActions.scrollTo()).perform(click());
        //Because data insertion is an asynchronous operation, an interval needs to be set to be able to query the data
        Thread.sleep(1000);
        Assert.assertTrue(CheckUtils.checkValueInLineProtocol(DataType.RUM_APP, "ArithmeticException"));
    }


}
