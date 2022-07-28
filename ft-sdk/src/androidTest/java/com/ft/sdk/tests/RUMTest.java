package com.ft.sdk.tests;

import static com.ft.sdk.FTSdkAllTests.hasPrepare;

import android.os.Looper;

import com.ft.sdk.FTRUMConfig;
import com.ft.sdk.FTRUMGlobalManager;
import com.ft.sdk.FTSDKConfig;
import com.ft.sdk.FTSdk;
import com.ft.sdk.FTTraceConfig;
import com.ft.sdk.TraceType;
import com.ft.sdk.garble.bean.ActionBean;
import com.ft.sdk.garble.bean.AppState;
import com.ft.sdk.garble.bean.DataType;
import com.ft.sdk.garble.bean.ErrorType;
import com.ft.sdk.garble.bean.NetStatusBean;
import com.ft.sdk.garble.bean.ResourceParams;
import com.ft.sdk.garble.bean.SyncJsonData;
import com.ft.sdk.garble.bean.ViewBean;
import com.ft.sdk.garble.db.FTDBManager;
import com.ft.sdk.garble.utils.Constants;
import com.ft.sdk.garble.utils.Utils;
import com.ft.test.base.FTBaseTest;
import com.ft.test.utils.CheckUtils;
import com.ft.test.utils.RequestUtil;
import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

public class RUMTest extends FTBaseTest {


    public static final String FIRST_VIEW = "FirstView";
    public static final String SECOND_VIEW = "SecondView";
    public static final String ROOT = "root";
    public static final String ACTION_NAME = "action";
    public static final long DURATION = 1000L;
    public static final String ACTION_TYPE_NAME = "action test";
    public static final String ANY_ACTION = "AnyAction";
    public static final String LONG_TASK = "longTask";
    public static final String ERROR = "error";
    public static final String ERROR_MESSAGE = "error message";
    public static final String RESOURCE_REQUEST_HEADER = "{x-datadog-parent-id=73566521391796532, x-datadog-sampling-priority=1, " +
            "ft-dio-key=a44e0ab0-232f-4f93-a6fd-b7a45cf8d20c, x-datadog-origin=rum, x-datadog-trace-id=123622877354441421}";
    public static final String RESOURCE_RESPONSE_HEADER = "{date=[Fri, 26 Nov 2021 06:08:47 GMT], server=[sffe], content-length=[1437]," +
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
    public static final String BODY_CONTENT = "body content";

    @BeforeClass
    public static void setUp() throws Exception {
        if (!hasPrepare) {
            Looper.prepare();
            hasPrepare = true;
        }
        stopSyncTask();
        avoidCleanData();
    }

    @Test
    public void actionGenerateTest() throws Exception {
        FTRUMGlobalManager.get().startAction(ACTION_NAME, ACTION_TYPE_NAME);
        invokeCheckActionClose();
        waitForInThreadPool();
        ArrayList<ActionBean> list = FTDBManager.get().querySumAction(0);

        ActionBean action = list.get(0);
        Assert.assertTrue(action.isClose());
        Assert.assertEquals(action.getActionName(), ACTION_NAME);
        Assert.assertEquals(action.getActionType(), ACTION_TYPE_NAME);


    }

    @Test
    public void addActionTest() throws Exception {
        Whitebox.invokeMethod(FTRUMGlobalManager.get(), "addAction",
                ACTION_NAME, ACTION_TYPE_NAME, DURATION);
        waitForInThreadPool();

        ArrayList<ActionBean> list = FTDBManager.get().querySumAction(0);

        ActionBean action = list.get(0);
        Assert.assertTrue(action.isClose());
        Assert.assertEquals(action.getActionName(), ACTION_NAME);
        Assert.assertEquals(action.getActionType(), ACTION_TYPE_NAME);
    }


    @Test
    public void viewGenerateTest() throws Exception {
        FTRUMGlobalManager.get().onCreateView(FIRST_VIEW, DURATION);
        FTRUMGlobalManager.get().startView(FIRST_VIEW);
        FTRUMGlobalManager.get().stopView();

        Thread.sleep(300);

        FTRUMGlobalManager.get().startView(SECOND_VIEW);

        waitForInThreadPool();

        ArrayList<ViewBean> list = FTDBManager.get().querySumView(0);
        Assert.assertEquals(list.size(), 2);

        ViewBean firstView = list.get(0);
        ViewBean secondView = list.get(1);

        Assert.assertNotEquals(firstView.getId(), secondView.getId());

        Assert.assertTrue(firstView.isClose());
        Assert.assertFalse(secondView.isClose());

        Assert.assertTrue(firstView.getLoadTime() > 0);
        Assert.assertEquals(-1, secondView.getLoadTime());

    }

    @Test
    public void viewMapTest() throws InterruptedException {
        FTRUMGlobalManager.get().startView(FIRST_VIEW);
        FTRUMGlobalManager.get().stopView();

        Thread.sleep(300);

        FTRUMGlobalManager.get().startView(SECOND_VIEW);
        FTRUMGlobalManager.get().stopView();

        waitForInThreadPool();

        ArrayList<ViewBean> list = FTDBManager.get().querySumView(0);
        Assert.assertEquals(list.size(), 2);

        ViewBean firstView = list.get(0);
        ViewBean secondView = list.get(1);

        Assert.assertEquals(firstView.getViewName(), FIRST_VIEW);
        Assert.assertEquals(secondView.getViewName(), SECOND_VIEW);

        Assert.assertEquals(firstView.getViewReferrer(), ROOT);
        Assert.assertEquals(secondView.getViewReferrer(), FIRST_VIEW);


    }

    @Test
    public void viewActionSumTest() throws Exception {
        FTRUMGlobalManager.get().startView(FIRST_VIEW);

        FTRUMGlobalManager.get().startAction(ACTION_NAME, ACTION_TYPE_NAME);

        Request request = new Request.Builder().url("https://www.baidu.com").build();

        String resourceId = Utils.identifyRequest(request);

        Assert.assertFalse(resourceId.isEmpty());

        FTRUMGlobalManager.get().startResource(resourceId);
        FTRUMGlobalManager.get().stopResource(resourceId);

        FTRUMGlobalManager.get().addError("error", "error msg", ErrorType.JAVA, AppState.RUN);


        FTRUMGlobalManager.get().addLongTask("longtask", DURATION);
        invokeCheckActionClose();
        waitForInThreadPool();

        ArrayList<ActionBean> actionList = FTDBManager.get().querySumAction(0);
        Assert.assertEquals(actionList.size(), 1);

        ActionBean action = actionList.get(0);
        Assert.assertEquals(action.getResourceCount(), 1);
        Assert.assertEquals(action.getErrorCount(), 1);
        Assert.assertEquals(action.getLongTaskCount(), 1);

        waitForInThreadPool();

        ArrayList<ViewBean> viewList = FTDBManager.get().querySumView(0);
        ViewBean view = viewList.get(0);

        Assert.assertEquals(view.getResourceCount(), 1);
        Assert.assertEquals(view.getErrorCount(), 1);
        Assert.assertEquals(view.getLongTaskCount(), 1);
        Assert.assertEquals(view.getActionCount(), 1);

    }

    @Test
    public void sessionIdTest() throws Exception {
        FTRUMGlobalManager.get().startView(FIRST_VIEW);
        FTRUMGlobalManager.get().startAction(ACTION_NAME, ACTION_TYPE_NAME);
        invokeCheckActionClose();
        waitForInThreadPool();

        ArrayList<ActionBean> actionList = FTDBManager.get().querySumAction(0);
        ActionBean action = actionList.get(0);
        ArrayList<ViewBean> viewList = FTDBManager.get().querySumView(0);

        ViewBean view = viewList.get(0);
        Assert.assertFalse(action.getSessionId().isEmpty());
        Assert.assertEquals(action.getSessionId(), view.getSessionId());
        setSessionExpire();

        FTRUMGlobalManager.get().startAction(ACTION_NAME, ACTION_TYPE_NAME);
        invokeCheckActionClose();
        waitForInThreadPool();

        ArrayList<ActionBean> newActionList = FTDBManager.get().querySumAction(0);
        ActionBean newAction = newActionList.get(1);

        Assert.assertNotEquals(action.getSessionId(), newAction.getSessionId());

    }


    @Test
    public void resourceDataTest() throws InterruptedException {
        sendResource();

        Thread.sleep(3000);

        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        for (SyncJsonData recordData : recordDataList) {
            try {
                JSONObject json = new JSONObject(recordData.getDataString());
                JSONObject fields = json.optJSONObject("fields");
                String measurement = json.optString("measurement");
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

    @Test
    public void errorDataTest() throws InterruptedException {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initRUMWithConfig(new FTRUMConfig());
        FTRUMGlobalManager.get().addError(ERROR, ERROR_MESSAGE, ErrorType.JAVA, AppState.RUN);
        Thread.sleep(3000L);
        Assert.assertTrue(CheckUtils.checkValue(DataType.RUM_APP,
                new String[]{ERROR, ERROR_MESSAGE}, 0));
    }

    @Test
    public void sampleRateZeroTest() throws Exception {
        FTSdk.initRUMWithConfig(new FTRUMConfig().setSamplingRate(0));

        generateRUMData();

        List<SyncJsonData> recordDataList = FTDBManager
                .get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);
        Assert.assertEquals(0, recordDataList.size());

    }

    /**
     * 测试采样率 100%
     *
     * @throws Exception
     */
    @Test
    public void sampleRate100Test() throws Exception {
        FTSdk.initRUMWithConfig(new FTRUMConfig().setSamplingRate(100));

        generateRUMData();

        List<SyncJsonData> recordDataList = FTDBManager
                .get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);
        Assert.assertTrue(recordDataList.size() >= 5);

    }

    /**
     * @throws Exception
     */
    private void generateRUMData() throws Exception {
        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTRUMGlobalManager.get().addError(ERROR, ERROR_MESSAGE, ErrorType.JAVA, AppState.RUN);
        FTRUMGlobalManager.get().addLongTask(LONG_TASK, DURATION);
        FTRUMGlobalManager.get().startAction(ANY_ACTION, "Any");
        invokeCheckActionClose();
        sendResource();
        FTRUMGlobalManager.get().stopView();
        waitForInThreadPool();
        Thread.sleep(3000L);
    }

    /**
     * 发送资源
     */
    private void sendResource() {
        String resourceId = Utils.getGUID_16();
        FTRUMGlobalManager.get().startResource(resourceId);
        ResourceParams params = new ResourceParams();
        params.resourceMethod = "GET";
        params.requestHeader = RESOURCE_REQUEST_HEADER;
        params.responseHeader = RESOURCE_RESPONSE_HEADER;

        params.responseBody = BODY_CONTENT;
        params.responseConnection = "";
        params.responseContentEncoding = "";
        params.responseContentType = "";
        params.url = TEST_FAKE_URL;
        params.resourceStatus = 200;

        NetStatusBean bean = new NetStatusBean();
        FTRUMGlobalManager.get().stopResource(resourceId);
        FTRUMGlobalManager.get().addResource(resourceId, params, bean);
    }

    /**
     * RUM link Trace enable
     *
     * @throws InterruptedException
     * @throws IOException
     */

    @Test
    public void traceLinkRUMDataEnable() throws InterruptedException, IOException {
        Assert.assertTrue(checkTraceHasLinkRumData(true));
    }

    /**
     * RUM link Trace disable
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void traceLinkRUMDataDisable() throws InterruptedException, IOException {
        Assert.assertFalse(checkTraceHasLinkRumData(false));
    }

    private boolean checkTraceHasLinkRumData(boolean enableLinkRUMData) throws InterruptedException, IOException {
        MockWebServer mockWebServer = new MockWebServer();

        MockResponse mockResponse = new MockResponse();
        mockResponse.setBody("");
        mockResponse.setResponseCode(HttpURLConnection.HTTP_OK);
        mockWebServer.enqueue(mockResponse);
        mockWebServer.play();

        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setTraceType(TraceType.DDTRACE)
                .setEnableAutoTrace(true)
                .setEnableLinkRUMData(enableLinkRUMData)
        );
        Thread.sleep(2000);

        RequestUtil.okhttpRequestUrl(mockWebServer.getUrl("/").toString());

        Thread.sleep(5000);

        List<SyncJsonData> recordDataList = FTDBManager.get()
                .queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        String tracId = "";
        String spanId = "";

        for (SyncJsonData recordData : recordDataList) {
            try {
                JSONObject json = new JSONObject(recordData.getDataString());
                JSONObject tags = json.optJSONObject("tags");
                String measurement = json.optString("measurement");
                if (Constants.FT_MEASUREMENT_RUM_RESOURCE.equals(measurement)) {
                    if (tags != null) {
                        tracId = tags.optString("trace_id");
                        spanId = tags.optString("span_id");
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mockWebServer.shutdown();
        return !tracId.isEmpty() && !spanId.isEmpty();
    }

    /**
     * @throws InterruptedException
     */
    @Test
    public void resourceWithOutActionId() throws InterruptedException {
        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTRUMGlobalManager.get().startAction(ANY_ACTION, "Any");
        Thread.sleep(100);

        FTRUMGlobalManager.get().stopView();
        sendResource();

        waitForInThreadPool();

        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        for (SyncJsonData recordData : recordDataList) {
            try {
                JSONObject json = new JSONObject(recordData.getDataString());
                JSONObject tags = json.optJSONObject("tags");
                String measurement = json.optString("measurement");
                if ("resource".equals(measurement)) {
                    if (tags != null) {
                        Assert.assertFalse(tags.has("action_id"));
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}
