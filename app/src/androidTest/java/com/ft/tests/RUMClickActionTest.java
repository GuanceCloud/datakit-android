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
import com.ft.sdk.garble.bean.SyncData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.test.utils.LineProtocolData;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * RUM click event monitoring
 */
@RunWith(AndroidJUnit4.class)
public class RUMClickActionTest extends BaseTest {

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
                .setDebug(true)//Set whether it is debug
                .setEnv(EnvType.GRAY);
        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setEnableTrackAppCrash(true)
                .setRumAppId(BuildConfig.RUM_APP_ID)
                .setEnableTrackAppUIBlock(true)
                .setEnableTraceUserAction(true)
        );

    }


    /**
     * Simulate whether Action related data is normally generated after clicking the Button during actual application startup
     *
     * @throws Exception
     */
    @Test
    public void rumCLickTest() throws Exception {
        Thread.sleep(2000);

        onView(ViewMatchers.withId(R.id.main_mock_click_btn)).perform(ViewActions.scrollTo()).perform(click());

        invokeCheckActionClose();
        Thread.sleep(2000);

        List<SyncData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        String actionId = "";
        for (SyncData recordData : recordDataList) {
            LineProtocolData parser = new LineProtocolData(recordData.getDataString());
            String measurement = parser.getMeasurement();
            if ("action".equals(measurement)) {
                actionId = parser.getTagAsString("action_id");
            }
        }

        Assert.assertNotNull(actionId);
        Assert.assertFalse(actionId.isEmpty());
    }


}
