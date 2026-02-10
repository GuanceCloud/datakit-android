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
import com.ft.NetworkTestActivity;
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
import java.util.Objects;

/**
 * Resource request data monitoring
 *
 * @author Brandon
 */
@RunWith(AndroidJUnit4.class)
public class RUMResourceTest extends BaseTest {

    @Rule
    public ActivityScenarioRule<NetworkTestActivity> rule = new ActivityScenarioRule<>(NetworkTestActivity.class);


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
                .setEnableTraceUserAction(true)
                .setEnableTraceUserResource(true)
        );

    }

    /**
     * Verify if OkHttp ft-plugin correctly weaves Interceptor related code
     *
     * @throws Exception
     */
    @Test
    public void resourceInterceptorTest() throws Exception {
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.btn_normal_request)).perform(click());
        invokeCheckActionClose();

        Thread.sleep(2000);

        List<SyncData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        for (SyncData recordData : recordDataList) {
            LineProtocolData lineProtocolData = new LineProtocolData(recordData.getDataString());
            String measurement = lineProtocolData.getMeasurement();
            if ("action".equals(measurement)) {
                if (Objects.equals(lineProtocolData.getTagAsString("action_type"), "click")) {
                    int resourceCount = lineProtocolData.getFieldAsInt("action_resource_count");
                    Assert.assertEquals(1, resourceCount);
                    break;
                }
            }

        }
    }


}
