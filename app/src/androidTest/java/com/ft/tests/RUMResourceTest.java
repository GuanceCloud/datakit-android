package com.ft.tests;

import static com.ft.AllTests.hasPrepare;

import android.content.Context;
import android.os.Looper;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ft.AccountUtils;
import com.ft.BaseTest;
import com.ft.DebugMainActivity;
import com.ft.application.MockApplication;
import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class RUMResourceTest extends BaseTest {


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
        FTSDKConfig ftsdkConfig = FTSDKConfig
                .builder(AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL));
        FTSdk.install(ftsdkConfig);


        FTSdk.initRUMWithConfig(new FTRUMConfig()
                .setRumAppId(AccountUtils.getProperty(context, AccountUtils.RUM_APP_ID)));

    }

    @Test
    public void resourceDataTest() throws InterruptedException {

        FTRUMGlobalManager.get().startView("Current","Preview");
        String resourceId = Utils.getGUID_16();
        FTRUMGlobalManager.get().startResource(resourceId);
        ResourceParams params = new ResourceParams();
        params.resourceMethod = "GET";
        params.requestHeader = "{x-datadog-parent-id=73566521391796532, x-datadog-sampling-priority=1, " +
                "ft-dio-key=a44e0ab0-232f-4f93-a6fd-b7a45cf8d20c, x-datadog-origin=rum, x-datadog-trace-id=123622877354441421}";

        params.responseHeader = "{date=[Fri, 26 Nov 2021 06:08:47 GMT], server=[sffe], content-length=[1437]," +
                " expires=[Fri, 01 Jan 1990 00:00:00 GMT], vary=[Accept-Encoding], content-encoding=[gzip]," +
                " pragma=[no-cache], last-modified=[Fri, 19 Jun 2020 10:30:00 GMT], x-xss-protection=[0], " +
                "x-content-type-options=[nosniff], content-type=[text/html]," +
                " content-security-policy-report-only=[script-src 'nonce-e-zcQTJtEfSCU-KEHJAWqw'" +
                " 'report-sample' 'strict-dynamic' 'unsafe-eval' 'unsafe-inline' http: https:;" +
                " object-src 'none'; report-uri https://csp.withgoogle.com/csp/static-on-bigtable; " +
                "base-uri 'none']," +
                " report-to=[{\"group\":\"static-on-bigtable\",\"max_age\":2592000,\"endpoints\"" +
                ":[{\"url\":\"https://csp.withgoogle.com/csp/report-to/static-on-bigtable\"}]}]," +
                " cross-origin-resource-policy=[cross-origin], cache-control=[no-cache, must-revalidate]," +
                " accept-ranges=[bytes], cross-origin-opener-policy-report-only=[same-origin; report-to=\"static-on-bigtable\"]}";

        params.responseBody = "body";
        params.responseConnection = "";
        params.responseContentEncoding = "";
        params.responseContentType = "";
        params.url = "https://www.baidu.com";
        params.resourceStatus = 200;

        NetStatusBean bean = new NetStatusBean();
        FTRUMGlobalManager.get().stopResource(resourceId);
        FTRUMGlobalManager.get().addResource(resourceId, params, bean);
        FTRUMGlobalManager.get().stopView();

        Thread.sleep(5000);

        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        for (SyncJsonData recordData : recordDataList) {
            try {
                JSONObject json = new JSONObject(recordData.getDataString());
                JSONObject fields = json.optJSONObject("fields");
                String measurement = json.optString("measurement");
                if ("view".equals(measurement)) {
                    if (fields != null) {
                        int resourceCount = fields.optInt("view_resource_count");
                        Assert.assertEquals(1, resourceCount);
                        break;
                    }
                }
                if ("resource".equals(measurement)) {
                    if (fields != null) {
                        Assert.assertTrue(fields.length() > 0);
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }



}
