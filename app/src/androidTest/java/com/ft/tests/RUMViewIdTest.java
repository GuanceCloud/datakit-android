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

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static com.ft.AllTests.hasPrepare;

@RunWith(AndroidJUnit4.class)
public class RUMViewIdTest extends BaseTest {

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
        FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL))
                .setDebug(true)//设置是否是 debug
                .setEnableTrackAppCrash(true)
                .setEnv(EnvType.GRAY)
                .setRumAppId(AccountUtils.getProperty(context, AccountUtils.RUM_APP_ID))
                .setEnableTrackAppUIBlock(true)
                .setEnableTraceUserAction(true);
        FTSdk.install(ftSDKConfig);

    }

    /**
     * view 统计统计 UIBlock 与 ANR 需要手动测试
     *
     * @throws InterruptedException
     */

    @Test
    public void viewGenerateTest() throws InterruptedException {
        Thread.sleep(2000);
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        String viewId = "";
        for (SyncJsonData recordData : recordDataList) {
            try {
                JSONObject json = new JSONObject(recordData.getDataString());
                JSONObject tags = json.optJSONObject("tags");
                String measurement = json.optString("measurement");
                if ("view".equals(measurement)) {
                    if (tags != null) {
                        viewId = tags.optString("view_id");
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Assert.assertFalse(viewId.isEmpty());

        onView(ViewMatchers.withId(R.id.main_view_loop_test)).perform(ViewActions.scrollTo()).perform(click());

        Thread.sleep(1000);

        String newViewId = "";

        recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        for (SyncJsonData recordData : recordDataList) {
            try {
                JSONObject json = new JSONObject(recordData.getDataString());
                JSONObject tags = json.optJSONObject("tags");
                String measurement = json.optString("measurement");
                if ("view".equals(measurement)) {
                    if (tags != null) {
                        newViewId = tags.optString("view_id");
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Assert.assertNotEquals(viewId, newViewId);

    }

}
