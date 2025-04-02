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
import com.ft.sdk.FTInTakeUrlHandler;
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
 * RUM resource url 过滤校验
 */
@RunWith(AndroidJUnit4.class)
public class RUMResourceInTakeUrlTest extends BaseTest {

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
                .setEnableTrackAppCrash(true)
                .setRumAppId(BuildConfig.RUM_APP_ID)
                .setResourceUrlHandler(new FTInTakeUrlHandler() {
                    @Override
                    public boolean isInTakeUrl(String url) {
                        return url.equals(BuildConfig.TRACE_URL);
                    }
                })
                .setEnableTraceUserAction(true)
                .setEnableTraceUserResource(true)
        );

    }

    /**
     * 验证 OkHttp ft-plugin织入 Interceptor 相关代码的情况下，{@link FTRUMConfig#setResourceUrlHandler(FTInTakeUrlHandler)} 是否正确起小
     *
     * @throws Exception
     */
    @Test
    public void resourceInterceptorTest() throws Exception {
        Thread.sleep(2000);
        onView(ViewMatchers.withId(R.id.main_mock_okhttp_btn)).perform(ViewActions.scrollTo()).perform(click());
        invokeCheckActionClose();
        Thread.sleep(1000);

        List<SyncData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        for (SyncData recordData : recordDataList) {
            LineProtocolData lineProtocolData = new LineProtocolData(recordData.getDataString());
            String measurement = lineProtocolData.getMeasurement();
            if (Constants.FT_MEASUREMENT_RUM_ACTION.equals(measurement)) {
                String resourceCount = lineProtocolData.getFieldAsString(Constants.KEY_RUM_ACTION_RESOURCE_COUNT);
                Assert.assertEquals("0i", resourceCount);
                break;
            }
        }
    }


}
