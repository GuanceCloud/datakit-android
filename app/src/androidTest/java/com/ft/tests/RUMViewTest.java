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
 * RUM View data monitoring
 */
@RunWith(AndroidJUnit4.class)
public class RUMViewTest extends BaseTest {

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
                .setDebug(true)//Set whether it's debug
                .setEnv(EnvType.GRAY);
        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setRumAppId(BuildConfig.RUM_APP_ID)
                .setEnableTraceUserView(true)
        );

        FTAutoTrack.startApp(null);

    }

    /**
     * Verify real application startup, page jumps, whether {@link com.ft.sdk.FTActivityLifecycleCallbacks} normally outputs View data
     *
     * @throws Exception
     */
    @Test
    public void viewGenerateTest() throws Exception {
        invokeGenerateRumData();
        Thread.sleep(2000);
        List<SyncData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0,
                DataType.RUM_APP);

        String viewId = "";
        for (SyncData recordData : recordDataList) {
            LineProtocolData data = new LineProtocolData(recordData.getDataString());
            String measurement = data.getMeasurement();
            if (Constants.FT_MEASUREMENT_RUM_VIEW.equals(measurement)) {
                if (data.getFieldAsBoolean(Constants.KEY_RUM_VIEW_IS_ACTIVE,true)) {
                    viewId = data.getTagAsString(Constants.KEY_RUM_VIEW_ID);
                    break;

                }
            }
        }
        Assert.assertFalse(viewId.isEmpty());

        onView(ViewMatchers.withId(R.id.main_view_loop_test)).perform(ViewActions.scrollTo()).perform(click());

        Thread.sleep(5000);

        String newViewId = "";

        recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        for (SyncData recordData : recordDataList) {
            LineProtocolData data = new LineProtocolData(recordData.getDataString());
            String measurement = data.getMeasurement();
            if (Constants.FT_MEASUREMENT_RUM_VIEW.equals(measurement)) {
                if (data.getFieldAsBoolean(Constants.KEY_RUM_VIEW_IS_ACTIVE,true)) {
                        newViewId = data.getTagAsString(Constants.KEY_RUM_VIEW_ID);
                        break;

                }
            }
        }

        Assert.assertNotEquals(viewId, newViewId);

    }
}
