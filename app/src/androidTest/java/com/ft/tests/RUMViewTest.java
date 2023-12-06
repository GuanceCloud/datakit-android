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
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * RUM View 数据监测
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
                .setDebug(true)//设置是否是 debug
                .setEnv(EnvType.GRAY);
        FTSdk.install(ftSDKConfig);

        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setRumAppId(BuildConfig.RUM_APP_ID)
                .setEnableTraceUserView(true)
        );

    }

    /**
     * 检验真实应用启动，页面跳动，{@link com.ft.sdk.FTActivityLifecycleCallbacks} 是否正常输出 View 数据
     * @throws Exception
     */
    @Test
    public void viewGenerateTest() throws Exception {
        invokeGenerateRumData();
        Thread.sleep(2000);
        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0,
                DataType.RUM_APP);

        String viewId = "";
        for (SyncJsonData recordData : recordDataList) {
            try {
                JSONObject json = new JSONObject(recordData.getDataString());
                JSONObject tags = json.optJSONObject("tags");
                JSONObject fields = json.optJSONObject("fields");
                String measurement = json.optString("measurement");
                if (Constants.FT_MEASUREMENT_RUM_VIEW.equals(measurement)) {
                    if (fields != null) {
                        if (fields.optBoolean(Constants.KEY_RUM_VIEW_IS_ACTIVE, false)) {
                            if (tags != null) {
                                viewId = tags.optString(Constants.KEY_RUM_VIEW_ID);
                                break;
                            }
                        }

                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Assert.assertFalse(viewId.isEmpty());

        onView(ViewMatchers.withId(R.id.main_view_loop_test)).perform(ViewActions.scrollTo()).perform(click());

        Thread.sleep(5000);

        String newViewId = "";

        recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        for (SyncJsonData recordData : recordDataList) {
            try {
                JSONObject json = new JSONObject(recordData.getDataString());
                JSONObject tags = json.optJSONObject("tags");
                JSONObject fields = json.optJSONObject("fields");
                String measurement = json.optString("measurement");
                if (Constants.FT_MEASUREMENT_RUM_VIEW.equals(measurement)) {
                    if (fields != null) {
                        if (fields.optBoolean(Constants.KEY_RUM_VIEW_IS_ACTIVE, false)) {
                            if (tags != null) {
                                newViewId = tags.optString(Constants.KEY_RUM_VIEW_ID);
                                break;
                            }
                        }

                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Assert.assertNotEquals(viewId, newViewId);

    }
}
