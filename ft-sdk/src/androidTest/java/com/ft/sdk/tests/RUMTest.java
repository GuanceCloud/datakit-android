package com.ft.sdk.tests;

import static com.ft.sdk.tests.FTSdkAllTests.hasPrepare;

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
import com.ft.test.utils.LineProtocolData;
import com.ft.test.utils.RequestUtil;
import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Request;

public class RUMTest extends FTBaseTest {


    public static final String FIRST_VIEW = "FirstView";
    public static final String SECOND_VIEW = "SecondView";
    public static final String ROOT = "root";
    public static final String ACTION_NAME = "action";
    public static final long DURATION = 1000L;
    public static final String ACTION_TYPE_NAME = "action test";
    public static final String PROPERTY_NAME = "Property";
    public static final String PROPERTY_VALUE = "Property Value";
    public static final String PROPERTY_OVERRIDE_VALUE = "Property Value 2";
    public static final String ANY_ACTION = "AnyAction";
    public static final String LONG_TASK = "longTask";
    public static final String ERROR = "error";
    public static final String ERROR_MESSAGE = "error message";
    public static final String LONG_TASK_MESSAGE = "long task message";

    /**
     * 模拟 http 返回 头参数
     */
    public static final String RESOURCE_REQUEST_HEADER = "{x-datadog-parent-id=73566521391796532, x-datadog-sampling-priority=1, " +
            "ft-dio-key=a44e0ab0-232f-4f93-a6fd-b7a45cf8d20c, x-datadog-origin=rum, x-datadog-trace-id=123622877354441421}";
    /**
     * 模拟 http 请求 头参数
     */
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

    @Before
    public void initRUM() {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initRUMWithConfig(new FTRUMConfig().setRumAppId(TEST_FAKE_RUM_ID));
    }

    /**
     * {@link FTRUMGlobalManager#startAction(String, String)} 数据生成测试
     *
     * @throws Exception
     */
    @Test
    public void actionGenerateTest() throws Exception {
        FTRUMGlobalManager.get().startAction(ACTION_NAME, ACTION_TYPE_NAME);
        invokeCheckActionClose();
        waitEventConsumeInThreadPool();
        ArrayList<ActionBean> list = FTDBManager.get().querySumAction(0);

        ActionBean action = list.get(0);
        Assert.assertTrue(action.isClose());
        Assert.assertEquals(action.getActionName(), ACTION_NAME);
        Assert.assertEquals(action.getActionType(), ACTION_TYPE_NAME);


    }

    /**
     * {@link FTRUMGlobalManager#addAction(String, String, long, long)} } 数据生成测试
     *
     * @throws Exception
     */
    @Test
    public void addActionTest() throws Exception {
        HashMap<String, Object> property = new HashMap<>();
        property.put(PROPERTY_NAME, PROPERTY_VALUE);

        FTRUMGlobalManager.get().addAction(ACTION_NAME, ACTION_TYPE_NAME, DURATION, property);
        waitEventConsumeInThreadPool();

        List<SyncJsonData> list = FTDBManager.get().queryDataByDataByTypeLimit(0, DataType.RUM_APP);

        Assert.assertFalse(list.isEmpty());

        SyncJsonData data = list.get(0);
//        Assert.assertTrue(action.isClose());
        LineProtocolData lineProtocolData = new LineProtocolData(data.getDataString());
        Assert.assertEquals(lineProtocolData.getTagAsString("action_name"), ACTION_NAME);
        Assert.assertEquals(lineProtocolData.getTagAsString("action_type"), ACTION_TYPE_NAME);
        Assert.assertEquals(lineProtocolData.getField(PROPERTY_NAME), PROPERTY_VALUE);
    }

    /**
     * 数据连续写入
     * @throws InterruptedException
     */
    @Test
    public void multiActionData() throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            FTRUMGlobalManager.get().addAction(ACTION_NAME, ACTION_TYPE_NAME);
        }
        Thread.sleep(2000);
        List<SyncJsonData> list = FTDBManager.get().queryDataByDataByTypeLimit(0, DataType.RUM_APP);
        Assert.assertEquals(1000, list.size());
    }

    /**
     * Action 携带动态参数测试
     *
     * @throws Exception
     */
    @Test
    public void actionGenerateParamsTest() throws Exception {
        HashMap<String, Object> property = new HashMap<>();
        property.put(PROPERTY_NAME, PROPERTY_VALUE);
        FTRUMGlobalManager.get().startAction(ACTION_NAME, ACTION_TYPE_NAME, property);
        invokeCheckActionClose();
        waitEventConsumeInThreadPool();
        ArrayList<ActionBean> list = FTDBManager.get().querySumAction(0);
        ActionBean action = list.get(0);
        Assert.assertEquals(action.getProperty().get(PROPERTY_NAME), PROPERTY_VALUE);

        Thread.sleep(2000);

        Assert.assertTrue(CheckUtils.checkDynamicValue(PROPERTY_NAME, PROPERTY_VALUE,
                Constants.FT_MEASUREMENT_RUM_ACTION, DataType.RUM_APP, false));
    }

    /**
     * View 数据生成测试
     *
     * @throws Exception
     */

    @Test
    public void viewGenerateTest() throws Exception {
        FTRUMGlobalManager.get().onCreateView(FIRST_VIEW, DURATION);
        FTRUMGlobalManager.get().startView(FIRST_VIEW);
        FTRUMGlobalManager.get().stopView();

        Thread.sleep(300);

        FTRUMGlobalManager.get().startView(SECOND_VIEW);

        waitEventConsumeInThreadPool();

        ArrayList<ViewBean> list = FTDBManager.get().querySumView(0, true);
        Assert.assertEquals(list.size(), 2);

        ViewBean firstView = list.get(0);
        ViewBean secondView = list.get(1);

        Assert.assertNotEquals(firstView.getId(), secondView.getId());

        Assert.assertTrue(firstView.isClose());
        Assert.assertFalse(secondView.isClose());

        Assert.assertTrue(firstView.getLoadTime() > 0);
        Assert.assertEquals(-1, secondView.getLoadTime());

    }

    /**
     * View 携带动态参数测试
     *
     * @throws Exception
     */
    @Test
    public void viewGenerateParamsTest() throws Exception {
        HashMap<String, Object> property = new HashMap<>();
        property.put(PROPERTY_NAME, PROPERTY_VALUE);

        FTRUMGlobalManager.get().onCreateView(FIRST_VIEW, DURATION);
        FTRUMGlobalManager.get().startView(FIRST_VIEW, property);

        waitEventConsumeInThreadPool();

        ArrayList<ViewBean> list = FTDBManager.get().querySumView(0);
        ViewBean viewStart = list.get(0);
        Assert.assertEquals(viewStart.getProperty().get(PROPERTY_NAME), PROPERTY_VALUE);

        invokeGenerateRumData();

        Thread.sleep(2000);

        Assert.assertTrue(CheckUtils.checkDynamicValue(PROPERTY_NAME, PROPERTY_VALUE,
                Constants.FT_MEASUREMENT_RUM_VIEW, DataType.RUM_APP, false));

        property.put(PROPERTY_NAME, PROPERTY_OVERRIDE_VALUE);
        FTRUMGlobalManager.get().stopView(property);
        list = FTDBManager.get().querySumView(0);
        ViewBean viewStop = list.get(0);
        Assert.assertEquals(viewStop.getProperty().get(PROPERTY_NAME), PROPERTY_VALUE);

        invokeGenerateRumData();

        Thread.sleep(2000);

        Assert.assertTrue(CheckUtils.checkDynamicValue(PROPERTY_NAME, PROPERTY_OVERRIDE_VALUE,
                Constants.FT_MEASUREMENT_RUM_VIEW, DataType.RUM_APP, false));

    }

    /**
     * 模拟连续 view 跳转操作，最后验证各个 {@link ViewBean#viewReferrer}是否对应正确
     *
     * @throws InterruptedException
     */
    @Test
    public void viewMapTest() throws InterruptedException {
        FTRUMGlobalManager.get().startView(FIRST_VIEW);
        FTRUMGlobalManager.get().stopView();

        Thread.sleep(300);

        FTRUMGlobalManager.get().startView(SECOND_VIEW);
        FTRUMGlobalManager.get().stopView();

        waitEventConsumeInThreadPool();

        ArrayList<ViewBean> list = FTDBManager.get().querySumView(0, true);
        Assert.assertEquals(list.size(), 2);

        ViewBean firstView = list.get(0);
        ViewBean secondView = list.get(1);

        Assert.assertEquals(firstView.getViewName(), FIRST_VIEW);
        Assert.assertEquals(secondView.getViewName(), SECOND_VIEW);

        Assert.assertEquals(firstView.getViewReferrer(), ROOT);
        Assert.assertEquals(secondView.getViewReferrer(), FIRST_VIEW);


    }

    /**
     * 模拟进入 View 后，进行一个 Action ，一个 Resource，一个 Error，一个 LongTask
     * <p>
     * 最后验证 {@link ViewBean#resourceCount}
     * {@link ViewBean#errorCount}
     * {@link ViewBean#longTaskCount}
     * {@link ViewBean#actionCount} 数量是否正确
     *
     * @throws Exception
     */
    @Test
    public void viewActionSumTest() throws Exception {
        FTRUMGlobalManager.get().startView(FIRST_VIEW);

        FTRUMGlobalManager.get().startAction(ACTION_NAME, ACTION_TYPE_NAME);

        Request request = new Request.Builder().url(TEST_FAKE_URL).build();

        String resourceId = Utils.identifyRequest(request);

        Assert.assertFalse(resourceId.isEmpty());

        FTRUMGlobalManager.get().startResource(resourceId);
        FTRUMGlobalManager.get().stopResource(resourceId);

        FTRUMGlobalManager.get().addError(ERROR, ERROR_MESSAGE, ErrorType.JAVA, AppState.RUN);

        FTRUMGlobalManager.get().addLongTask(LONG_TASK_MESSAGE, DURATION);
        invokeCheckActionClose();
        waitEventConsumeInThreadPool();

        ArrayList<ActionBean> actionList = FTDBManager.get().querySumAction(0);
        Assert.assertEquals(actionList.size(), 1);

        ActionBean action = actionList.get(0);
        Assert.assertEquals(action.getResourceCount(), 1);
        Assert.assertEquals(action.getErrorCount(), 1);
        Assert.assertEquals(action.getLongTaskCount(), 1);

        waitEventConsumeInThreadPool();

        ArrayList<ViewBean> viewList = FTDBManager.get().querySumView(0);
        ViewBean view = viewList.get(0);

        Assert.assertEquals(view.getResourceCount(), 1);
        Assert.assertEquals(view.getErrorCount(), 1);
        Assert.assertEquals(view.getLongTaskCount(), 1);
        Assert.assertEquals(view.getActionCount(), 1);

    }

    /**
     * 模拟 session id 过期，在一个session id 过期之后，进行操作会生成新的 session id
     *
     * @throws Exception
     */
    @Test
    public void sessionIdTest() throws Exception {
        FTRUMGlobalManager.get().startView(FIRST_VIEW);
        FTRUMGlobalManager.get().startAction(ACTION_NAME, ACTION_TYPE_NAME);
        invokeCheckActionClose();
        waitEventConsumeInThreadPool();

        ArrayList<ActionBean> actionList = FTDBManager.get().querySumAction(0);
        ActionBean action = actionList.get(0);
        ArrayList<ViewBean> viewList = FTDBManager.get().querySumView(0);

        ViewBean view = viewList.get(0);
        Assert.assertFalse(action.getSessionId().isEmpty());
        Assert.assertEquals(action.getSessionId(), view.getSessionId());
        setSessionExpire();

        FTRUMGlobalManager.get().startAction(ACTION_NAME, ACTION_TYPE_NAME);
        invokeCheckActionClose();
        waitEventConsumeInThreadPool();

        ArrayList<ActionBean> newActionList = FTDBManager.get().querySumAction(0);
        ActionBean newAction = newActionList.get(1);

        Assert.assertNotEquals(action.getSessionId(), newAction.getSessionId());

    }

    /**
     * 模拟发起一 Resource 请求
     *
     * @throws InterruptedException
     */
    @Test
    public void resourceDataTest() throws InterruptedException {
        sendResource();

        Thread.sleep(2000);

        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        for (SyncJsonData recordData : recordDataList) {
            LineProtocolData data = new LineProtocolData(recordData.getDataString());

            if (Constants.FT_MEASUREMENT_RUM_RESOURCE.equals(data.getMeasurement())) {
                Assert.assertTrue(data.getFieldSize() > 0);
                break;
            }

        }
    }

    /**
     * 模拟 Resource 请求期间附带动态参数
     *
     * @throws InterruptedException
     */
    @Test
    public void resourceDataParamsTest() throws InterruptedException {
        HashMap<String, Object> property = new HashMap<>();
        property.put(PROPERTY_NAME, PROPERTY_VALUE);
        sendResource(property, null);

        Thread.sleep(2000);

        Assert.assertTrue(CheckUtils.checkDynamicValue(PROPERTY_NAME, PROPERTY_VALUE,
                Constants.FT_MEASUREMENT_RUM_RESOURCE, DataType.RUM_APP, false));

    }

    /**
     * Resource 动态参数覆盖测试
     *
     * @throws InterruptedException
     */
    @Test
    public void resourceDataParamsOverrideTest() throws InterruptedException {
        HashMap<String, Object> property = new HashMap<>();
        property.put(PROPERTY_NAME, PROPERTY_VALUE);
        HashMap<String, Object> propertyOverride = new HashMap<>();
        propertyOverride.put(PROPERTY_NAME, PROPERTY_OVERRIDE_VALUE);

        sendResource(property, propertyOverride);

        Thread.sleep(2000);

        Assert.assertTrue(CheckUtils.checkDynamicValue(PROPERTY_NAME, PROPERTY_OVERRIDE_VALUE,
                Constants.FT_MEASUREMENT_RUM_RESOURCE, DataType.RUM_APP, false));
    }

    /**
     * Error 数据测试
     *
     * @throws InterruptedException
     */
    @Test
    public void errorDataTest() throws InterruptedException {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initRUMWithConfig(new FTRUMConfig());
        FTRUMGlobalManager.get().addError(ERROR, ERROR_MESSAGE, ErrorType.JAVA, AppState.RUN);
        Thread.sleep(3000L);
        Assert.assertTrue(CheckUtils.checkValueInLineProtocol(DataType.RUM_APP,
                new String[]{ERROR, ERROR_MESSAGE}));
    }

    /**
     * Error 数据附带动态参数测试
     *
     * @throws InterruptedException
     */
    @Test
    public void errorDataParamsTest() throws InterruptedException {
        FTSdk.install(FTSDKConfig.builder(TEST_FAKE_URL));
        FTSdk.initRUMWithConfig(new FTRUMConfig());
        HashMap<String, Object> property = new HashMap<>();
        property.put(PROPERTY_NAME, PROPERTY_VALUE);
        FTRUMGlobalManager.get().addError(ERROR, ERROR_MESSAGE, ErrorType.JAVA, AppState.RUN, property);
        Thread.sleep(3000L);
        Assert.assertTrue(CheckUtils.checkDynamicValue(PROPERTY_NAME, PROPERTY_VALUE,
                Constants.FT_MEASUREMENT_RUM_ERROR, DataType.RUM_APP, false));
    }

    /**
     * LongTask 数据测试
     *
     * @throws InterruptedException
     */
    @Test
    public void longTaskTest() throws InterruptedException {
        FTRUMGlobalManager.get().addLongTask(LONG_TASK_MESSAGE, DURATION);
        Thread.sleep(3000L);
        Assert.assertTrue(CheckUtils.checkDynamicValue(Constants.KEY_RUM_LONG_TASK_STACK, LONG_TASK_MESSAGE,
                Constants.FT_MEASUREMENT_RUM_LONG_TASK, DataType.RUM_APP, false));
    }

    /**
     * LongTask 数据附带动态参数测试
     *
     * @throws InterruptedException
     */
    @Test
    public void longTaskParamsTest() throws InterruptedException {
        HashMap<String, Object> property = new HashMap<>();
        property.put(PROPERTY_NAME, PROPERTY_VALUE);
        FTRUMGlobalManager.get().addLongTask(LONG_TASK_MESSAGE, DURATION, property);
        Thread.sleep(3000L);
        Assert.assertTrue(CheckUtils.checkDynamicValue(PROPERTY_NAME, PROPERTY_VALUE,
                Constants.FT_MEASUREMENT_RUM_LONG_TASK, DataType.RUM_APP, false));
    }

    /**
     * {@link FTRUMConfig#samplingRate} 采样率为 0测试
     *
     * @throws Exception
     */
    @Test
    public void sampleRateZeroTest() throws Exception {
        FTSdk.initRUMWithConfig(new FTRUMConfig().setSamplingRate(0));

        generateRUMData();

        List<SyncJsonData> recordDataList = FTDBManager
                .get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);
        Assert.assertEquals(0, recordDataList.size());

        FTSdk.initRUMWithConfig(new FTRUMConfig().setSamplingRate(1f));

    }

    /**
     * {@link FTRUMConfig#samplingRate} 采样率 100% 测试
     *
     * @throws Exception
     */
    @Test
    public void sampleRate100Test() throws Exception {
        FTSdk.initRUMWithConfig(new FTRUMConfig().setSamplingRate(1f));

        generateRUMData();

        List<SyncJsonData> recordDataList = FTDBManager
                .get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);
        Assert.assertTrue(recordDataList.size() >= 5);

    }

    /**
     * View Action Error LongTask 数据生成
     *
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
        waitEventConsumeInThreadPool();
        Thread.sleep(3000L);
    }

    /**
     * 模拟发送 Resource 资源数据
     */
    private void sendResource() {
        sendResource(null, null);
    }

    /**
     * 模拟发送 Resource 资源数据
     *
     * @param property         动态参数
     * @param propertyOverride 需要覆盖的动态参数
     */
    private void sendResource(HashMap<String, Object> property, HashMap<String, Object> propertyOverride) {
        String resourceId = Utils.getGUID_16();
        FTRUMGlobalManager.get().startResource(resourceId, property);
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
        FTRUMGlobalManager.get().stopResource(resourceId, propertyOverride);
        FTRUMGlobalManager.get().addResource(resourceId, params, bean);
    }

    /**
     * {@link FTTraceConfig#enableLinkRUMData}  为 true 情况下，检验 RUM 是否包含链路链路相关数据
     *
     * @throws InterruptedException
     * @throws IOException
     */

    @Test
    public void traceLinkRUMDataEnable() throws InterruptedException, IOException {
        Assert.assertTrue(checkTraceHasLinkRumData(true));
    }

    /**
     * {@link FTTraceConfig#enableLinkRUMData}  为 false 情况下，检验 RUM 是否包含链路链路相关数据
     *
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void traceLinkRUMDataDisable() throws InterruptedException, IOException {
        Assert.assertFalse(checkTraceHasLinkRumData(false));
    }

    /**
     * 检验 RUM 数据 {@link Constants#KEY_RUM_RESOURCE_TRACE_ID},{@link Constants#KEY_RUM_RESOURCE_SPAN_ID }是否包含在内
     *
     * @param enableLinkRUMData
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    private boolean checkTraceHasLinkRumData(boolean enableLinkRUMData) throws InterruptedException, IOException {
        MockWebServer mockWebServer = new MockWebServer();

        MockResponse mockResponse = new MockResponse();
        mockResponse.setBody("");
        mockResponse.setResponseCode(HttpURLConnection.HTTP_OK);
        mockWebServer.enqueue(mockResponse);
        mockWebServer.play();

        FTSdk.initRUMWithConfig(new FTRUMConfig());

        FTSdk.initTraceWithConfig(new FTTraceConfig()
                .setTraceType(TraceType.DDTRACE)
                .setEnableAutoTrace(true)
                .setEnableLinkRUMData(enableLinkRUMData)
        );
        Thread.sleep(1000);

        RequestUtil.okhttpRequestUrl(mockWebServer.getUrl("/").toString());

        Thread.sleep(1000);

        List<SyncJsonData> recordDataList = FTDBManager.get()
                .queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);

        String tracId = "";
        String spanId = "";

        for (SyncJsonData recordData : recordDataList) {

            LineProtocolData data = new LineProtocolData(recordData.getDataString());

            if (Constants.FT_MEASUREMENT_RUM_RESOURCE.equals(data.getMeasurement())) {
                tracId = data.getTagAsString(Constants.KEY_RUM_RESOURCE_TRACE_ID, "");
                spanId = data.getTagAsString(Constants.KEY_RUM_RESOURCE_SPAN_ID, "");
                break;
            }

        }
        mockWebServer.shutdown();
        return !tracId.isEmpty() && !spanId.isEmpty();
    }

    /**
     * 检验如果 Resource 过程中未发生 Action 数据是否正，是否会监测到 Action
     *
     * @throws InterruptedException
     */
    @Test
    public void resourceWithOutActionId() throws InterruptedException {
        FTRUMGlobalManager.get().startView(ANY_VIEW);
        FTRUMGlobalManager.get().startAction(ANY_ACTION, "Any");
        Thread.sleep(100);

        FTRUMGlobalManager.get().stopView();
        sendResource();

        waitEventConsumeInThreadPool();

        List<SyncJsonData> recordDataList = FTDBManager.get().queryDataByDataByTypeLimitDesc(0, DataType.RUM_APP);
        Assert.assertFalse(recordDataList.isEmpty());
        for (SyncJsonData recordData : recordDataList) {
            LineProtocolData data = new LineProtocolData(recordData.getDataString());
            if (Constants.FT_MEASUREMENT_RUM_RESOURCE.equals(data.getMeasurement())) {
                Assert.assertNull(data.getTagAsString(Constants.KEY_RUM_ACTION_ID));
                break;
            }
        }

    }

}
