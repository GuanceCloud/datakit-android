package com.ft.tests;

import android.content.Context;
import android.os.Looper;

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
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static com.ft.AllTests.hasPrepare;

@RunWith(AndroidJUnit4.class)
public class RUMActionSumTest extends BaseTest {

    @Rule
    public ActivityScenarioRule<DebugMainActivity> rule = new ActivityScenarioRule<>(DebugMainActivity.class);


    @BeforeClass
    public static void settingBeforeLaunch() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }

        stopSyncTask();

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
                .setEnableTraceUserAction(true)
        );

    }

    @Test
    public void actionSumTest() throws Exception {
        Thread.sleep(1000);
        avoidCrash();
        onView(ViewMatchers.withId(R.id.main_mock_okhttp_btn)).perform(ViewActions.scrollTo()).perform(click());

        syncActionCount();

        Thread.sleep(5000);

        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        for (SyncJsonData recordData : recordDataList) {
            try {
                JSONObject json = new JSONObject(recordData.getDataString());
                JSONObject fields = json.optJSONObject("fields");
                String measurement = json.optString("measurement");
                if ("action".equals(measurement)) {
                    if (fields != null) {
                        int resourceCount = fields.optInt("action_resource_count");
                        Assert.assertEquals(1, resourceCount);
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        onView(ViewMatchers.withId(R.id.main_mock_crash_btn)).perform(ViewActions.scrollTo()).perform(click());
        syncActionCount();

        Thread.sleep(5000);

        recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);
        for (SyncJsonData recordData : recordDataList) {
            try {
                JSONObject json = new JSONObject(recordData.getDataString());
                JSONObject fields = json.optJSONObject("fields");
                String measurement = json.optString("measurement");
                if ("action".equals(measurement)) {
                    if (fields != null) {
                        int error_count = fields.optInt("action_error_count");
                        Assert.assertEquals(1, error_count);
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void syncActionCount() throws Exception {
        Thread.sleep(100);
        Whitebox.invokeMethod(FTRUMGlobalManager.get(), "generateRumData");
    }

}